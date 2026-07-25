package com.example.billing.repository;

import com.example.billing.model.Price;
import com.example.billing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRepository extends JpaRepository<Price, String> {
    List<Price> findByProductAndPriceListOrderByStartDateAsc(Product product, int priceList);
}