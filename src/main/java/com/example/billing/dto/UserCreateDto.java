package com.example.billing.dto;

import com.example.billing.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateDto(
        @NotBlank(message = "Името е задължително")
        String name,
        @NotBlank(message = "Референцията (ID) е задължителна")
        String reference,
        String tariffCode,
        @NotBlank(message = "Паролата е задължителна")
        String password,
        @NotNull(message = "Ролята е задължителна")
        Role role
) {}