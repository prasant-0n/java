package com.example.banking.model;

import java.util.Objects;

public record AccountId(String value) {
    public AccountId {
        Objects.requireNonNull(value, "AccountId cannot be null");
        value = value.trim().toUpperCase();
        if (!value.matches("^ACC-[A-Z0-9]{4,12}$")) {
            throw new IllegalArgumentException("Invalid AccountId format (must match ACC-XXXX): " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
