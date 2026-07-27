package com.example.billing.service;

import com.example.billing.exception.InvalidDataException;
import com.example.billing.model.Price;
import com.example.billing.model.Product;
import com.example.billing.model.Reading;
import com.example.billing.model.Role;
import com.example.billing.model.User;
import com.example.billing.repository.PriceRepository;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvParserService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;
    private final PasswordEncoder passwordEncoder;

    public CsvParserService(UserRepository userRepository, ReadingRepository readingRepository,
                            PriceRepository priceRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void importUsers(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            validateHeaders(headerLine, "Customer Name", "Customer ID", "Tariff Code");

            List<User> users = reader.lines().map(line -> {
                String[] parts = line.split(",");
                if (parts.length < 3) throw new InvalidDataException("Липсват данни на ред: " + line);

                String reference = parts[1].trim();
                String encodedPassword = passwordEncoder.encode(reference);

                return new User(
                        parts[0].trim(),
                        reference,
                        Integer.parseInt(parts[2].trim()),
                        encodedPassword,
                        Role.CLIENT
                );
            }).toList();
            userRepository.saveAll(users);

            if (userRepository.findByReference("ADMIN-1").isEmpty()) {
                User admin = new User("System Admin", "ADMIN-1", 0,
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            validateHeaders(headerLine, "Customer ID", "Product", "DateTime", "Consumption");

            List<Reading> readings = reader.lines().map(line -> {
                String[] parts = line.split(",");
                if (parts.length < 4) throw new InvalidDataException("Липсват данни на ред: " + line);

                String userRef = parts[0].trim();
                User user = userRepository.findByReference(userRef)
                        .orElseThrow(() -> new InvalidDataException("Ненамерен клиент с ID: " + userRef));

                return new Reading(
                        user,
                        Product.valueOf(parts[1].trim().toUpperCase()),
                        OffsetDateTime.parse(parts[2].trim()),
                        new BigDecimal(parts[3].trim()),
                        false
                );
            }).toList();
            readingRepository.saveAll(readings);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Грешка при четене на отчетите: " + e.getMessage());
        }
    }

    @Transactional
    public void importPrices(MultipartFile file, int priceListId) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            validateHeaders(headerLine, "Product", "Valid From", "Valid To", "Price");

            List<Price> prices = reader.lines().map(line -> {
                String[] parts = line.split(",");
                if (parts.length < 4) throw new InvalidDataException("Липсват данни на ред: " + line);

                return new Price(
                        Product.valueOf(parts[0].trim().toUpperCase()),
                        LocalDate.parse(parts[1].trim()),
                        LocalDate.parse(parts[2].trim()),
                        new BigDecimal(parts[3].trim()),
                        priceListId
                );
            }).toList();
            priceRepository.saveAll(prices);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Грешка при четене на цените: " + e.getMessage());
        }
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
}