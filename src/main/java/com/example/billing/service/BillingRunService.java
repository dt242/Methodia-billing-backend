package com.example.billing.service;

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
        BillingRun run = new BillingRun(month, year);
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
                    processSingleClient(client, frozenPrices);
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

    private void processSingleClient(User client, List<Price> frozenPrices) {
        List<Reading> readings = readingRepository.findByUserAndProductAndStatusOrderByDateTimeAsc(client, Product.GAS, ReadingStatus.VALIDATED);
        if (readings.size() < 2) return;

        Reading latestReading = readings.get(readings.size() - 1);
        if (latestReading.isInvoiced()) {
            return;
        }

        if (readings.size() >= 3) {
            Reading r1 = readings.get(readings.size() - 3);
            Reading r2 = readings.get(readings.size() - 2);
            Reading r3 = readings.get(readings.size() - 1);

            BigDecimal previousConsumption = r2.getLastReading().subtract(r1.getLastReading());
            BigDecimal currentConsumption = r3.getLastReading().subtract(r2.getLastReading());

            boolean needsManualCheck = false;

            if (previousConsumption.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = currentConsumption.subtract(previousConsumption).abs();
                BigDecimal percentage = difference.divide(previousConsumption, 2, RoundingMode.HALF_UP);

                if (percentage.compareTo(new BigDecimal("0.50")) > 0) {
                    needsManualCheck = true;
                    logError(ErrorSeverity.WARNING, "Отклонение в потреблението над 50%. Предишно: "
                            + previousConsumption + ", Текущо: " + currentConsumption, client.getReference(), "Validation");
                }
            }

            Invoice invoice = invoiceService.generateInvoice(client.getReference(), Product.GAS, frozenPrices);
            if (needsManualCheck) {
                invoice.setRequiresManualCheck(true);
                invoiceRepository.save(invoice);
            }
        } else {
            invoiceService.generateInvoice(client.getReference(), Product.GAS, frozenPrices);
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