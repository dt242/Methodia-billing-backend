package com.example.billing.repository;

import com.example.billing.model.FileImport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileImportRepository extends JpaRepository<FileImport, String> {
    List<FileImport> findTop3ByOrderByUploadedAtDesc();
}