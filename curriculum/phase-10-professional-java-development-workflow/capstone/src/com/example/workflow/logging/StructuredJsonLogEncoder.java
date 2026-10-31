package com.example.workflow.logging;

import java.time.Instant;
import java.util.Map;

/**
 * High-performance structured JSON log encoder neutralizing CRLF log forging
 * and embedding immutable MDC context snapshots.
 */
public class StructuredJsonLogEncoder {

    public record LogRecord(
        Instant timestamp,
        String level,
        String logger,
        String thread,
        String message,
        Map<String, String> mdc
    ) {}

    public static String encode(LogRecord record) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{");
        sb.append("\"@timestamp\":\"").append(record.timestamp()).append("\",");
        sb.append("\"level\":\"").append(record.level()).append("\",");
        sb.append("\"logger\":\"").append(escapeJson(record.logger())).append("\",");
        sb.append("\"thread\":\"").append(escapeJson(record.thread())).append("\",");
        sb.append("\"message\":\"").append(escapeJson(record.message())).append("\"");

        if (record.mdc() != null && !record.mdc().isEmpty()) {
            sb.append(",\"mdc\":{");
            boolean first = true;
            for (Map.Entry<String, String> entry : record.mdc().entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                  .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    public static String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n"); // Escaped newline prevents CRLF log forging
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
