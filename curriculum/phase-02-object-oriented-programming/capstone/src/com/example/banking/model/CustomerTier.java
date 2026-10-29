package com.example.banking.model;

public enum CustomerTier {
    STANDARD(100_000L),  // Daily transfer limit $1,000.00
    PREMIUM(1_000_000L),  // Daily transfer limit $10,000.00
    VIP(10_000_000L);     // Daily transfer limit $100,000.00

    private final long dailyTransferLimitCents;

    CustomerTier(long dailyTransferLimitCents) {
        this.dailyTransferLimitCents = dailyTransferLimitCents;
    }

    public long getDailyTransferLimitCents() {
        return dailyTransferLimitCents;
    }
}
