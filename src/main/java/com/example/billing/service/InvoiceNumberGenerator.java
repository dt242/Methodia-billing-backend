package com.example.billing.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InvoiceNumberGenerator {

    private static final int INVOICE_START_NUMBER = 10000;
    private final AtomicInteger currentNumber = new AtomicInteger(INVOICE_START_NUMBER);

    public String getNextNumber() {
        return String.valueOf(currentNumber.getAndIncrement());
    }
}