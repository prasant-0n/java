package com.example.pipeline.exception;

import java.util.Map;

/**
 * Thrown when an automated transaction rollback encounters a failure.
 */
@SuppressWarnings("serial")
public class TransactionRollbackException extends PipelineException {

    public TransactionRollbackException(String message, Throwable cause, Map<String, Object> context) {
        super(ErrorCode.TRANSACTION_ROLLBACK_FAILED, message, cause, context);
    }

    public TransactionRollbackException(String message, Throwable cause) {
        super(ErrorCode.TRANSACTION_ROLLBACK_FAILED, message, cause);
    }

    public TransactionRollbackException(String message) {
        super(ErrorCode.TRANSACTION_ROLLBACK_FAILED, message);
    }
}
