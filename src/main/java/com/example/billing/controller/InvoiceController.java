package com.example.billing.controller;

import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Invoice;
import com.example.billing.repository.InvoiceRepository;
import com.example.billing.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final PdfGenerationService pdfGenerationService;

    public InvoiceController(InvoiceRepository invoiceRepository, PdfGenerationService pdfGenerationService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfGenerationService = pdfGenerationService;
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
}