package com.example.billing.repository;

import com.example.billing.model.FileImport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileImportRepository extends JpaRepository<FileImport, String> {
}