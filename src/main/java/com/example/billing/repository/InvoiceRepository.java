package com.example.billing.repository;

import com.example.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    long countByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);
}