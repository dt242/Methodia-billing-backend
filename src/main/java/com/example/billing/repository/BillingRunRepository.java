package com.example.billing.repository;
import com.example.billing.model.BillingRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRunRepository extends JpaRepository<BillingRun, String> {}