package com.example.billing.controller;

import com.example.billing.dto.BillingRequest;
import com.example.billing.model.Invoice;
import com.example.billing.repository.InvoiceRepository;
import com.example.billing.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    public BillingController(InvoiceService invoiceService, InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping("/invoice")
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody BillingRequest request) {
        Invoice invoice = invoiceService.generateInvoice(request.reference(), request.product());
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceRepository.findAll());
    }
}