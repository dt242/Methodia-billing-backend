package com.example.billing.repository;
import com.example.billing.model.BillingRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRunRepository extends JpaRepository<BillingRun, String> {
    Optional<BillingRun> findByBillingMonthAndBillingYear(int billingMonth, int billingYear);
    Optional<BillingRun> findTopByOrderByStartTimeDesc();
}