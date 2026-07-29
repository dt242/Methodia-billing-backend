package com.example.billing.service;

import com.example.billing.exception.InvalidDataException;
import com.example.billing.model.*;
import com.example.billing.repository.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BillingRunService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;
    private final InvoiceService invoiceService;
    private final BillingRunRepository billingRunRepository;
    private final ErrorLogRepository errorLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    public BillingRunService(UserRepository userRepository, ReadingRepository readingRepository,
                             PriceRepository priceRepository, InvoiceService invoiceService, BillingRunRepository billingRunRepository,
                             ErrorLogRepository errorLogRepository, InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
        this.invoiceService = invoiceService;
        this.billingRunRepository = billingRunRepository;
        this.errorLogRepository = errorLogRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Async
    public void startBillingRun(int month, int year) {
        if (billingRunRepository.findByBillingMonthAndBillingYear(month, year).isPresent()) {
            throw new InvalidDataException("Вече съществува Billing Run за този период. Използвайте Restart, ако искате да го пуснете отново.");
        }

        BillingRun run = new BillingRun(month, year);
        billingRunRepository.save(run);
        isPaused.set(false);

        List<User> clients = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CLIENT)
                .toList();

        List<Price> frozenPrices = priceRepository.findAll();

        if (frozenPrices.isEmpty() || clients.isEmpty()) {
            run.setStatus(BillingStatus.FAILED);
            run.setEndTime(OffsetDateTime.now());
            billingRunRepository.save(run);
            logError(ErrorSeverity.CRITICAL, "Липсват клиенти или тарифи за стартиране на процеса.", null, "BillingRun");
            return;
        }

        processClients(clients, run, frozenPrices);
    }

    @Async
    public void restartBillingRun(int month, int year) {
        BillingRun run = billingRunRepository.findByBillingMonthAndBillingYear(month, year)
                .orElseThrow(() -> new InvalidDataException("Няма съществуващ процес за " + month + "/" + year + ", който да бъде рестартиран."));

        if (run.getStatus() == BillingStatus.IN_PROGRESS) {
            throw new InvalidDataException("Процесът вече се изпълнява в момента.");
        }

        run.setStatus(BillingStatus.IN_PROGRESS);
        run.setEndTime(null);
        billingRunRepository.save(run);

        isPaused.set(false);

        List<User> clients = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CLIENT)
                .toList();

        List<Price> frozenPrices = priceRepository.findAll();

        processClients(clients, run, frozenPrices);
    }

    public void pauseBillingRun() {
        isPaused.set(true);
    }

    private void processClients(List<User> clients, BillingRun run, List<Price> frozenPrices) {
        try {
            for (User client : clients) {
                while (isPaused.get()) {
                    Thread.sleep(2000);
                    run.setStatus(BillingStatus.PAUSED);
                    billingRunRepository.save(run);
                }

                run.setStatus(BillingStatus.IN_PROGRESS);

                try {
                    processSingleClient(client, frozenPrices, run.getBillingMonth(), run.getBillingYear());
                } catch (Exception e) {
                    logError(ErrorSeverity.ERROR, "Грешка при обработка: " + e.getMessage(), client.getReference(), "BillingRun");
                }
            }

            run.setStatus(BillingStatus.COMPLETED);
        } catch (Exception e) {
            run.setStatus(BillingStatus.FAILED);
            logError(ErrorSeverity.CRITICAL, "Системен срив: " + e.getMessage(), null, "BillingRun");
        } finally {
            run.setEndTime(OffsetDateTime.now());
            billingRunRepository.save(run);
        }
    }

    private void processSingleClient(User client, List<Price> frozenPrices, int targetMonth, int targetYear) {
        List<Reading> readings = readingRepository.findByUserAndProductAndStatusOrderByDateTimeAsc(client, Product.GAS, ReadingStatus.VALIDATED);

        if (readings.size() < 2) return;

        int targetIndex = -1;
        for (int i = readings.size() - 1; i >= 0; i--) {
            OffsetDateTime dt = readings.get(i).getDateTime();
            if (dt.getMonthValue() == targetMonth && dt.getYear() == targetYear) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex < 1) {
            return;
        }

        Reading endReading = readings.get(targetIndex);
        if (endReading.isInvoiced()) {
            return;
        }

        Reading startReading = readings.get(targetIndex - 1);

        boolean needsManualCheck = false;
        if (targetIndex >= 2) {
            Reading previousStartReading = readings.get(targetIndex - 2);

            BigDecimal previousConsumption = startReading.getLastReading().subtract(previousStartReading.getLastReading());
            BigDecimal currentConsumption = endReading.getLastReading().subtract(startReading.getLastReading());

            if (previousConsumption.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = currentConsumption.subtract(previousConsumption).abs();
                BigDecimal percentage = difference.divide(previousConsumption, 2, RoundingMode.HALF_UP);

                if (percentage.compareTo(new BigDecimal("0.50")) > 0) {
                    needsManualCheck = true;
                    logError(ErrorSeverity.WARNING, "Отклонение в потреблението над 50%. Предишно: "
                            + previousConsumption + ", Текущо: " + currentConsumption, client.getReference(), "Validation");
                }
            }
        }

        Invoice invoice = invoiceService.generateInvoice(client, startReading, endReading, frozenPrices);
        if (needsManualCheck) {
            invoice.setRequiresManualCheck(true);
            invoiceRepository.save(invoice);
        }
    }

    private void logError(ErrorSeverity severity, String description, String customerId, String process) {
        ErrorLog error = new ErrorLog(severity, description, customerId, process);
        errorLogRepository.save(error);
    }

    public void resumeBillingRun() {
        isPaused.set(false);
    }
}