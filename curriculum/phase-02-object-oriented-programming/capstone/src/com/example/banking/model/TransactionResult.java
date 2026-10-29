package com.example.banking.model;

import java.time.Instant;
import java.util.Objects;

public sealed interface TransactionResult permits 
    TransactionResult.Success, 
    TransactionResult.RequiresChallenge, 
    TransactionResult.Rejected {

    record Success(
        TransactionRecord record, 
        String confirmationCode, 
        Instant timestamp
    ) implements TransactionResult {
        public Success {
            Objects.requireNonNull(record);
            Objects.requireNonNull(confirmationCode);
            Objects.requireNonNull(timestamp);
        }
    }

    record RequiresChallenge(
        String challengeId, 
        String challengeType, 
        Money attemptedAmount, 
        Instant expiresAt
    ) implements TransactionResult {
        public RequiresChallenge {
            Objects.requireNonNull(challengeId);
            Objects.requireNonNull(challengeType);
            Objects.requireNonNull(attemptedAmount);
            Objects.requireNonNull(expiresAt);
        }
    }

    record Rejected(
        String reasonCode, 
        String details, 
        Instant timestamp
    ) implements TransactionResult {
        public Rejected {
            Objects.requireNonNull(reasonCode);
            Objects.requireNonNull(details);
            Objects.requireNonNull(timestamp);
        }
    }
}
