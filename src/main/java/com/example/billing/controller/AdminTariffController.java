package com.example.billing.controller;

import com.example.billing.model.Price;
import com.example.billing.repository.PriceRepository;
import com.example.billing.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tariffs")
@CrossOrigin(origins = "*")
public class AdminTariffController {

    private final PriceRepository priceRepository;
    private final AuditService auditService;

    public AdminTariffController(PriceRepository priceRepository, AuditService auditService) {
        this.priceRepository = priceRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Price>> getAllTariffs(
            @RequestParam(required = false) String tariffCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Price> prices = priceRepository.findWithFilters(tariffCode, startDate, endDate);
        auditService.logAction("Tariff Plans", "Admin viewed tariff plans list with filters");
        return ResponseEntity.ok(prices);
    }
}