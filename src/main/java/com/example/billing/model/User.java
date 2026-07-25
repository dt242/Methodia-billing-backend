package com.example.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "reference_number", nullable = false, unique = true)
    private String reference;

    @Column(name = "price_list")
    private int priceListId;

    public User(String name, String reference, int priceListId) {
        this.id = generateUuid();
        this.name = name;
        this.reference = reference;
        this.priceListId = priceListId;
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}