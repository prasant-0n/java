package com.example.pipeline.exception;

/**
 * Standardized, machine-readable error codes for the transaction pipeline.
 */
public enum ErrorCode {
    LEDGER_LOCK_TIMEOUT(503, "Service Unavailable: Ledger Lock Acquisition Timed Out"),
    CONCURRENT_MODIFICATION(409, "Conflict: Account State Concurrently Modified"),
    DATA_STORE_UNAVAILABLE(503, "Service Unavailable: Account Repository Storage Offline"),
    TRANSACTION_ROLLBACK_FAILED(500, "Internal Server Error: Transaction Rollback Aborted with Faults"),
    INVALID_TRANSACTION_PAYLOAD(400, "Bad Request: Transaction Precondition Violated");

    private final int httpStatus;
    private final String description;

    ErrorCode(int httpStatus, String description) {
        this.httpStatus = httpStatus;
        this.description = description;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDescription() {
        return description;
    }
}
