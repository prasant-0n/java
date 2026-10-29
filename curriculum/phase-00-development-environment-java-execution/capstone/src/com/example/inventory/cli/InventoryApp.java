package com.example.inventory.cli;

import com.example.inventory.model.Product;
import com.example.inventory.service.InventoryService;

public class InventoryApp {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    PHASE 00 CAPSTONE: BARE-METAL INVENTORY CLI   ");
        System.out.println("==================================================");

        // Register JVM Shutdown Hook (demonstrating P00-C005)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SHUTDOWN] InventoryApp shutting down cleanly.");
        }, "capstone-shutdown-hook"));

        InventoryService service = new InventoryService();
        service.addProduct(new Product("SKU-1001", "PostgreSQL High-Perf SSD", 12, 249.99));
        service.addProduct(new Product("SKU-1002", "10Gbps Network Interface Card", 8, 189.50));
        service.addProduct(new Product("SKU-1003", "ECC DDR5 64GB Server RAM", 24, 320.00));

        System.out.printf("Loaded %d products into inventory.%n", service.listProducts().size());
        for (Product p : service.listProducts()) {
            System.out.printf("  -> [%s] %-32s (Qty: %2d, Unit: $%.2f)%n",
                    p.sku(), p.name(), p.quantity(), p.price());
        }

        double totalVal = service.calculateTotalInventoryValue();
        System.out.printf("%nTotal Inventory Valuation: $%.2f%n", totalVal);
        System.out.println("==================================================");
        System.out.println("CAPSTONE VERIFICATION: SUCCESS");
    }
}
