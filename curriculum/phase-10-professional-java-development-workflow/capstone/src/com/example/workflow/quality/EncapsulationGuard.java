package com.example.workflow.quality;

import java.util.Arrays;

/**
 * Encapsulation and SpotBugs EI_EXPOSE_REP defensive copy utility.
 */
public class EncapsulationGuard {

    public static final class GuardedBuffer {
        private final byte[] buffer;

        public GuardedBuffer(byte[] input) {
            // Remediates EI_EXPOSE_REP2 (clones incoming mutable array)
            this.buffer = input != null ? input.clone() : new byte[0];
        }

        public byte[] getBuffer() {
            // Remediates EI_EXPOSE_REP (returns defensive clone)
            return this.buffer.clone();
        }

        public int size() {
            return buffer.length;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GuardedBuffer that)) return false;
            return Arrays.equals(buffer, that.buffer);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(buffer);
        }
    }
}
