package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "billing_runs")
@Getter
@Setter
@NoArgsConstructor
public class BillingRun {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Column(name = "billing_month", nullable = false)
    private int billingMonth;

    @Column(name = "billing_year", nullable = false)
    private int billingYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    public BillingRun(int billingMonth, int billingYear) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.billingMonth = billingMonth;
        this.billingYear = billingYear;
        this.status = BillingStatus.IN_PROGRESS;
        this.startTime = OffsetDateTime.now();
    }
}