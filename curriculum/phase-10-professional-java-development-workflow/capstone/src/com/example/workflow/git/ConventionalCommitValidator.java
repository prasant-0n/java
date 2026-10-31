package com.example.workflow.git;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conventional Commits 1.0.0 semantic commit message validator and SemVer calculator.
 */
public class ConventionalCommitValidator {

    public enum SemVerBump { NONE, PATCH, MINOR, MAJOR }

    public record ValidatedCommit(
        String raw,
        String type,
        String scope,
        boolean isBreaking,
        String description
    ) {
        public SemVerBump getSemVerBump() {
            if (isBreaking) return SemVerBump.MAJOR;
            if ("feat".equalsIgnoreCase(type)) return SemVerBump.MINOR;
            if ("fix".equalsIgnoreCase(type) || "perf".equalsIgnoreCase(type)) return SemVerBump.PATCH;
            return SemVerBump.NONE;
        }
    }

    private static final Pattern HEADER_PATTERN = Pattern.compile(
        "^([a-z]+)(?:\\(([a-zA-Z0-9_\\-]+)\\))?(!)?\\s*:\\s*(.+)$"
    );

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "feat", "fix", "docs", "style", "refactor", "perf", "test", "build", "ci", "chore", "revert"
    );

    public static Optional<ValidatedCommit> validate(String message) {
        if (message == null || message.isBlank()) return Optional.empty();
        String header = message.lines().findFirst().orElse("").trim();

        Matcher matcher = HEADER_PATTERN.matcher(header);
        if (!matcher.matches()) return Optional.empty();

        String type = matcher.group(1);
        if (!ALLOWED_TYPES.contains(type)) return Optional.empty();

        String scope = matcher.group(2);
        boolean breakingExclamation = matcher.group(3) != null;
        String desc = matcher.group(4);

        boolean breakingFooter = message.contains("BREAKING CHANGE:") || message.contains("BREAKING-CHANGE:");
        boolean isBreaking = breakingExclamation || breakingFooter;

        return Optional.of(new ValidatedCommit(message, type, scope, isBreaking, desc));
    }
}
