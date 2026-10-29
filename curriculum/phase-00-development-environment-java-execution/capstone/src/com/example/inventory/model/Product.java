package com.example.inventory.model;

public record Product(String sku, String name, int quantity, double price) {
    public Product {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU cannot be blank");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (price < 0.0) throw new IllegalArgumentException("Price cannot be negative");
    }
}
