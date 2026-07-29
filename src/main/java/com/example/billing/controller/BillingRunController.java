package com.example.billing.controller;

import com.example.billing.service.BillingRunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/billing-run")
@CrossOrigin(origins = "*")
public class BillingRunController {

    private final BillingRunService billingRunService;

    public BillingRunController(BillingRunService billingRunService) {
        this.billingRunService = billingRunService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startRun(@RequestParam int month, @RequestParam int year) {
        billingRunService.startBillingRun(month, year);
        return ResponseEntity.ok("Billing Run стартира успешно във фонов режим.");
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pauseRun() {
        billingRunService.pauseBillingRun();
        return ResponseEntity.ok("Сигнал за пауза е изпратен. Процесът ще спре след текущия клиент.");
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resumeRun() {
        billingRunService.resumeBillingRun();
        return ResponseEntity.ok("Billing Run процесът е възобновен успешно.");
    }
}