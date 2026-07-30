package com.example.billing.controller;

import com.example.billing.dto.SelfReportRequest;
import com.example.billing.service.ReadingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/readings")
@CrossOrigin(origins = "*")
public class ClientReadingController {

    private final ReadingService readingService;

    public ClientReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping
    public ResponseEntity<String> submitSelfReport(@Valid @RequestBody SelfReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        readingService.submitSelfReport(request, auth.getName());

        return ResponseEntity.ok("Самоотчетът е изпратен успешно и чака одобрение от администратор.");
    }
}