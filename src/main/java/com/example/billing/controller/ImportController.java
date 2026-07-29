package com.example.billing.controller;

import com.example.billing.exception.InvalidDataException;
import com.example.billing.service.AuditService;
import com.example.billing.service.CsvParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
@CrossOrigin(origins = "*")
public class ImportController {

    private final CsvParserService csvParserService;
    private final AuditService auditService;

    public ImportController(CsvParserService csvParserService, AuditService auditService) {
        this.csvParserService = csvParserService;
        this.auditService = auditService;
    }

    @PostMapping("/users")
    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new InvalidDataException("Файлът не може да бъде празен.");
        csvParserService.importUsers(file);
        auditService.logAction("Customers", "Imported users file: " + file.getOriginalFilename());
        return ResponseEntity.ok("Клиентите са успешно импортирани.");
    }

    @PostMapping("/readings")
    public ResponseEntity<String> importReadings(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new InvalidDataException("Файлът не може да бъде празен.");
        csvParserService.importReadings(file);
        auditService.logAction("Usage Data", "Imported readings file: " + file.getOriginalFilename());
        return ResponseEntity.ok("Отчетите са успешно импортирани.");
    }

    @PostMapping("/prices")
    public ResponseEntity<String> importPrices(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new InvalidDataException("Файлът не може да бъде празен.");
        csvParserService.importPrices(file);
        auditService.logAction("Tariff Plans", "Imported tariff prices file: " + file.getOriginalFilename());
        return ResponseEntity.ok("Тарифите са успешно импортирани.");
    }
}