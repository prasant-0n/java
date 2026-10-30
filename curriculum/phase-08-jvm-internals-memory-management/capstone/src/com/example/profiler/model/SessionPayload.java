package com.example.profiler.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an in-memory user session with attached byte payload.
 */
public record SessionPayload(
        UUID sessionId,
        String userId,
        long tenantId,
        Instant createdAt,
        byte[] sessionData
) {
    public SessionPayload {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        sessionData = sessionData != null ? sessionData.clone() : new byte[0];
    }

    public static SessionPayload of(UUID sessionId, String userId, long tenantId, int payloadSizeBytes) {
        return new SessionPayload(sessionId, userId, tenantId, Instant.now(), new byte[payloadSizeBytes]);
    }

    public int getPayloadLength() {
        return sessionData.length;
    }
}
