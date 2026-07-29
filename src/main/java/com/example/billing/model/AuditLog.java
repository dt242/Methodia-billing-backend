package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false, length = 1000)
    private String action;

    public AuditLog(String username, String module, String action) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.timestamp = OffsetDateTime.now();
        this.username = username;
        this.module = module;
        this.action = action;
    }
}