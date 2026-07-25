package com.example.billing.service;

import com.example.billing.model.Price;
import com.example.billing.model.Product;
import com.example.billing.model.Reading;
import com.example.billing.model.User;
import com.example.billing.repository.PriceRepository;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CsvParserService {

    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final PriceRepository priceRepository;

    public CsvParserService(UserRepository userRepository, ReadingRepository readingRepository, PriceRepository priceRepository) {
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
        this.priceRepository = priceRepository;
    }

    @Transactional
    public void importUsers() {
        try (BufferedReader reader = getReader("data/users.csv")) {
            List<User> users = reader.lines().map(line -> {
                String[] parts = line.split(",");
                return new User(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()));
            }).toList();
            userRepository.saveAll(users);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import users", e);
        }
    }

    @Transactional
    public void importReadings() {
        try (BufferedReader reader = getReader("data/readings.csv")) {
            List<Reading> readings = reader.lines().map(line -> {
                String[] parts = line.split(",");
                String userRef = parts[0].trim();

                User user = userRepository.findByReference(userRef)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userRef));

                return new Reading(
                        user,
                        Product.valueOf(parts[1].trim().toUpperCase()),
                        OffsetDateTime.parse(parts[2].trim()),
                        new BigDecimal(parts[3].trim()),
                        false
                );
            }).toList();
            readingRepository.saveAll(readings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import readings", e);
        }
    }

    @Transactional
    public void importPrices(int priceListId) {
        try (BufferedReader reader = getReader("data/prices-" + priceListId + ".csv")) {
            List<Price> prices = reader.lines().map(line -> {
                String[] parts = line.split(",");
                return new Price(
                        Product.valueOf(parts[0].trim().toUpperCase()),
                        LocalDate.parse(parts[1].trim()),
                        LocalDate.parse(parts[2].trim()),
                        new BigDecimal(parts[3].trim()),
                        priceListId
                );
            }).toList();
            priceRepository.saveAll(prices);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import prices", e);
        }
    }

    private BufferedReader getReader(String filePath) throws Exception {
        return new BufferedReader(new InputStreamReader(
                new ClassPathResource(filePath).getInputStream(), StandardCharsets.UTF_8));
    }
}