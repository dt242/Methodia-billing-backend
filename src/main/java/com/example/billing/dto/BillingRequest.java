package com.example.billing.dto;

import com.example.billing.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BillingRequest(
        @NotBlank(message = "Референтният номер е задължителен")
        String reference,

        @NotNull(message = "Продуктът е задължителен")
        Product product
) {}