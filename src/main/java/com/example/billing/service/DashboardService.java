package com.example.billing.service;

import com.example.billing.dto.DashboardStatsDto;
import com.example.billing.model.BillingRun;
import com.example.billing.model.FileImport;
import com.example.billing.model.Role;
import com.example.billing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final BillingRunRepository billingRunRepository;
    private final InvoiceRepository invoiceRepository;
    private final ErrorLogRepository errorLogRepository;
    private final FileImportRepository fileImportRepository;

    public DashboardService(UserRepository userRepository, BillingRunRepository billingRunRepository,
                            InvoiceRepository invoiceRepository, ErrorLogRepository errorLogRepository,
                            FileImportRepository fileImportRepository) {
        this.userRepository = userRepository;
        this.billingRunRepository = billingRunRepository;
        this.invoiceRepository = invoiceRepository;
        this.errorLogRepository = errorLogRepository;
        this.fileImportRepository = fileImportRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        long totalClients = userRepository.countByRole(Role.CLIENT);

        List<String> lastFiles = fileImportRepository.findTop3ByOrderByUploadedAtDesc()
                .stream().map(FileImport::getFilename).toList();

        List<String> recentErrors = errorLogRepository.findTop3ByOrderByTimestampDesc()
                .stream().map(e -> e.getId().substring(0, 4) + " - " + e.getDescription()).toList();

        BillingRun lastRun = billingRunRepository.findTopByOrderByStartTimeDesc().orElse(null);

        if (lastRun == null) {
            return new DashboardStatsDto(totalClients, "Not Started", "N/A", "N/A", 0, 0, lastFiles, recentErrors);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");
        String start = lastRun.getStartTime() != null ? lastRun.getStartTime().format(dtf) : "N/A";
        String end = lastRun.getEndTime() != null ? lastRun.getEndTime().format(dtf) : "N/A";

        OffsetDateTime calcEnd = lastRun.getEndTime() != null ? lastRun.getEndTime() : OffsetDateTime.now();
        long success = invoiceRepository.countByDateTimeBetween(lastRun.getStartTime(), calcEnd);
        long failed = errorLogRepository.countByTimestampBetween(lastRun.getStartTime(), calcEnd);

        return new DashboardStatsDto(
                totalClients,
                lastRun.getStatus().name(),
                start,
                end,
                success,
                failed,
                lastFiles,
                recentErrors
        );
    }
}