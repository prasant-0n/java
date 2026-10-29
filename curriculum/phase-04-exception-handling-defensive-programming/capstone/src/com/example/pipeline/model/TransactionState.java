package com.example.pipeline.model;

/**
 * State lifecycle for financial transactions.
 */
public enum TransactionState {
    PENDING,
    ACQUIRING_LOCKS,
    PROCESSING,
    COMMITTED,
    ROLLED_BACK
}
