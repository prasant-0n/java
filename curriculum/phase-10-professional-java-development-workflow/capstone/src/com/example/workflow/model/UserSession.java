package com.example.workflow.model;

/**
 * Domain entity demonstrating encapsulation and type-level masking defense.
 */
public record UserSession(
    String sessionId,
    String username,
    String tenantId,
    MaskedSecret token,
    MaskedSecret creditCardPan
) {
    @Override
    public String toString() {
        return "UserSession[id=" + sessionId + ", user=" + username +
               ", tenant=" + tenantId + ", token=" + token + ", card=" + creditCardPan + "]";
    }
}
