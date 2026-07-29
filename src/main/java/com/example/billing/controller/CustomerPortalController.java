package com.example.billing.controller;

import com.example.billing.model.Invoice;
import com.example.billing.model.Reading;
import com.example.billing.model.User;
import com.example.billing.repository.InvoiceRepository;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import com.example.billing.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/portal")
@CrossOrigin(origins = "*")
public class CustomerPortalController {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReadingRepository readingRepository;
    private final AuditService auditService;

    public CustomerPortalController(UserRepository userRepository, InvoiceRepository invoiceRepository,
                                    ReadingRepository readingRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.readingRepository = readingRepository;
        this.auditService = auditService;
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices() {
        User client = getAuthenticatedClient();
        auditService.logAction("Customer Portal", "Client viewed their invoices history");
        return ResponseEntity.ok(invoiceRepository.findByUserOrderByDateTimeDesc(client));
    }

    @GetMapping("/usage")
    public ResponseEntity<List<Reading>> getMyUsage() {
        User client = getAuthenticatedClient();
        auditService.logAction("Customer Portal", "Client viewed their usage history");
        return ResponseEntity.ok(readingRepository.findByUserOrderByDateTimeDesc(client));
    }

    private User getAuthenticatedClient() {
        String reference = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен в системата."));
    }
}