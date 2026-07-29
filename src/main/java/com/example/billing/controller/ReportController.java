package com.example.billing.controller;

import com.example.billing.dto.ReportSummaryDto;
import com.example.billing.model.ErrorLog;
import com.example.billing.service.AuditService;
import com.example.billing.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;

    public ReportController(ReportService reportService, AuditService auditService) {
        this.reportService = reportService;
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> getSummary(@RequestParam int month, @RequestParam int year) {
        auditService.logAction("Reports", "Generated Billing Summary for " + month + "/" + year);
        return ResponseEntity.ok(reportService.getSummary(month, year));
    }

    @GetMapping("/failed")
    public ResponseEntity<List<ErrorLog>> getFailedRecords(@RequestParam int month, @RequestParam int year) {
        auditService.logAction("Reports", "Generated Failed Records report for " + month + "/" + year);
        return ResponseEntity.ok(reportService.getFailedRecords(month, year));
    }
}