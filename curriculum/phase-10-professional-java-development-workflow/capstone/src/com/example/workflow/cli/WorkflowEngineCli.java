package com.example.workflow.cli;

import com.example.workflow.git.ConventionalCommitValidator;
import com.example.workflow.git.GitDagEngine;
import com.example.workflow.logging.MdcTraceContext;
import com.example.workflow.logging.PiiMaskingConverter;
import com.example.workflow.logging.StructuredJsonLogEncoder;
import com.example.workflow.model.MaskedSecret;
import com.example.workflow.model.UserSession;
import com.example.workflow.quality.EncapsulationGuard;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Command-Line Interface demonstrating Phase 10 synthesis:
 * Maven builds, structured logging with MDC, PII masking, diagnostics, and git hygiene.
 */
public class WorkflowEngineCli {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   DevOps Workflow Engine CLI (Phase 10 Capstone Integration Project)          ");
        System.out.println("================================================================================");

        // 1. Domain Model with Type-Level Secret Masking
        UserSession session = new UserSession(
            "SES-8891",
            "alex_admin",
            "TENANT-CORP",
            MaskedSecret.of("SuperSecretAuthToken99#"),
            MaskedSecret.of("4111-2222-3333-4444")
        );
        System.out.println("[STEP 1] Domain session created: " + session);

        // 2. MDC Tracing and Structured JSON Logging
        System.out.println("[STEP 2] Establishing MDC Context and emitting Structured JSON Log Event:");
        try (MdcTraceContext.Scope scope = MdcTraceContext.openScope("TRACE-9901-XYZ", session.tenantId(), session.username())) {
            String rawMessage = "Processed charge on card 4111-2222-3333-4444 for $299.00";
            String sanitizedMessage = PiiMaskingConverter.sanitize(rawMessage);

            StructuredJsonLogEncoder.LogRecord record = new StructuredJsonLogEncoder.LogRecord(
                Instant.now(),
                "INFO",
                "com.example.workflow.OrderService",
                Thread.currentThread().getName(),
                sanitizedMessage,
                MdcTraceContext.getCopyOfContextMap()
            );

            String jsonOutput = StructuredJsonLogEncoder.encode(record);
            System.out.println("         " + jsonOutput);
        }

        // 3. Encapsulation & Defensive Copying
        byte[] sensitiveMemory = new byte[]{0x10, 0x20, 0x30};
        EncapsulationGuard.GuardedBuffer guarded = new EncapsulationGuard.GuardedBuffer(sensitiveMemory);
        sensitiveMemory[0] = (byte) 0xFF; // External mutation
        boolean isolated = guarded.getBuffer()[0] == 0x10;
        System.out.println("[STEP 3] Encapsulation defensive copy verification: " + (isolated ? "ISOLATED (PASSED)" : "FAILED"));

        // 4. Conventional Commit & SemVer Calculation
        String commitMsg = "feat(audit)!: introduce immutable distributed ledger logging";
        var commitOpt = ConventionalCommitValidator.validate(commitMsg);
        if (commitOpt.isPresent()) {
            var c = commitOpt.get();
            System.out.println("[STEP 4] Conventional Commit Parsed: " + c.type() + "(" + c.scope() + ") -> Breaking=" + c.isBreaking());
            System.out.println("         Calculated SemVer Version Bump: " + c.getSemVerBump());
        }

        // 5. Git DAG Fast-Forward Merge Simulation
        GitDagEngine.CommitNode c0 = new GitDagEngine.CommitNode("Initial commit");
        GitDagEngine.CommitNode c1 = new GitDagEngine.CommitNode("feat: setup build", c0);
        GitDagEngine.CommitNode f1 = new GitDagEngine.CommitNode("feat(order): add cart service", c1);
        GitDagEngine.CommitNode merged = GitDagEngine.merge(c1, f1, "Merge branch 'feature'");
        System.out.println("[STEP 5] Git DAG Fast-Forward merge resolved to commit: " + merged.getHash() + " (" + merged.getMessage() + ")");

        System.out.println("================================================================================");
        System.out.println(" Phase 10 Capstone Engine executed successfully with zero runtime errors!       ");
        System.out.println("================================================================================");
    }
}
