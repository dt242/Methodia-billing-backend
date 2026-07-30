package com.example.billing.controller;

import com.example.billing.model.Price;
import com.example.billing.service.PriceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tariffs")
@CrossOrigin(origins = "*")
public class AdminTariffController {

    private final PriceService priceService;

    public AdminTariffController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public ResponseEntity<List<Price>> getAllTariffs(
            @RequestParam(required = false) String tariffCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(priceService.getAllTariffs(tariffCode, startDate, endDate));
    }
}