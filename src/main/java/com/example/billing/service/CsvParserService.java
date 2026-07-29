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
import java.util.List;
import java.io.InputStream;

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

            List<User> users = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                if (parts.length < 3) throw new InvalidDataException("Липсват данни на ред: " + (i + 1));

                String reference = parts[1];
                String encodedPassword = passwordEncoder.encode(reference);
                String tariffCode = parts[2];

                users.add(new User(parts[0], reference, tariffCode, encodedPassword, Role.CLIENT));
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
            throw new InvalidDataException("Грешка при четене на файла с клиенти: " + e.getMessage());
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

            List<Reading> readings = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                if (parts.length < 4) throw new InvalidDataException("Липсват данни на ред: " + (i + 1));
                String userRef = parts[0];
                int rowNumber = i + 1;
                User user = userRepository.findByReference(userRef)
                        .orElseThrow(() -> new InvalidDataException("Ненамерен клиент с ID: " + userRef + " на ред: " + rowNumber));

                readings.add(new Reading(
                        user,
                        Product.valueOf(parts[1].toUpperCase()),
                        OffsetDateTime.parse(parts[2]),
                        new BigDecimal(parts[3]),
                        false,
                        ReadingStatus.VALIDATED
                ));
            }
            readingRepository.saveAll(readings);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Грешка при четене на отчетите: " + e.getMessage());
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

            List<Price> prices = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                if (parts.length < 5) throw new InvalidDataException("Липсват данни на ред: " + (i + 1));

                Price price = new Price(
                        Product.valueOf(parts[4].toUpperCase()),
                        LocalDate.parse(parts[2]),
                        LocalDate.parse(parts[3]),
                        new BigDecimal(parts[1]),
                        parts[0]
                );
                price.setFileImport(fileImport);
                prices.add(price);
            }
            priceRepository.saveAll(prices);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Грешка при четене на цените: " + e.getMessage());
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
}