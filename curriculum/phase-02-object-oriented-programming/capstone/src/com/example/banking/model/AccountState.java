package com.example.banking.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public enum AccountState {
    ACTIVE {
        @Override
        public Set<AccountState> allowedTransitions() {
            return EnumSet.of(FROZEN, DORMANT, CLOSED);
        }
        @Override
        public boolean allowsTransactions() { return true; }
    },
    FROZEN {
        @Override
        public Set<AccountState> allowedTransitions() {
            return EnumSet.of(ACTIVE, CLOSED);
        }
        @Override
        public boolean allowsTransactions() { return false; }
    },
    DORMANT {
        @Override
        public Set<AccountState> allowedTransitions() {
            return EnumSet.of(ACTIVE, CLOSED);
        }
        @Override
        public boolean allowsTransactions() { return false; }
    },
    CLOSED {
        @Override
        public Set<AccountState> allowedTransitions() {
            return EnumSet.noneOf(AccountState.class);
        }
        @Override
        public boolean allowsTransactions() { return false; }
    };

    public abstract Set<AccountState> allowedTransitions();
    public abstract boolean allowsTransactions();

    public boolean canTransitionTo(AccountState next) {
        Objects.requireNonNull(next);
        return allowedTransitions().contains(next);
    }
}
