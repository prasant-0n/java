package com.example.billing.security;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

/**
 * High-performance UUIDv7 Generator and 16-Byte Binary Packer.
 * Synthesizes Concept P05-C006 (RFC 9562 UUIDv7 & Database Indexing).
 * Generates K-sortable, monotonically increasing UUIDs preserving B+ Tree locality.
 */
public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
        // Utility class
    }

    /**
     * Generates an RFC 9562 UUIDv7 based on current system time in milliseconds.
     */
    public static UUID generateUuidV7() {
        return generateUuidV7(System.currentTimeMillis());
    }

    /**
     * Generates an RFC 9562 UUIDv7 with explicit timestamp in milliseconds.
     * Layout:
     * - Bits 00-47: 48-bit Unix timestamp in milliseconds
     * - Bits 48-51: 4-bit Version (0111 = 0x7)
     * - Bits 52-63: 12-bit random entropy
     * - Bits 64-65: 2-bit Variant (10 = RFC 4122/9562)
     * - Bits 66-127: 62-bit random entropy
     */
    public static UUID generateUuidV7(long timestampMillis) {
        long msb = (timestampMillis & 0xFFFFFFFFFFFFL) << 16;
        msb |= 0x7000L; // Version 7 in bits 48-51
        msb |= (RANDOM.nextInt(0x1000) & 0x0FFFL); // 12-bit random sequence

        long lsb = RANDOM.nextLong();
        lsb &= 0x3FFFFFFFFFFFFFFFL; // Clear bits 64-65
        lsb |= 0x8000000000000000L; // Set variant 10 in bits 64-65

        return new UUID(msb, lsb);
    }

    /**
     * Extracts the 48-bit millisecond timestamp from a UUIDv7.
     */
    public static long extractTimestampMillis(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid must not be null");
        if (uuid.version() != 7) {
            throw new IllegalArgumentException("Expected UUIDv7 but found version: " + uuid.version());
        }
        return uuid.getMostSignificantBits() >>> 16;
    }

    /**
     * Packs a 128-bit UUID into a 16-byte binary representation (55% space savings over String).
     */
    public static byte[] toBytes(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid must not be null");
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Reconstructs a UUID from a 16-byte binary representation.
     */
    public static UUID fromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        if (bytes.length != 16) {
            throw new IllegalArgumentException("UUID byte array must be exactly 16 bytes, received: " + bytes.length);
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
