package com.example.billing.repository;

import com.example.billing.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', CAST(:username AS String), '%'))) AND " +
            "(:module IS NULL OR LOWER(a.module) LIKE LOWER(CONCAT('%', CAST(:module AS String), '%'))) AND " +
            "(cast(:startDate as timestamp) IS NULL OR a.timestamp >= :startDate) AND " +
            "(cast(:endDate as timestamp) IS NULL OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findWithFilters(@Param("username") String username,
                                   @Param("module") String module,
                                   @Param("startDate") OffsetDateTime startDate,
                                   @Param("endDate") OffsetDateTime endDate);
}