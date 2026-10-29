package com.example.banking.model;

import java.util.Objects;

public record Customer(String customerId, String fullName, String email, CustomerTier tier) {
    public Customer {
        Objects.requireNonNull(customerId, "customerId required");
        Objects.requireNonNull(fullName, "fullName required");
        Objects.requireNonNull(email, "email required");
        Objects.requireNonNull(tier, "tier required");
    }
}
