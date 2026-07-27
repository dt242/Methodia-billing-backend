package com.example.billing.dto;

import com.example.billing.model.Product;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SelfReportRequest(
        @NotNull(message = "Продуктът е задължителен")
        Product product,

        @NotNull(message = "Датата и часът са задължителни")
        OffsetDateTime dateTime,

        @NotNull(message = "Показанието е задължително")
        BigDecimal lastReading
) {}