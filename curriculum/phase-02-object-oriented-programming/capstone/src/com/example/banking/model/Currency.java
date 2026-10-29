package com.example.banking.model;

import java.util.Objects;

public record Currency(String code, String symbol, int decimalPlaces) {
    public static final Currency USD = new Currency("USD", "$", 2);
    public static final Currency EUR = new Currency("EUR", "€", 2);
    public static final Currency GBP = new Currency("GBP", "£", 2);

    public Currency {
        Objects.requireNonNull(code, "code required");
        Objects.requireNonNull(symbol, "symbol required");
        if (code.length() != 3) throw new IllegalArgumentException("Currency code must be 3 letters");
        if (decimalPlaces < 0 || decimalPlaces > 4) throw new IllegalArgumentException("Invalid decimal places");
    }
}
