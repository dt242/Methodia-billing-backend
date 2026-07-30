package com.example.billing.controller;

import com.example.billing.model.Reading;
import com.example.billing.model.ReadingStatus;
import com.example.billing.service.ReadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/readings")
@CrossOrigin(origins = "*")
public class AdminReadingController {

    private final ReadingService readingService;

    public AdminReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Reading>> getPendingReadings() {
        return ResponseEntity.ok(readingService.getPendingReadings());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveReading(@PathVariable String id) {
        readingService.approveReading(id);
        return ResponseEntity.ok("Отчетът е успешно валидиран.");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectReading(@PathVariable String id) {
        readingService.rejectReading(id);
        return ResponseEntity.ok("Отчетът е отхвърлен.");
    }

    @GetMapping
    public ResponseEntity<List<Reading>> getAllReadings(
            @RequestParam(required = false) ReadingStatus status) {

        return ResponseEntity.ok(readingService.getAllReadings(status));
    }
}