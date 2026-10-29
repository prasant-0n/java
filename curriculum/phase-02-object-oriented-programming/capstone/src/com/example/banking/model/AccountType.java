package com.example.banking.model;

public enum AccountType {
    CHECKING(0) {
        @Override
        public long calculateMonthlyInterestCents(long balanceCents) {
            return 0L; // Checking yields zero interest
        }
    },
    SAVINGS(350) { // 3.50% APY -> basis points = 350
        @Override
        public long calculateMonthlyInterestCents(long balanceCents) {
            if (balanceCents <= 0) return 0L;
            return (balanceCents * 350L) / 120_000L;
        }
    },
    INVESTMENT(500) { // 5.00% APY
        @Override
        public long calculateMonthlyInterestCents(long balanceCents) {
            if (balanceCents <= 0) return 0L;
            return (balanceCents * 500L) / 120_000L;
        }
    };

    private final int annualBasisPoints;

    AccountType(int annualBasisPoints) {
        this.annualBasisPoints = annualBasisPoints;
    }

    public int getAnnualBasisPoints() { return annualBasisPoints; }
    public abstract long calculateMonthlyInterestCents(long balanceCents);
}
