package com.example.billing.config;

import java.util.Locale;

public final class AppConstants {

    private AppConstants() {}

    public static final String OUTPUT_DIRECTORY = "output";
    public static final int INVOICE_START_NUMBER = 10000;
    public static final Locale BG_LOCALE = Locale.forLanguageTag("bg-BG");
    public static final String FILE_DATE_PATTERN = "LLLL-yy";
    public static final int QUANTITY_SCALE = 3;
    public static final int AMOUNT_SCALE = 2;
}