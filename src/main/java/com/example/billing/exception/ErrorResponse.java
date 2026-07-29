package com.example.billing.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        Object message,
        Instant timestamp
) {}