package com.example.pipeline.exception;

import java.util.Map;

/**
 * Thrown when acquiring a distributed ledger lock fails or times out.
 */
@SuppressWarnings("serial")
public class LedgerLockException extends PipelineException {

    public LedgerLockException(String message, Throwable cause, Map<String, Object> context) {
        super(ErrorCode.LEDGER_LOCK_TIMEOUT, message, cause, context);
    }

    public LedgerLockException(String message, Throwable cause) {
        super(ErrorCode.LEDGER_LOCK_TIMEOUT, message, cause);
    }

    public LedgerLockException(String message) {
        super(ErrorCode.LEDGER_LOCK_TIMEOUT, message);
    }
}
