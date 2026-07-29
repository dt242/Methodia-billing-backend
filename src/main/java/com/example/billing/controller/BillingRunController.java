package com.example.billing.controller;

import com.example.billing.service.AuditService;
import com.example.billing.service.BillingRunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/billing-run")
@CrossOrigin(origins = "*")
public class BillingRunController {

    private final BillingRunService billingRunService;
    private final AuditService auditService;

    public BillingRunController(BillingRunService billingRunService, AuditService auditService) {
        this.billingRunService = billingRunService;
        this.auditService = auditService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startRun(@RequestParam int month, @RequestParam int year) {
        billingRunService.startBillingRun(month, year);
        auditService.logAction("Billing Run", "Started Billing Run for " + month + "/" + year);
        return ResponseEntity.ok("Billing Run стартира успешно във фонов режим.");
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pauseRun() {
        billingRunService.pauseBillingRun();
        auditService.logAction("Billing Run", "Paused current Billing Run");
        return ResponseEntity.ok("Сигнал за пауза е изпратен. Процесът ще спре след текущия клиент.");
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resumeRun() {
        billingRunService.resumeBillingRun();
        auditService.logAction("Billing Run", "Resumed current Billing Run");
        return ResponseEntity.ok("Billing Run процесът е възобновен успешно.");
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartRun(@RequestParam int month, @RequestParam int year) {
        billingRunService.restartBillingRun(month, year);
        auditService.logAction("Billing Run", "Restarted Billing Run for " + month + "/" + year);
        return ResponseEntity.ok("Billing Run процесът е рестартиран успешно.");
    }
}