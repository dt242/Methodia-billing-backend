package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "error_logs")
@Getter
@Setter
@NoArgsConstructor
public class ErrorLog {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorSeverity severity;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "customer_id")
    private String customerId;

    @Column(nullable = false)
    private String process;

    public ErrorLog(ErrorSeverity severity, String description, String customerId, String process) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.timestamp = OffsetDateTime.now();
        this.severity = severity;
        this.description = description;
        this.customerId = customerId;
        this.process = process;
    }
}