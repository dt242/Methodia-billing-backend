package com.example.billing.repository;

import com.example.billing.model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, String> {

    @Modifying
    @Transactional
    void deleteByTimestampBefore(OffsetDateTime cutoffDate);
}