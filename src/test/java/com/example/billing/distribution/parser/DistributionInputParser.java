package com.example.billing.distribution.parser;

import com.example.billing.model.Price;
import com.example.billing.model.Product;
import com.example.billing.model.Reading;
import com.example.billing.model.User;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DistributionInputParser {

    public record ParsedInput(List<ReadingPair> readingPairs, List<Price> prices) {}

    public record ReadingPair(Reading start, Reading end) {}

    public static ParsedInput parse(InputStream inputStream) throws Exception {
        List<ReadingPair> pairs = new ArrayList<>();
        List<Price> prices = new ArrayList<>();

        User dummyUser = new User("Test User", "TEST-REF", 1);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts[0].equals("Q")) {
                    OffsetDateTime startDt = OffsetDateTime.parse(parts[1]);
                    OffsetDateTime endDt = OffsetDateTime.parse(parts[2]);
                    BigDecimal quantity = new BigDecimal(parts[3]);

                    Reading start = new Reading(dummyUser, Product.ELEC, startDt, BigDecimal.ZERO, false);
                    Reading end = new Reading(dummyUser, Product.ELEC, endDt, quantity, false);

                    pairs.add(new ReadingPair(start, end));

                } else if (parts[0].equals("P")) {
                    LocalDate startDt = LocalDate.parse(parts[1]);
                    LocalDate endDt = LocalDate.parse(parts[2]);
                    BigDecimal priceVal = new BigDecimal(parts[3]);

                    Price p = new Price(Product.ELEC, startDt, endDt, priceVal, 1);
                    prices.add(p);
                }
            }
        }
        return new ParsedInput(pairs, prices);
    }
}