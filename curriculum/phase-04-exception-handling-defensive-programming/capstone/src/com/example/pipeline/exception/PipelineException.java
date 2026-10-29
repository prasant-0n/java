package com.example.pipeline.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract root domain exception enforcing error codes and structured diagnostic context.
 */
@SuppressWarnings("serial")
public abstract class PipelineException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> context;

    public PipelineException(ErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode, "ErrorCode cannot be null").getDescription());
        this.errorCode = errorCode;
        this.context = Map.of();
    }

    public PipelineException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
        this.context = Map.of();
    }

    public PipelineException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
        this.context = Map.of();
    }

    public PipelineException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
        this.context = Map.of();
    }

    public PipelineException(ErrorCode errorCode, String message, Throwable cause, Map<String, Object> context) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
        this.context = context != null ? Map.copyOf(new HashMap<>(context)) : Map.of();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
