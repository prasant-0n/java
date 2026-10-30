package com.example.etl.json;

import com.example.etl.model.TransactionRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Streaming JSON Writer that emits an array of transaction records
 * without buffering the complete collection in heap memory.
 */
public final class StreamingJsonWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private boolean firstRecord = true;

    public StreamingJsonWriter(Path outputPath, int bufferSize) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath cannot be null");
        this.writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
        this.writer.write("[\n");
    }

    /**
     * Serializes a single TransactionRecord as a JSON object into the stream.
     */
    public void writeRecord(TransactionRecord record) throws IOException {
        if (!firstRecord) {
            writer.write(",\n");
        } else {
            firstRecord = false;
        }

        writer.write("  {\n");
        writer.write("    \"transactionId\": \"" + record.transactionId() + "\",\n");
        writer.write("    \"customerId\": \"" + escapeJson(record.customerId()) + "\",\n");
        writer.write("    \"amount\": " + record.amount().toPlainString() + ",\n");
        writer.write("    \"currency\": \"" + record.currency() + "\",\n");
        writer.write("    \"timestamp\": \"" + record.timestamp() + "\",\n");
        writer.write("    \"category\": \"" + escapeJson(record.category()) + "\",\n");
        writer.write("    \"description\": \"" + escapeJson(record.description()) + "\"\n");
        writer.write("  }");
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder(raw.length() + 8);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
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

    @Override
    public void close() throws IOException {
        writer.write("\n]\n");
        writer.flush();
        writer.close();
    }
}
