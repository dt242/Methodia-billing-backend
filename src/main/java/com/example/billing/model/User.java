package com.example.billing.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public User(String name, String reference, int priceListId, String password, Role role) {
        this.id = generateUuid();
        this.name = name;
        this.reference = reference;
        this.priceListId = priceListId;
        this.password = password;
        this.role = role;
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}