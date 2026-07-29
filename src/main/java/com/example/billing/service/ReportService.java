package com.example.billing.service;

import com.example.billing.dto.ReportSummaryDto;
import com.example.billing.model.BillingRun;
import com.example.billing.model.ErrorLog;
import com.example.billing.repository.BillingRunRepository;
import com.example.billing.repository.ErrorLogRepository;
import com.example.billing.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReportService {
    private final BillingRunRepository billingRunRepository;
    private final InvoiceRepository invoiceRepository;
    private final ErrorLogRepository errorLogRepository;

    public ReportService(BillingRunRepository billingRunRepository, InvoiceRepository invoiceRepository, ErrorLogRepository errorLogRepository) {
        this.billingRunRepository = billingRunRepository;
        this.invoiceRepository = invoiceRepository;
        this.errorLogRepository = errorLogRepository;
    }

    public ReportSummaryDto getSummary(int month, int year) {
        BillingRun run = billingRunRepository.findByBillingMonthAndBillingYear(month, year)
                .orElseThrow(() -> new RuntimeException("Няма данни за Billing Run за този период."));

        OffsetDateTime end = run.getEndTime() != null ? run.getEndTime() : OffsetDateTime.now();

        long successCount = invoiceRepository.countByDateTimeBetween(run.getStartTime(), end);
        long failCount = errorLogRepository.countByTimestampBetween(run.getStartTime(), end);

        String period = String.format("%02d.%d", month, year);
        return new ReportSummaryDto(period, successCount, failCount, run.getStatus().name());
    }

    public List<ErrorLog> getFailedRecords(int month, int year) {
        BillingRun run = billingRunRepository.findByBillingMonthAndBillingYear(month, year)
                .orElseThrow(() -> new RuntimeException("Няма данни за Billing Run за този период."));

        OffsetDateTime end = run.getEndTime() != null ? run.getEndTime() : OffsetDateTime.now();
        return errorLogRepository.findByTimestampBetween(run.getStartTime(), end);
    }
}