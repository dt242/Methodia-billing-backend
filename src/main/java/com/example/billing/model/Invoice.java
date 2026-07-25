package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Column(name = "date_time", nullable = false)
    private OffsetDateTime dateTime;

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Line> lines = new ArrayList<>();

    @Column(nullable = false)
    private boolean paid = false;

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void addLine(Line line) {
        lines.add(line);
        line.setInvoice(this);
    }
}