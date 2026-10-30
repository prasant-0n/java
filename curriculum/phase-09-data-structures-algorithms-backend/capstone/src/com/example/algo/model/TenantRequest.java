package com.example.algo.model;

import java.util.Objects;

/**
 * Inbound API request submitted by a tenant for ingestion and rate limiting.
 */
public record TenantRequest(
    String requestId,
    String tenantId,
    long timestampMs,
    String payload
) {
    public TenantRequest {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
    }
}
