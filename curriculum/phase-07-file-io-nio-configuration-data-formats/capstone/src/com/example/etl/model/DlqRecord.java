package com.example.etl.model;

import java.time.Instant;
import java.util.List;

/**
 * Dead-Letter Queue record representing a rejected or malformed raw row.
 */
public record DlqRecord(
    long lineNumber,
    List<String> rawTokens,
    String failureReason,
    Instant failureTimestamp
) {
    public String toLogLine() {
        return String.format("[%s] LINE %d: REASON='%s' RAW=%s",
            failureTimestamp, lineNumber, failureReason, rawTokens);
    }
}
