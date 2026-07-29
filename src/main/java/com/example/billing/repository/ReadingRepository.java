package com.example.billing.repository;

import com.example.billing.model.Product;
import com.example.billing.model.Reading;
import com.example.billing.model.ReadingStatus;
import com.example.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingRepository extends JpaRepository<Reading, String> {
    List<Reading> findByStatusOrderByDateTimeAsc(ReadingStatus status);
    List<Reading> findByUserAndProductAndStatusOrderByDateTimeAsc(User user, Product product, ReadingStatus status);
    List<Reading> findByUserOrderByDateTimeDesc(User user);
}