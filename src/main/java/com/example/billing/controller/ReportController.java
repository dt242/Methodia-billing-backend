package com.example.billing.controller;

import com.example.billing.dto.ReportSummaryDto;
import com.example.billing.model.ErrorLog;
import com.example.billing.service.AuditService;
import com.example.billing.service.ReportExportService;
import com.example.billing.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;
    private final AuditService auditService;

    public ReportController(ReportService reportService, ReportExportService reportExportService, AuditService auditService) {
        this.reportService = reportService;
        this.reportExportService = reportExportService;
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

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam String type, @RequestParam int month, @RequestParam int year) {
        byte[] pdfBytes = reportExportService.exportToPdf(type, month, year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report_" + type.toLowerCase() + "_" + month + "_" + year + ".pdf");

        auditService.logAction("Reports", "Exported " + type + " report to PDF for " + month + "/" + year);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam String type, @RequestParam int month, @RequestParam int year) {
        byte[] excelBytes = reportExportService.exportToExcel(type, month, year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "report_" + type.toLowerCase() + "_" + month + "_" + year + ".xlsx");

        auditService.logAction("Reports", "Exported " + type + " report to Excel for " + month + "/" + year);
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}