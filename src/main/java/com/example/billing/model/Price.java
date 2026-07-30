package com.example.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
public class Price {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Product product;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "tariff_code", nullable = false)
    private String tariffCode;

    @JsonIgnore
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "file_import_id")
     private FileImport fileImport;

    public Price(Product product, LocalDate startDate, LocalDate endDate, BigDecimal price, String tariffCode) {
        this.id = generateUuid();
        this.product = product;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.tariffCode = tariffCode;
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}