package com.example.etl.engine;

import com.example.etl.config.EtlBatchConfig;
import com.example.etl.csv.StreamingGzipCsvReader;
import com.example.etl.json.StreamingJsonWriter;
import com.example.etl.model.DlqRecord;
import com.example.etl.model.ProcessingSummary;
import com.example.etl.model.TransactionRecord;
import com.example.etl.validator.TransactionValidator;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.Objects;

/**
 * High-performance streaming ETL batch orchestrator.
 * Connects GZIP CSV stream, validation, DLQ routing, and atomic JSON file writing.
 */
public final class StreamingEtlProcessor {

    private final EtlBatchConfig config;
    private final TransactionValidator validator;

    public StreamingEtlProcessor(EtlBatchConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.validator = new TransactionValidator(config);
    }

    /**
     * Executes the ETL batch job.
     *
     * @param inputGzipCsv Path to input gzip-compressed CSV file.
     * @param outputJson   Final destination path for validated JSON output.
     * @param dlqLogPath   Destination path for rejected records log.
     * @return ProcessingSummary with throughput and aggregate volume metrics.
     */
    public ProcessingSummary execute(Path inputGzipCsv, Path outputJson, Path dlqLogPath) throws Exception {
        Objects.requireNonNull(inputGzipCsv, "inputGzipCsv cannot be null");
        Objects.requireNonNull(outputJson, "outputJson cannot be null");
        Objects.requireNonNull(dlqLogPath, "dlqLogPath cannot be null");

        if (!Files.isRegularFile(inputGzipCsv)) {
            throw new NoSuchFileException("Input file does not exist: " + inputGzipCsv);
        }

        long startTime = System.currentTimeMillis();
        Path outputDir = outputJson.getParent();
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }
        Files.createDirectories(outputDir);

        // Crash-safe atomic staging file pattern (P07-C004)
        Path stagingJson = Files.createTempFile(outputDir, "etl-staging-", ".tmp");

        long[] counters = new long[3]; // [0: totalRead, 1: validCount, 2: dlqCount]
        BigDecimal[] totalVolume = new BigDecimal[]{BigDecimal.ZERO};

        try {
            try (StreamingGzipCsvReader csvReader = new StreamingGzipCsvReader(
                    inputGzipCsv, config.bufferSizeBytes(), config.maxRecordLengthChars());
                 StreamingJsonWriter jsonWriter = new StreamingJsonWriter(
                     stagingJson, config.bufferSizeBytes());
                 BufferedWriter dlqWriter = Files.newBufferedWriter(
                     dlqLogPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                csvReader.processAll((recordNumber, fields) -> {
                    // Record 1 is header
                    if (recordNumber == 1) {
                        return;
                    }

                    counters[0]++; // Total data records read

                    try {
                        TransactionRecord record = validator.validate(recordNumber, fields);
                        jsonWriter.writeRecord(record);
                        counters[1]++; // Valid record written
                        totalVolume[0] = totalVolume[0].add(record.amount());
                    } catch (Exception e) {
                        counters[2]++; // DLQ record
                        DlqRecord dlq = new DlqRecord(recordNumber, fields, e.getMessage(), Instant.now());
                        dlqWriter.write(dlq.toLogLine());
                        dlqWriter.newLine();
                    }
                });
            }

            // Atomically replace destination file with staging file (rename(2) inode swap)
            try {
                Files.move(stagingJson, outputJson, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback for cross-filesystem moves
                Files.move(stagingJson, outputJson, StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            // Clean up staging file if an unhandled failure occurred prior to move
            Files.deleteIfExists(stagingJson);
        }

        long durationMs = Math.max(1, System.currentTimeMillis() - startTime);

        return new ProcessingSummary(
            counters[0],
            counters[1],
            counters[2],
            totalVolume[0],
            durationMs
        );
    }
}
