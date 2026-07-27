package com.example.billing.controller;

import com.example.billing.exception.InvalidDataException;
import com.example.billing.service.CsvParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
@CrossOrigin(origins = "*")
public class ImportController {

    private final CsvParserService csvParserService;

    public ImportController(CsvParserService csvParserService) {
        this.csvParserService = csvParserService;
    }

    @PostMapping("/users")
    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new InvalidDataException("Файлът не може да бъде празен.");
        csvParserService.importUsers(file);
        return ResponseEntity.ok("Клиентите са успешно импортирани.");
    }

    @PostMapping("/readings")
    public ResponseEntity<String> importReadings(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new InvalidDataException("Файлът не може да бъде празен.");
        csvParserService.importReadings(file);
        return ResponseEntity.ok("Отчетите са успешно импортирани.");
    }

    @PostMapping("/prices")
    public ResponseEntity<String> importPrices(
            @RequestParam("file") MultipartFile file,
            @RequestParam("priceListId") int priceListId) {
        if (file.isEmpty()) throw new InvalidDataException("Файлът не може да бъде празен.");
        csvParserService.importPrices(file, priceListId);
        return ResponseEntity.ok("Тарифите са успешно импортирани.");
    }
}