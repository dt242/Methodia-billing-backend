package com.example.billing.repository;

import com.example.billing.model.Invoice;
import com.example.billing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    long countByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);
    List<Invoice> findByUserOrderByDateTimeDesc(User user);
    List<Invoice> findByDateTimeBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT i FROM Invoice i WHERE " +
            "(:invoiceNumber IS NULL OR LOWER(i.number) LIKE LOWER(CONCAT('%', :invoiceNumber, '%'))) AND " +
            "(:customerName IS NULL OR LOWER(i.user.name) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(cast(:startDate as timestamp) IS NULL OR i.dateTime >= :startDate) AND " +
            "(cast(:endDate as timestamp) IS NULL OR i.dateTime <= :endDate) AND " +
            "(:isPaid IS NULL OR i.paid = :isPaid)")
    Page<Invoice> findWithFilters(@Param("invoiceNumber") String invoiceNumber,
                                  @Param("customerName") String customerName,
                                  @Param("startDate") OffsetDateTime startDate,
                                  @Param("endDate") OffsetDateTime endDate,
                                  @Param("isPaid") Boolean isPaid,
                                  Pageable pageable);
}