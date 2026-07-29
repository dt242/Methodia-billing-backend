package com.example.billing.dto;

public record ReportSummaryDto(
        String billingPeriod,
        long successfulRecords,
        long failedRecords,
        String status
) {}