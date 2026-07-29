package com.example.billing.dto;

import com.example.billing.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateDto(
        @NotBlank(message = "Името е задължително")
        String name,
        String tariffCode,
        @NotNull(message = "Ролята е задължителна")
        Role role
) {}