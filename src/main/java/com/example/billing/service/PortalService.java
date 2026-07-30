package com.example.billing.service;

import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Invoice;
import com.example.billing.model.Reading;
import com.example.billing.model.User;
import com.example.billing.repository.InvoiceRepository;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReadingRepository readingRepository;
    private final AuditService auditService;

    public PortalService(UserRepository userRepository, InvoiceRepository invoiceRepository,
                         ReadingRepository readingRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.readingRepository = readingRepository;
        this.auditService = auditService;
    }

    public List<Invoice> getMyInvoices(String reference) {
        User client = userRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен в системата."));

        auditService.logAction("Customer Portal", "Client viewed their invoices history");
        return invoiceRepository.findByUserOrderByDateTimeDesc(client);
    }

    public List<Reading> getMyUsage(String reference) {
        User client = userRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен в системата."));

        auditService.logAction("Customer Portal", "Client viewed their usage history");
        return readingRepository.findByUserOrderByDateTimeDesc(client);
    }
}