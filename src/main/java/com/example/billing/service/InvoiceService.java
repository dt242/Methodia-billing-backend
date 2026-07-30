package com.example.billing.service;

import com.example.billing.distribution.service.ProportionalDistributionService;
import com.example.billing.exception.InvalidDataException;
import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.*;
import com.example.billing.repository.InvoiceRepository;
import com.example.billing.repository.LineRepository;
import com.example.billing.repository.PriceRepository;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;
    private final ProportionalDistributionService distributionService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGenerator numberGenerator;
    private final PdfGenerationService pdfGenerationService;
    private final AuditService auditService;

    public InvoiceService(UserRepository userRepository, ReadingRepository readingRepository,
                          PriceRepository priceRepository, ProportionalDistributionService distributionService,
                          InvoiceRepository invoiceRepository,
                          InvoiceNumberGenerator numberGenerator, PdfGenerationService pdfGenerationService, AuditService auditService) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
        this.distributionService = distributionService;
        this.invoiceRepository = invoiceRepository;
        this.numberGenerator = numberGenerator;
        this.pdfGenerationService = pdfGenerationService;
        this.auditService = auditService;
    }

    @Transactional
    public Invoice generateInvoice(String userReference, Product product) {
        List<Price> currentPrices = priceRepository.findAll();
        return generateInvoice(userReference, product, currentPrices);
    }

    @Transactional
    public Invoice generateInvoice(String userReference, Product product, List<Price> frozenPrices) {
        User user = userRepository.findByReference(userReference)
                .orElseThrow(() -> new ResourceNotFoundException("Потребител с референция " + userReference + " не е намерен!"));

        List<Reading> readings = readingRepository.findByUserAndProductAndStatusOrderByDateTimeAsc(user, product, ReadingStatus.VALIDATED);
        if (readings.size() < 2) {
            throw new InvalidDataException("Няма достатъчно показания за този потребител, за да се изчисли консумацията.");
        }
        Reading startReading = readings.get(readings.size() - 2);
        Reading endReading = readings.get(readings.size() - 1);

        return generateInvoice(user, startReading, endReading, frozenPrices);
    }

    @Transactional
    public Invoice generateInvoice(User user, Reading startReading, Reading endReading, List<Price> frozenPrices) {
        if (endReading.isInvoiced()) {
            throw new InvalidDataException("Последният отчет за този клиент вече е фактуриран.");
        }

        List<Price> prices = frozenPrices.stream()
                .filter(p -> p.getProduct() == endReading.getProduct() && p.getTariffCode().equals(user.getTariffCode()))
                .sorted(java.util.Comparator.comparing(Price::getStartDate))
                .toList();

        List<Line> calculatedLines = distributionService.distribute(startReading, endReading, prices);

        BigDecimal totalAmount = calculatedLines.stream()
                .map(Line::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice invoice = new Invoice();
        invoice.setId(Invoice.generateUuid());
        invoice.setDateTime(OffsetDateTime.now());
        invoice.setNumber(numberGenerator.getNextNumber());
        invoice.setUser(user);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaid(false);

        int counter = 1;
        for (Line line : calculatedLines) {
            line.setId(Line.generateUuid());
            line.setLineId(counter++);
            invoice.addLine(line);
        }
        Invoice savedInvoice = invoiceRepository.save(invoice);
        endReading.setInvoiced(true);
        readingRepository.save(endReading);

        return savedInvoice;
    }

    @Transactional
    public Invoice regenerateInvoice(String invoiceId) {
        Invoice oldInvoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Фактурата не е намерена."));

        if (oldInvoice.getLines().isEmpty()) {
            throw new InvalidDataException("Фактурата няма редове и не може да бъде прегенерирана.");
        }

        User user = oldInvoice.getUser();
        Product product = oldInvoice.getLines().get(0).getProduct();
        OffsetDateTime firstLineStart = oldInvoice.getLines().get(0).getStartDateTime();
        OffsetDateTime lastLineEnd = oldInvoice.getLines().get(oldInvoice.getLines().size() - 1).getEndDateTime();

        List<Reading> readings = readingRepository.findByUserAndProductAndStatusOrderByDateTimeAsc(user, product, ReadingStatus.VALIDATED);

        Reading startReading = readings.stream()
                .filter(r -> !r.getDateTime().isAfter(firstLineStart))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new InvalidDataException("Не е намерен начален отчет за тази фактура."));

        Reading endReading = readings.stream()
                .filter(r -> !r.getDateTime().isBefore(lastLineEnd))
                .findFirst()
                .orElseThrow(() -> new InvalidDataException("Не е намерен краен отчет за тази фактура."));

        endReading.setInvoiced(false);
        readingRepository.save(endReading);
        invoiceRepository.delete(oldInvoice);
        invoiceRepository.flush();
        List<Price> currentPrices = priceRepository.findAll();

        return generateInvoice(user, startReading, endReading, currentPrices);
    }

    public Page<Invoice> getInvoices(String invoiceNumber, String customerName, OffsetDateTime startDate, OffsetDateTime endDate, Boolean isPaid, Pageable pageable) {
        auditService.logAction("Invoices", "Admin viewed invoices list with filters");
        return invoiceRepository.findWithFilters(invoiceNumber, customerName, startDate, endDate, isPaid, pageable);
    }

    public byte[] generatePdfForInvoice(String invoiceId, String username, boolean isAdmin) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Фактурата не е намерена"));

        if (!isAdmin && !invoice.getUser().getReference().equals(username)) {
            throw new IllegalArgumentException("Нямате достъп до тази фактура.");
        }

        return pdfGenerationService.generateInvoicePdf(invoice);
    }
}