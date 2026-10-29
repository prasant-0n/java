package com.example.pipeline.result;

import java.math.BigDecimal;

/**
 * Sealed hierarchy of expected business transaction declines.
 * These represent valid business branches, NOT unexpected infrastructure failures.
 */
public sealed interface DomainDecline permits 
        DomainDecline.InsufficientFunds,
        DomainDecline.DailyLimitExceeded,
        DomainDecline.AccountFrozen,
        DomainDecline.AccountNotFound {

    String reason();

    record InsufficientFunds(String accountId, BigDecimal requested, BigDecimal available) implements DomainDecline {
        @Override
        public String reason() {
            return "Account " + accountId + " has insufficient funds: requested " + requested + ", available " + available;
        }
    }

    record DailyLimitExceeded(String accountId, BigDecimal requested, BigDecimal remainingLimit) implements DomainDecline {
        @Override
        public String reason() {
            return "Transfer of " + requested + " exceeds remaining daily limit " + remainingLimit + " for account " + accountId;
        }
    }

    record AccountFrozen(String accountId, String reasonMessage) implements DomainDecline {
        @Override
        public String reason() {
            return "Account " + accountId + " is frozen: " + reasonMessage;
        }
    }

    record AccountNotFound(String accountId) implements DomainDecline {
        @Override
        public String reason() {
            return "Account not registered in ledger: " + accountId;
        }
    }
}
