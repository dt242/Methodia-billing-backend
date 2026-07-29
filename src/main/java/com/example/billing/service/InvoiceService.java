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
    private final LineRepository lineRepository;
    private final InvoiceNumberGenerator numberGenerator;

    public InvoiceService(UserRepository userRepository, ReadingRepository readingRepository,
                          PriceRepository priceRepository, ProportionalDistributionService distributionService,
                          InvoiceRepository invoiceRepository, LineRepository lineRepository,
                          InvoiceNumberGenerator numberGenerator) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
        this.distributionService = distributionService;
        this.invoiceRepository = invoiceRepository;
        this.lineRepository = lineRepository;
        this.numberGenerator = numberGenerator;
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

        List<Price> prices = frozenPrices.stream()
                .filter(p -> p.getProduct() == product && p.getPriceList() == user.getPriceListId())
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
        Invoice savedInvoice = invoiceRepository.save(invoice);

        int counter = 1;
        for (Line line : calculatedLines) {
            line.setId(Line.generateUuid());
            line.setLineId(counter++);
            line.setInvoice(savedInvoice);
        }
        lineRepository.saveAll(calculatedLines);

        return savedInvoice;
    }
}