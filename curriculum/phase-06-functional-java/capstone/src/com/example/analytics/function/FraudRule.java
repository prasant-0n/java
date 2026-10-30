package com.example.analytics.function;

import com.example.analytics.model.Order;
import java.util.Objects;

@FunctionalInterface
public interface FraudRule {

    boolean isSuspicious(Order order);

    default FraudRule and(FraudRule other) {
        Objects.requireNonNull(other, "other rule required");
        return order -> this.isSuspicious(order) && other.isSuspicious(order);
    }

    default FraudRule or(FraudRule other) {
        Objects.requireNonNull(other, "other rule required");
        return order -> this.isSuspicious(order) || other.isSuspicious(order);
    }

    default FraudRule negate() {
        return order -> !this.isSuspicious(order);
    }
}
