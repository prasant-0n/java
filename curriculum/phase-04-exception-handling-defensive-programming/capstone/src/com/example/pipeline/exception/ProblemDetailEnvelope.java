package com.example.pipeline.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Standard RFC 7807 / RFC 9457 Problem Details error payload.
 */
public record ProblemDetailEnvelope(
        URI type,
        String title,
        int status,
        String detail,
        URI instance,
        Instant timestamp,
        Map<String, Object> properties
) {
    public ProblemDetailEnvelope {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(detail, "Detail cannot be null");
        Objects.requireNonNull(instance, "Instance cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        properties = properties != null ? Map.copyOf(properties) : Map.of();
    }

    public static ProblemDetailEnvelope fromPipelineException(PipelineException ex, String path, String traceId) {
        URI typeUri = URI.create("https://api.bank.com/errors/" + ex.getErrorCode().name().toLowerCase());
        URI instanceUri = URI.create(path + "?traceId=" + traceId);

        return new ProblemDetailEnvelope(
                typeUri,
                ex.getErrorCode().name(),
                ex.getErrorCode().getHttpStatus(),
                ex.getMessage(),
                instanceUri,
                Instant.now(),
                ex.getContext()
        );
    }
}
