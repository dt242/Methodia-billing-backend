package com.example.billing.service;

import com.example.billing.repository.ErrorLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class LogCleanupService {

    private final ErrorLogRepository errorLogRepository;
    private final AuditService auditService;

    public LogCleanupService(ErrorLogRepository errorLogRepository, AuditService auditService) {
        this.errorLogRepository = errorLogRepository;
        this.auditService = auditService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldErrorLogs() {
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(30);

        try {
            errorLogRepository.deleteByTimestampBefore(cutoffDate);
            auditService.logAction("System", "Deleted error logs older than 30 days");
        } catch (Exception e) {
            auditService.logAction("System", "Failed to delete old error logs: " + e.getMessage());
        }
    }
}