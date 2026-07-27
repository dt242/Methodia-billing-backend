package com.example.billing.controller;

import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Reading;
import com.example.billing.model.ReadingStatus;
import com.example.billing.repository.ReadingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/readings")
@CrossOrigin(origins = "*")
public class AdminReadingController {

    private final ReadingRepository readingRepository;

    public AdminReadingController(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
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
        return ResponseEntity.ok("Отчетът е успешно валидиран.");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectReading(@PathVariable String id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчетът не е намерен."));

        reading.setStatus(ReadingStatus.REJECTED);
        readingRepository.save(reading);
        return ResponseEntity.ok("Отчетът е отхвърлен.");
    }
}