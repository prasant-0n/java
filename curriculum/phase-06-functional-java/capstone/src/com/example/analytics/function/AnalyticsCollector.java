package com.example.analytics.function;

import com.example.analytics.model.CustomerTier;
import com.example.analytics.model.LineItem;
import com.example.analytics.model.Order;
import com.example.analytics.model.TierAnalyticsSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collector;

public final class AnalyticsCollector {

    private AnalyticsCollector() {}

    public static Collector<Order, ?, TierAnalyticsSummary> toTierSummary(CustomerTier tier) {
        Objects.requireNonNull(tier, "tier required");

        class Accumulator {
            long count = 0;
            BigDecimal total = BigDecimal.ZERO;
            final Set<String> categories = new HashSet<>();

            void accept(Order order) {
                count++;
                total = total.add(order.totalAmount());
                for (LineItem item : order.items()) {
                    categories.add(item.category());
                }
            }

            Accumulator combine(Accumulator other) {
                this.count += other.count;
                this.total = this.total.add(other.total);
                this.categories.addAll(other.categories);
                return this;
            }

            TierAnalyticsSummary finish() {
                BigDecimal avg = count > 0
                        ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_EVEN)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

                return new TierAnalyticsSummary(
                        tier,
                        count,
                        total.setScale(2, RoundingMode.HALF_EVEN),
                        avg,
                        categories
                );
            }
        }

        return Collector.of(
                Accumulator::new,
                Accumulator::accept,
                Accumulator::combine,
                Accumulator::finish
        );
    }
}
