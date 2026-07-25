package com.example.billing.distribution.service;

import com.example.billing.model.Line;
import com.example.billing.model.Price;
import com.example.billing.model.Reading;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProportionalDistributionService {

    public List<Line> distribute(Reading startReading, Reading endReading, List<Price> prices) {
        List<Line> result = new ArrayList<>();

        OffsetDateTime qStart = startReading.getDateTime();
        OffsetDateTime qEnd = endReading.getDateTime();
        BigDecimal totalQuantity = endReading.getLastReading().subtract(startReading.getLastReading());

        LocalDate qStartDate = qStart.toLocalDate();
        LocalDate qEndDate = qEnd.toLocalDate();
        long totalDays = ChronoUnit.DAYS.between(qStartDate, qEndDate) + 1;

        if (totalDays <= 0) return result;

        prices.sort(Comparator.comparing(Price::getStartDate));

        List<PeriodData> periods = new ArrayList<>();

        for (Price price : prices) {
            LocalDate pStartDate = price.getStartDate();
            LocalDate pEndDate = price.getEndDate();

            if (pEndDate.isBefore(qStartDate) || pStartDate.isAfter(qEndDate)) {
                continue;
            }

            LocalDate actualStartDate = pStartDate.isAfter(qStartDate) ? pStartDate : qStartDate;
            LocalDate actualEndDate = pEndDate.isBefore(qEndDate) ? pEndDate : qEndDate;

            long overlapDays = ChronoUnit.DAYS.between(actualStartDate, actualEndDate) + 1;

            ZonedDateTime pStartZ = pStartDate.atStartOfDay(ZoneId.of("Europe/Sofia"));
            ZonedDateTime pEndZ = pEndDate.plusDays(1).atStartOfDay(ZoneId.of("Europe/Sofia"));

            OffsetDateTime displayStart = qStart.isAfter(pStartZ.toOffsetDateTime()) ? qStart : pStartZ.toOffsetDateTime();
            OffsetDateTime mathEnd = qEnd.isBefore(pEndZ.toOffsetDateTime()) ? qEnd : pEndZ.toOffsetDateTime();

            OffsetDateTime displayEnd = mathEnd;
            if (mathEnd.equals(pEndZ.toOffsetDateTime()) && mathEnd.isBefore(qEnd)) {
                displayEnd = mathEnd.minusSeconds(1);
            }

            periods.add(new PeriodData(displayStart, displayEnd, overlapDays, price));
        }

        BigDecimal distributedQuantity = BigDecimal.ZERO;

        for (int i = 0; i < periods.size(); i++) {
            PeriodData p = periods.get(i);
            Line line = new Line();
            line.setStartDateTime(p.displayStart);
            line.setEndDateTime(p.displayEnd);
            line.setProduct(startReading.getProduct());
            line.setPrice(p.price.getPrice());
            line.setPriceList(p.price.getPriceList());

            BigDecimal currentQuantity;

            if (i == periods.size() - 1) {
                currentQuantity = totalQuantity.subtract(distributedQuantity);
            } else {
                BigDecimal ratio = BigDecimal.valueOf(p.overlapDays)
                        .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.UP);
                currentQuantity = totalQuantity.multiply(ratio).setScale(2, RoundingMode.UP);
                distributedQuantity = distributedQuantity.add(currentQuantity);
            }

            line.setQuantity(currentQuantity);
            line.setAmount(currentQuantity.multiply(p.price.getPrice()).setScale(2, RoundingMode.HALF_UP));
            result.add(line);
        }

        return result;
    }

    private record PeriodData(OffsetDateTime displayStart, OffsetDateTime displayEnd, long overlapDays, Price price) {}
}