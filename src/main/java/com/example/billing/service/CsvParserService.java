package com.example.billing.service;

import com.example.billing.exception.InvalidDataException;
import com.example.billing.model.*;
import com.example.billing.repository.FileImportRepository;
import com.example.billing.repository.PriceRepository;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.InputStream;
import java.util.Set;

@Service
public class CsvParserService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;
    private final FileImportRepository fileImportRepository;
    private final PasswordEncoder passwordEncoder;

    public CsvParserService(UserRepository userRepository, ReadingRepository readingRepository,
                            PriceRepository priceRepository, FileImportRepository fileImportRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
        this.fileImportRepository = fileImportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void importUsers(MultipartFile file) {
        try {
            User uploadedBy = getAuthenticatedUser();
            FileImport fileImport = new FileImport(ImportType.USERS, file.getOriginalFilename(), uploadedBy, file.getBytes());
            fileImportRepository.save(fileImport);

            List<String[]> rows = parseFile(file);
            if (rows.isEmpty()) throw new InvalidDataException("Файлът е празен.");

            String[] headerLine = rows.get(0);
            validateHeaders(String.join(",", headerLine), "Customer Name", "Customer ID", "Tariff Code");

            List<String> errors = new ArrayList<>();
            List<User> users = new ArrayList<>();

            Set<String> seenReferences = new HashSet<>();

            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                int rowNumber = i + 1;

                if (parts.length < 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                    errors.add("Липсват задължителни данни или има празни полета на ред: " + rowNumber);
                    continue;
                }

                String customerName = parts[0];
                String reference = parts[1];
                String tariffCode = parts[2];

                if (!seenReferences.add(reference) || userRepository.findByReference(reference).isPresent()) {
                    errors.add("Дублиран Customer ID (" + reference + ") на ред: " + rowNumber);
                    continue;
                }

                try {
                    String encodedPassword = passwordEncoder.encode(reference);
                    users.add(new User(customerName, reference, tariffCode, encodedPassword, Role.CLIENT));
                } catch (Exception e) {
                    errors.add("Неочаквана грешка при обработка на данните на ред: " + rowNumber);
                }
            }

            if (!errors.isEmpty()) {
                throw new InvalidDataException(errors);
            }
            userRepository.saveAll(users);

            if (userRepository.findByReference("ADMIN-1").isEmpty()) {
                User admin = new User("System Admin", "ADMIN-1", "N/A",
                        passwordEncoder.encode("admin123"), Role.ADMIN);
                userRepository.save(admin);
            }
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Системна грешка при четене на файла с клиенти: " + e.getMessage());
        }
    }

    @Transactional
    public void importReadings(MultipartFile file) {
        try {
            User uploadedBy = getAuthenticatedUser();
            FileImport fileImport = new FileImport(ImportType.READINGS, file.getOriginalFilename(), uploadedBy, file.getBytes());
            fileImportRepository.save(fileImport);

            List<String[]> rows = parseFile(file);
            if (rows.isEmpty()) throw new InvalidDataException("Файлът е празен.");

            String[] headerLine = rows.get(0);
            validateHeaders(String.join(",", headerLine), "Customer ID", "Product", "DateTime", "Consumption");

            List<String> errors = new ArrayList<>();
            List<Reading> readings = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                int rowNumber = i + 1;

                if (parts.length < 4) {
                    errors.add("Липсват задължителни данни на ред: " + rowNumber);
                    continue;
                }

                String userRef = parts[0];
                var userOpt = userRepository.findByReference(userRef);

                if (userOpt.isEmpty()) {
                    errors.add("Ненамерен клиент с ID: " + userRef + " на ред: " + rowNumber);
                    continue;
                }

                try {
                    readings.add(new Reading(
                            userOpt.get(),
                            Product.valueOf(parts[1].toUpperCase()),
                            OffsetDateTime.parse(parts[2]),
                            new BigDecimal(parts[3]),
                            false,
                            ReadingStatus.VALIDATED
                    ));
                } catch (Exception e) {
                    errors.add("Невалиден формат на данните на ред: " + rowNumber);
                }
            }

            if (!errors.isEmpty()) {
                throw new InvalidDataException(errors);
            }

            readingRepository.saveAll(readings);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Системна грешка при четене на отчетите: " + e.getMessage());
        }
    }

    @Transactional
    public void importPrices(MultipartFile file) {
        try {
            User uploadedBy = getAuthenticatedUser();
            FileImport fileImport = new FileImport(ImportType.PRICES, file.getOriginalFilename(), uploadedBy, file.getBytes());
            fileImport = fileImportRepository.save(fileImport);

            List<String[]> rows = parseFile(file);
            if (rows.isEmpty()) throw new InvalidDataException("Файлът е празен.");

            String[] headerLine = rows.get(0);
            validateHeaders(String.join(",", headerLine), "Tariff Code", "Price", "Valid From", "Valid To", "Product");

            List<String> errors = new ArrayList<>();
            List<Price> prices = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                int rowNumber = i + 1;

                if (parts.length < 5) {
                    errors.add("Липсват данни на ред: " + rowNumber);
                    continue;
                }

                try {
                    Price price = new Price(
                            Product.valueOf(parts[4].toUpperCase()),
                            LocalDate.parse(parts[2]),
                            LocalDate.parse(parts[3]),
                            new BigDecimal(parts[1]),
                            parts[0]
                    );
                    price.setFileImport(fileImport);
                    prices.add(price);
                } catch (Exception e) {
                    errors.add("Невалиден формат на цената или датите на ред: " + rowNumber);
                }
            }

            errors.addAll(validateOverlappingPrices(prices));

            if (!errors.isEmpty()) {
                throw new InvalidDataException(errors);
            }

            priceRepository.saveAll(prices);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Системна грешка при четене на цените: " + e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByReference(username).orElse(null);
    }

    private void validateHeaders(String headerLine, String... expectedHeaders) {
        if (headerLine == null || headerLine.trim().isEmpty()) {
            throw new InvalidDataException("Файлът е празен или липсва заглавен ред (header).");
        }

        String upperHeader = headerLine.toUpperCase();
        List<String> missing = new ArrayList<>();

        for (String expected : expectedHeaders) {
            if (!upperHeader.replace(" ", "").contains(expected.toUpperCase().replace(" ", ""))) {
                missing.add(expected);
            }
        }

        if (!missing.isEmpty()) {
            throw new InvalidDataException("Невалидна структура. Липсват задължителни колони: " + String.join(", ", missing));
        }
    }

    private List<String[]> parseFile(MultipartFile file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        String filename = file.getOriginalFilename();

        if (filename.toLowerCase().endsWith(".xlsx")) {
            try (InputStream is = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter dataFormatter = new DataFormatter();

                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    int lastCellNum = Math.max(row.getLastCellNum(), 0);
                    for (int cn = 0; cn < lastCellNum; cn++) {
                        Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        rowData.add(dataFormatter.formatCellValue(cell).trim());
                    }
                    rows.add(rowData.toArray(new String[0]));
                }
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    for (int i = 0; i < parts.length; i++) {
                        parts[i] = parts[i].trim();
                    }
                    rows.add(parts);
                }
            }
        }
        return rows;
    }

    private List<String> validateOverlappingPrices(List<Price> newPrices) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < newPrices.size(); i++) {
            for (int j = i + 1; j < newPrices.size(); j++) {
                Price p1 = newPrices.get(i);
                Price p2 = newPrices.get(j);

                if (p1.getTariffCode().equals(p2.getTariffCode()) && p1.getProduct() == p2.getProduct()) {
                    if (!p1.getStartDate().isAfter(p2.getEndDate()) && !p1.getEndDate().isBefore(p2.getStartDate())) {
                        errors.add("Overlapping Validity Period: Конфликт вътре във файла за тарифа " + p1.getTariffCode());
                    }
                }
            }
        }

        List<Price> existingPrices = priceRepository.findAll();

        for (Price newPrice : newPrices) {
            for (Price existingPrice : existingPrices) {
                if (newPrice.getTariffCode().equals(existingPrice.getTariffCode()) && newPrice.getProduct() == existingPrice.getProduct()) {
                    if (!newPrice.getStartDate().isAfter(existingPrice.getEndDate()) && !newPrice.getEndDate().isBefore(existingPrice.getStartDate())) {
                        errors.add("Overlapping Validity Period: Конфликт със съществуваща тарифа в базата за " + newPrice.getTariffCode());
                    }
                }
            }
        }
        return errors;
    }
}