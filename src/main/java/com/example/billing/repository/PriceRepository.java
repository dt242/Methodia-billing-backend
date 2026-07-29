package com.example.billing.repository;

import com.example.billing.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PriceRepository extends JpaRepository<Price, String> {

    @Query("SELECT p FROM Price p WHERE " +
            "(:tariffCode IS NULL OR LOWER(p.tariffCode) LIKE LOWER(CONCAT('%', CAST(:tariffCode AS String), '%'))) AND " +
            "(cast(:startDate as timestamp) IS NULL OR p.startDate >= :startDate) AND " +
            "(cast(:endDate as timestamp) IS NULL OR p.endDate <= :endDate)")
    List<Price> findWithFilters(@Param("tariffCode") String tariffCode,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}