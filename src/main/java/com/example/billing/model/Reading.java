package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "readings")
@Getter
@Setter
@NoArgsConstructor
public class Reading {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Product product;

    @Column(name = "date_time", nullable = false)
    private OffsetDateTime dateTime;

    @Column(name = "last_reading", precision = 19, scale = 3, nullable = false)
    private BigDecimal lastReading;

    @Column(nullable = false)
    private boolean invoiced = false;

    @Column(name = "self_reported", nullable = false)
    private boolean selfReported;

    public Reading(User user, Product product, OffsetDateTime dateTime, BigDecimal lastReading, boolean selfReported) {
        this.id = generateUuid();
        this.user = user;
        this.product = product;
        this.dateTime = dateTime;
        this.lastReading = lastReading;
        this.selfReported = selfReported;
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}