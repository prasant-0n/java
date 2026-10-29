package com.example.billing.model;

/**
 * Supported international currency codes and formatting.
 */
public enum Currency {
    USD("$", 2),
    EUR("€", 2),
    GBP("£", 2);

    private final String symbol;
    private final int fractionDigits;

    Currency(String symbol, int fractionDigits) {
        this.symbol = symbol;
        this.fractionDigits = fractionDigits;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getFractionDigits() {
        return fractionDigits;
    }
}
