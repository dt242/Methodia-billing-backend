package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lines")
@Getter
@Setter
@NoArgsConstructor
public class Line {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Column(name = "line_id", nullable = false)
    private int lineId;

    @Column(precision = 19, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "start_date_time", nullable = false)
    private OffsetDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private OffsetDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Product product;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "price_list", nullable = false)
    private int priceList;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}