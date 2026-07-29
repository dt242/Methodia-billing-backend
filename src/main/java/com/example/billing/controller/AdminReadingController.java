package com.example.billing.controller;

import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Reading;
import com.example.billing.model.ReadingStatus;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/readings")
@CrossOrigin(origins = "*")
public class AdminReadingController {

    private final ReadingRepository readingRepository;
    private final AuditService auditService;


    public AdminReadingController(ReadingRepository readingRepository, AuditService auditService) {
        this.readingRepository = readingRepository;
        this.auditService = auditService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Reading>> getPendingReadings() {
        List<Reading> pending = readingRepository.findByStatusOrderByDateTimeAsc(ReadingStatus.UNVALIDATED);
        return ResponseEntity.ok(pending);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveReading(@PathVariable String id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчетът не е намерен."));

        reading.setStatus(ReadingStatus.VALIDATED);
        readingRepository.save(reading);
        auditService.logAction("Usage Data", "Approved reading with ID: " + id);
        return ResponseEntity.ok("Отчетът е успешно валидиран.");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectReading(@PathVariable String id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчетът не е намерен."));

        reading.setStatus(ReadingStatus.REJECTED);
        readingRepository.save(reading);
        auditService.logAction("Usage Data", "Rejected reading with ID: " + id);
        return ResponseEntity.ok("Отчетът е отхвърлен.");
    }

    @GetMapping
    public ResponseEntity<List<Reading>> getAllReadings(
            @RequestParam(required = false) ReadingStatus status) {

        List<Reading> readings = readingRepository.findWithFilters(status);
        auditService.logAction("Usage Data", "Admin viewed all usage data readings");
        return ResponseEntity.ok(readings);
    }
}