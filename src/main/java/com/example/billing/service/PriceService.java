package com.example.billing.service;

import com.example.billing.model.Price;
import com.example.billing.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PriceService {

    private final PriceRepository priceRepository;
    private final AuditService auditService;

    public PriceService(PriceRepository priceRepository, AuditService auditService) {
        this.priceRepository = priceRepository;
        this.auditService = auditService;
    }

    public List<Price> getAllTariffs(String tariffCode, LocalDate startDate, LocalDate endDate) {
        auditService.logAction("Tariff Plans", "Admin viewed tariff plans list with filters");
        return priceRepository.findWithFilters(tariffCode, startDate, endDate);
    }
}