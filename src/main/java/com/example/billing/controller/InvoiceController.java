package com.example.billing.controller;

import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Invoice;
import com.example.billing.repository.InvoiceRepository;
import com.example.billing.service.AuditService;
import com.example.billing.service.InvoiceService;
import com.example.billing.service.PdfGenerationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final PdfGenerationService pdfGenerationService;
    private final AuditService auditService;
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceRepository invoiceRepository, PdfGenerationService pdfGenerationService, AuditService auditService, InvoiceService invoiceService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfGenerationService = pdfGenerationService;
        this.auditService = auditService;
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<Page<Invoice>> getInvoices(
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) Boolean isPaid,
            @PageableDefault(size = 20, sort = "dateTime") Pageable pageable) {

        Page<Invoice> invoices = invoiceRepository.findWithFilters(invoiceNumber, customerName, startDate, endDate, isPaid, pageable);
        auditService.logAction("Invoices", "Admin viewed invoices list with filters");
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фактурата не е намерена"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !invoice.getUser().getReference().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + invoice.getNumber() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/regenerate")
    public ResponseEntity<Invoice> regenerateInvoice(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Invoice newInvoice = invoiceService.regenerateInvoice(id);
        auditService.logAction("Invoices", "Regenerated invoice. Old ID: " + id + ", New ID: " + newInvoice.getId());
        return ResponseEntity.ok(newInvoice);
    }
}