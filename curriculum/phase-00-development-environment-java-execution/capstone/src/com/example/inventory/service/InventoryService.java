package com.example.inventory.service;

import com.example.inventory.model.Product;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InventoryService {
    private final List<Product> stock = new ArrayList<>();

    public void addProduct(Product product) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        stock.add(product);
    }

    public List<Product> listProducts() {
        return Collections.unmodifiableList(stock);
    }

    public Optional<Product> findBySku(String sku) {
        return stock.stream()
                .filter(p -> p.sku().equalsIgnoreCase(sku))
                .findFirst();
    }

    public double calculateTotalInventoryValue() {
        return stock.stream()
                .mapToDouble(p -> p.quantity() * p.price())
                .sum();
    }
}
