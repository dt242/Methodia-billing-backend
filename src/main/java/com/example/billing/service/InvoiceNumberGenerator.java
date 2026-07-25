package com.example.billing.service;

import com.example.billing.config.AppConstants;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InvoiceNumberGenerator {

    private final AtomicInteger currentNumber = new AtomicInteger(AppConstants.INVOICE_START_NUMBER);

    public String getNextNumber() {
        return String.valueOf(currentNumber.getAndIncrement());
    }
}