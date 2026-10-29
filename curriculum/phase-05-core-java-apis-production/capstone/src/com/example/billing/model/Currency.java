package com.example.billing.model;

/**
 * Supported Billing Currencies and their official minor-unit decimal scales.
 */
public enum Currency {
    USD(2, "$"),
    EUR(2, "€"),
    GBP(2, "£"),
    JPY(0, "¥"),
    INR(2, "₹");

    private final int defaultScale;
    private final String symbol;

    Currency(int defaultScale, String symbol) {
        this.defaultScale = defaultScale;
        this.symbol = symbol;
    }

    public int defaultScale() {
        return defaultScale;
    }

    public String symbol() {
        return symbol;
    }
}
