package com.example.etl.csv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * Streaming RFC 4180 CSV Reader with on-the-fly GZIP decompression.
 * Operates in O(1) memory relative to file size with line-bomb threshold defenses.
 */
public final class StreamingGzipCsvReader implements AutoCloseable {

    public interface RecordConsumer {
        void accept(long recordNumber, List<String> fields) throws Exception;
    }

    private enum ParserState {
        START_OF_FIELD,
        IN_UNQUOTED_FIELD,
        IN_QUOTED_FIELD,
        AFTER_QUOTE_IN_QUOTED_FIELD
    }

    private final Reader reader;
    private final int maxFieldLength;

    public StreamingGzipCsvReader(Path gzipCsvPath, int bufferSize, int maxFieldLength) throws IOException {
        Objects.requireNonNull(gzipCsvPath, "gzipCsvPath cannot be null");
        this.maxFieldLength = maxFieldLength;

        InputStream fileIn = Files.newInputStream(gzipCsvPath);
        BufferedInputStream bufIn = new BufferedInputStream(fileIn, bufferSize);
        GZIPInputStream gzipIn = new GZIPInputStream(bufIn, bufferSize);
        this.reader = new BufferedReader(new InputStreamReader(gzipIn, StandardCharsets.UTF_8), bufferSize);
    }

    /**
     * Streams all records through the consumer. Memory footprint is strictly bounded
     * to the size of a single record.
     */
    public void processAll(RecordConsumer consumer) throws Exception {
        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        ParserState state = ParserState.START_OF_FIELD;
        long recordCounter = 0;
        int chInt;

        while ((chInt = reader.read()) != -1) {
            char c = (char) chInt;

            if (currentField.length() > maxFieldLength) {
                throw new IllegalStateException("CSV Field length exceeded maximum allowed bound (" + 
                    maxFieldLength + " chars) on record " + (recordCounter + 1));
            }

            switch (state) {
                case START_OF_FIELD -> {
                    if (c == '"') {
                        state = ParserState.IN_QUOTED_FIELD;
                    } else if (c == ',') {
                        currentRecord.add("");
                        state = ParserState.START_OF_FIELD;
                    } else if (c == '\r') {
                        // Skip carriage return in CRLF
                    } else if (c == '\n') {
                        currentRecord.add("");
                        recordCounter++;
                        consumer.accept(recordCounter, new ArrayList<>(currentRecord));
                        currentRecord.clear();
                        state = ParserState.START_OF_FIELD;
                    } else {
                        currentField.append(c);
                        state = ParserState.IN_UNQUOTED_FIELD;
                    }
                }
                case IN_UNQUOTED_FIELD -> {
                    if (c == ',') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                        state = ParserState.START_OF_FIELD;
                    } else if (c == '\r') {
                        // Skip carriage return
                    } else if (c == '\n') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                        recordCounter++;
                        consumer.accept(recordCounter, new ArrayList<>(currentRecord));
                        currentRecord.clear();
                        state = ParserState.START_OF_FIELD;
                    } else {
                        currentField.append(c);
                    }
                }
                case IN_QUOTED_FIELD -> {
                    if (c == '"') {
                        state = ParserState.AFTER_QUOTE_IN_QUOTED_FIELD;
                    } else {
                        currentField.append(c);
                    }
                }
                case AFTER_QUOTE_IN_QUOTED_FIELD -> {
                    if (c == '"') {
                        // Escaped quote: "" -> "
                        currentField.append('"');
                        state = ParserState.IN_QUOTED_FIELD;
                    } else if (c == ',') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                        state = ParserState.START_OF_FIELD;
                    } else if (c == '\r') {
                        // Skip carriage return
                    } else if (c == '\n') {
                        currentRecord.add(currentField.toString());
                        currentField.setLength(0);
                        recordCounter++;
                        consumer.accept(recordCounter, new ArrayList<>(currentRecord));
                        currentRecord.clear();
                        state = ParserState.START_OF_FIELD;
                    } else {
                        currentField.append(c);
                        state = ParserState.IN_UNQUOTED_FIELD;
                    }
                }
            }
        }

        // Flush trailing record if present
        if (!currentRecord.isEmpty() || currentField.length() > 0) {
            currentRecord.add(currentField.toString());
            recordCounter++;
            consumer.accept(recordCounter, currentRecord);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
