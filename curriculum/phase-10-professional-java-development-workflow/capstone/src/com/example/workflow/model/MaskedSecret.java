package com.example.workflow.model;

import java.util.Objects;

/**
 * Type-level domain model wrapper guaranteeing zero plaintext secret leakage
 * in logs, exception frames, or string serializations.
 */
public final class MaskedSecret {
    private final String rawSecret;

    private MaskedSecret(String rawSecret) {
        this.rawSecret = rawSecret != null ? rawSecret : "";
    }

    public static MaskedSecret of(String secret) {
        return new MaskedSecret(secret);
    }

    public String getUnmaskedValue() {
        return rawSecret;
    }

    @Override
    public String toString() {
        return "********";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaskedSecret that)) return false;
        return Objects.equals(rawSecret, that.rawSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawSecret);
    }
}
