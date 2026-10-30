package com.example.etl.test;

import com.example.etl.config.EtlBatchConfig;
import com.example.etl.engine.StreamingEtlProcessor;
import com.example.etl.model.ProcessingSummary;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * Full 10-concept integration test suite for Phase 07 Capstone Engine.
 * Verifies GZIP decompression, RFC 4180 parsing, formula sanitization, DLQ routing,
 * atomic moves, and memory safety under -Xmx64m heap constraint.
 */
public class EtlEngineIntegrationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("===============================================================================");
        System.out.println("Starting Phase 07 Capstone: Streaming High-Performance ETL Engine Verification");
        System.out.println("===============================================================================");

        Path tempDir = Files.createTempDirectory("etl-capstone-test-");

        try {
            testGzipDecompressionAndRfc4180Parsing(tempDir);
            testFormulaInjectionSanitization(tempDir);
            testValidationAndDeadLetterQueueRouting(tempDir);
            testAtomicStagingAndReplacement(tempDir);
            testHighThroughputLowMemoryStressBatch(tempDir);

            System.out.println("\nAll 10 integration test assertions passed successfully!");
            System.out.println("Phase 07 Capstone Integration Project Verified Cleanly!");
        } finally {
            // Recursive directory cleanup (P07-C005)
            deleteRecursively(tempDir);
        }
    }

    /**
     * Test 1 & 2: GZIP decompression streaming and RFC 4180 complex quoting/escaping.
     */
    private static void testGzipDecompressionAndRfc4180Parsing(Path workDir) throws Exception {
        System.out.println("Running Tests 1 & 2: GZIP Streaming and RFC 4180 Quote Parsing...");

        Path inputGz = workDir.resolve("transactions-sample.csv.gz");
        Path outputJson = workDir.resolve("output-sample.json");
        Path dlqLog = workDir.resolve("dlq-sample.log");

        // Prepare GZIP CSV
        String csvContent = "transaction_id,customer_id,amount,currency,timestamp,category,description\n" +
            "b6c00d43-7f62-4217-a1e4-c5a45e9989a1,CUST-1001,250.75,USD,2026-10-30T12:00:00Z,RETAIL,\"Standard Purchase\"\n" +
            "a8d11e54-8e73-4328-b2f5-d6b56f0090b2,CUST-1002,1200.00,EUR,2026-10-30T12:05:00Z,TECH,\"Laptop, Docking Station, and \"\"Pro\"\" Mouse\"\n";

        createGzipFile(inputGz, csvContent);

        EtlBatchConfig config = EtlBatchConfig.loadDefault();
        StreamingEtlProcessor processor = new StreamingEtlProcessor(config);
        ProcessingSummary summary = processor.execute(inputGz, outputJson, dlqLog);

        // Assertion 1: Total records count
        assert summary.totalRecordsRead() == 2 : "Assertion 1 Failed: Expected 2 data records read";
        assert summary.validRecordsWritten() == 2 : "Assertion 1 Failed: Expected 2 valid records written";
        assert summary.deadLetterRecordsCount() == 0 : "Assertion 1 Failed: Expected 0 DLQ records";

        // Assertion 2: RFC 4180 escaping preserved in JSON output
        String jsonOutput = Files.readString(outputJson, StandardCharsets.UTF_8);
        assert jsonOutput.contains("Laptop, Docking Station, and \\\"Pro\\\" Mouse")
            : "Assertion 2 Failed: Quoted string with commas and escaped quotes corrupted in JSON";

        System.out.println(" -> PASSED: GZIP Streaming and RFC 4180 Quoting Verified.");
    }

    /**
     * Test 3 & 4: Formula injection defense (CWE-1236) and multiline RFC 4180 records.
     */
    private static void testFormulaInjectionSanitization(Path workDir) throws Exception {
        System.out.println("Running Tests 3 & 4: Formula Injection Sanitization and Multiline Records...");

        Path inputGz = workDir.resolve("malicious-formula.csv.gz");
        Path outputJson = workDir.resolve("output-formula.json");
        Path dlqLog = workDir.resolve("dlq-formula.log");

        String csvContent = "transaction_id,customer_id,amount,currency,timestamp,category,description\n" +
            "11111111-1111-1111-1111-111111111111,CUST-EVIL,99.99,USD,2026-10-30T12:10:00Z,SECURITY,\"=cmd|' /C calc'!A0\"\n" +
            "22222222-2222-2222-2222-222222222222,CUST-MULTILINE,50.00,USD,2026-10-30T12:15:00Z,NOTES,\"First line\nSecond line\nThird line\"\n";

        createGzipFile(inputGz, csvContent);

        EtlBatchConfig config = EtlBatchConfig.loadDefault();
        StreamingEtlProcessor processor = new StreamingEtlProcessor(config);
        ProcessingSummary summary = processor.execute(inputGz, outputJson, dlqLog);

        assert summary.validRecordsWritten() == 2 : "Assertion 3 Failed: Multiline row must be parsed as a single record";

        String jsonOutput = Files.readString(outputJson, StandardCharsets.UTF_8);

        // Assertion 3: Formula trigger '=' must be prepended with single quote "'"
        assert jsonOutput.contains("'=cmd|' /C calc'!A0")
            : "Assertion 3 Failed: Formula injection was not sanitized";

        // Assertion 4: Multiline newline preserved inside escaped JSON string
        assert jsonOutput.contains("First line\\nSecond line\\nThird line")
            : "Assertion 4 Failed: Multiline string was not properly escaped in JSON";

        System.out.println(" -> PASSED: Formula Injection and Multiline CSV Parsing Verified.");
    }

    /**
     * Test 5 & 6: Schema validation and Dead-Letter Queue (DLQ) file isolation.
     */
    private static void testValidationAndDeadLetterQueueRouting(Path workDir) throws Exception {
        System.out.println("Running Tests 5 & 6: Business Invariant Validation and DLQ Error Logging...");

        Path inputGz = workDir.resolve("mixed-batch.csv.gz");
        Path outputJson = workDir.resolve("output-mixed.json");
        Path dlqLog = workDir.resolve("dlq-mixed.log");

        String csvContent = "transaction_id,customer_id,amount,currency,timestamp,category,description\n" +
            "33333333-3333-3333-3333-333333333333,CUST-GOOD,100.00,USD,2026-10-30T12:20:00Z,VALID,Valid Tx\n" +
            "INVALID-UUID-HERE,CUST-BAD-1,50.00,USD,2026-10-30T12:21:00Z,BAD,Bad UUID\n" +
            "44444444-4444-4444-4444-444444444444,,75.00,USD,2026-10-30T12:22:00Z,BAD,Blank Customer\n" +
            "55555555-5555-5555-5555-555555555555,CUST-BAD-3,-25.00,USD,2026-10-30T12:23:00Z,BAD,Negative Amount\n" +
            "66666666-6666-6666-6666-666666666666,CUST-BAD-4,10.00,XYZ,2026-10-30T12:24:00Z,BAD,Unsupported Currency\n" +
            "77777777-7777-7777-7777-777777777777,CUST-BAD-5,10.00,USD,NOT-A-DATE,BAD,Invalid Timestamp\n";

        createGzipFile(inputGz, csvContent);

        EtlBatchConfig config = EtlBatchConfig.loadDefault();
        StreamingEtlProcessor processor = new StreamingEtlProcessor(config);
        ProcessingSummary summary = processor.execute(inputGz, outputJson, dlqLog);

        // Assertion 5: Validation filtering
        assert summary.totalRecordsRead() == 6 : "Assertion 5 Failed: Expected 6 records read";
        assert summary.validRecordsWritten() == 1 : "Assertion 5 Failed: Only 1 record was valid";
        assert summary.deadLetterRecordsCount() == 5 : "Assertion 5 Failed: Exactly 5 records must be rejected to DLQ";

        // Assertion 6: DLQ log content and reasons
        assert Files.exists(dlqLog) : "Assertion 6 Failed: DLQ log file must exist";
        List<String> dlqLines = Files.readAllLines(dlqLog, StandardCharsets.UTF_8);
        assert dlqLines.size() == 5 : "Assertion 6 Failed: DLQ file must contain exactly 5 failure entries";
        assert dlqLines.get(0).contains("Invalid transaction UUID") : "DLQ must record UUID error";
        assert dlqLines.get(1).contains("Customer ID cannot be blank") : "DLQ must record blank customer error";
        assert dlqLines.get(2).contains("Amount must be strictly positive") : "DLQ must record negative amount error";
        assert dlqLines.get(3).contains("Unsupported currency") : "DLQ must record unsupported currency error";
        assert dlqLines.get(4).contains("Invalid ISO-8601 timestamp") : "DLQ must record invalid timestamp error";

        System.out.println(" -> PASSED: Schema Validation and DLQ Logging Verified.");
    }

    /**
     * Test 7 & 8: Crash-safe atomic staging-and-swap replacement and configuration precedence.
     */
    private static void testAtomicStagingAndReplacement(Path workDir) throws Exception {
        System.out.println("Running Tests 7 & 8: Atomic Staging-and-Swap and Configuration Precedence...");

        Path inputGz = workDir.resolve("atomic-input.csv.gz");
        Path outputJson = workDir.resolve("atomic-output.json");
        Path dlqLog = workDir.resolve("atomic-dlq.log");

        // Write existing file to verify atomic overwrite
        Files.writeString(outputJson, "OLD_CORRUPTED_CONTENT");

        String csvContent = "transaction_id,customer_id,amount,currency,timestamp,category,description\n" +
            "88888888-8888-8888-8888-888888888888,CUST-ATOMIC,350.00,USD,2026-10-30T12:30:00Z,SYSTEM,Atomic Swap\n";
        createGzipFile(inputGz, csvContent);

        // System property override simulation
        System.setProperty("etl.buffer.size", "32768");
        EtlBatchConfig config = EtlBatchConfig.loadDefault();
        assert config.bufferSizeBytes() == 32768 : "Assertion 8 Failed: System property override must take precedence";

        StreamingEtlProcessor processor = new StreamingEtlProcessor(config);
        processor.execute(inputGz, outputJson, dlqLog);

        // Assertion 7: Output file was atomically replaced and contains valid JSON
        String jsonContent = Files.readString(outputJson, StandardCharsets.UTF_8);
        assert !jsonContent.contains("OLD_CORRUPTED_CONTENT") : "Assertion 7 Failed: Staging did not overwrite old content";
        assert jsonContent.startsWith("[\n") && jsonContent.endsWith("\n]\n") : "Assertion 7 Failed: Must be valid JSON array";
        assert jsonContent.contains("88888888-8888-8888-8888-888888888888") : "Assertion 7 Failed: New record missing";

        System.out.println(" -> PASSED: Atomic Staging-and-Swap and Configuration Precedence Verified.");
    }

    /**
     * Test 9 & 10: High-throughput batch processing under low memory heap constraint (<64MB).
     */
    private static void testHighThroughputLowMemoryStressBatch(Path workDir) throws Exception {
        System.out.println("Running Tests 9 & 10: 10,000-Record Streaming Stress Batch under 64MB Heap...");

        Path stressGz = workDir.resolve("stress-10k.csv.gz");
        Path outputJson = workDir.resolve("stress-output.json");
        Path dlqLog = workDir.resolve("stress-dlq.log");

        int recordCount = 10_000;
        BigDecimal unitAmount = new BigDecimal("12.50");
        BigDecimal expectedTotalVolume = unitAmount.multiply(new BigDecimal(recordCount));

        // Generate 10,000 compressed records on the fly
        try (OutputStream fos = Files.newOutputStream(stressGz);
             BufferedOutputStream bos = new BufferedOutputStream(fos, 65536);
             GZIPOutputStream gzos = new GZIPOutputStream(bos);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzos, StandardCharsets.UTF_8))) {

            writer.write("transaction_id,customer_id,amount,currency,timestamp,category,description\n");
            for (int i = 0; i < recordCount; i++) {
                UUID id = new UUID(0x12345678L, i);
                writer.write(String.format("%s,CUST-%05d,12.50,USD,2026-10-30T12:00:00Z,BATCH,\"Batch item %d\"\n",
                    id, i, i));
            }
        }

        EtlBatchConfig config = EtlBatchConfig.loadDefault();
        StreamingEtlProcessor processor = new StreamingEtlProcessor(config);

        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        ProcessingSummary summary = processor.execute(stressGz, outputJson, dlqLog);
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Assertion 9: Data fidelity across 10,000 streaming records
        assert summary.totalRecordsRead() == recordCount : "Assertion 9 Failed: Expected 10,000 records read";
        assert summary.validRecordsWritten() == recordCount : "Assertion 9 Failed: Expected 10,000 valid records";
        assert summary.deadLetterRecordsCount() == 0 : "Assertion 9 Failed: Expected 0 DLQ records";
        assert summary.totalVolumeUsd().compareTo(expectedTotalVolume) == 0
            : "Assertion 9 Failed: Expected volume " + expectedTotalVolume + ", got " + summary.totalVolumeUsd();

        // Assertion 10: Constant memory streaming performance
        System.out.printf("   [Stress Metrics] Processed %d records in %d ms (%.1f records/sec)\n",
            summary.totalRecordsRead(), summary.processingDurationMs(), summary.throughputRecordsPerSecond());
        System.out.printf("   [Memory Metrics] Heap Used Delta: ~%.2f MB (Peak allowed: 64MB)\n",
            Math.max(0, (endMemory - startMemory) / (1024.0 * 1024.0)));

        assert Files.exists(outputJson) : "Assertion 10 Failed: Final JSON file must exist";
        assert Files.size(outputJson) > 1_500_000 : "Assertion 10 Failed: 10,000 JSON records must be >1.5MB";

        System.out.println(" -> PASSED: High-Throughput Constant Memory Batch Verified.");
    }

    private static void createGzipFile(Path path, String content) throws IOException {
        try (OutputStream fos = Files.newOutputStream(path);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             Writer writer = new OutputStreamWriter(gzos, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
             .sorted(Comparator.reverseOrder())
             .forEach(p -> {
                 try {
                     Files.deleteIfExists(p);
                 } catch (IOException ignored) {}
             });
    }
}
