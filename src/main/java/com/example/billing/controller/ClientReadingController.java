package com.example.billing.controller;

import com.example.billing.dto.SelfReportRequest;
import com.example.billing.model.Reading;
import com.example.billing.model.ReadingStatus;
import com.example.billing.model.User;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import com.example.billing.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/readings")
@CrossOrigin(origins = "*")
public class ClientReadingController {

    private final ReadingRepository readingRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ClientReadingController(ReadingRepository readingRepository, UserRepository userRepository, AuditService auditService) {
        this.readingRepository = readingRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<String> submitSelfReport(@Valid @RequestBody SelfReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String reference = auth.getName();

        User user = userRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен"));

        Reading reading = new Reading(
                user,
                request.product(),
                request.dateTime(),
                request.lastReading(),
                true,
                ReadingStatus.UNVALIDATED
        );

        readingRepository.save(reading);
        auditService.logAction("Usage Data", "Client submitted a self-report for product: " + request.product());
        return ResponseEntity.ok("Самоотчетът е изпратен успешно и чака одобрение от администратор.");
    }
}