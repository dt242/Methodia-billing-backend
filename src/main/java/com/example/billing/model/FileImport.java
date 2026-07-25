package com.example.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_imports")
@Getter
@Setter
@NoArgsConstructor
public class FileImport {

    @Id
    @Column(length = 32, nullable = false, unique = true)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportType type;

    @Column(nullable = false)
    private String filename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @Lob
    @Column(nullable = false)
    private byte[] file;

    public FileImport(ImportType type, String filename, User uploadedBy, byte[] file) {
        this.id = generateUuid();
        this.type = type;
        this.filename = filename;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = OffsetDateTime.now();
        this.file = file;
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}