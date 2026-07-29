package com.example.billing.dto;

import java.util.List;

public record DashboardStatsDto(
        long totalClients,
        String lastBillingRunStatus,
        String lastBillingRunStart,
        String lastBillingRunEnd,
        long lastRunSuccess,
        long lastRunFailed,
        List<String> lastUploadedFiles,
        List<String> recentErrors
) {}