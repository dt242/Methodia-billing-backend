package com.example.billing.repository;

import com.example.billing.model.Invoice;
import com.example.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    long countByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);
    List<Invoice> findByUserOrderByDateTimeDesc(User user);
    List<Invoice> findByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);
}