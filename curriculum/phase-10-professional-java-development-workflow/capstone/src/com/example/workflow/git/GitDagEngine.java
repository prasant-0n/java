package com.example.workflow.git;

import java.security.MessageDigest;
import java.util.*;

/**
 * Git Directed Acyclic Graph (DAG) commit topology and Fast-Forward / 3-Way merge engine.
 */
public class GitDagEngine {

    public static class CommitNode {
        private final String hash;
        private final String message;
        private final List<CommitNode> parents;

        public CommitNode(String message, CommitNode... parents) {
            this.message = message;
            this.parents = parents != null ? Arrays.asList(parents) : Collections.emptyList();
            this.hash = computeHash(message, this.parents);
        }

        private static String computeHash(String msg, List<CommitNode> parents) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(msg.getBytes());
                for (CommitNode p : parents) md.update(p.hash.getBytes());
                byte[] dig = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : dig) sb.append(String.format("%02x", b));
                return sb.substring(0, 8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getHash() { return hash; }
        public String getMessage() { return message; }
        public List<CommitNode> getParents() { return parents; }
    }

    public static boolean isAncestor(CommitNode ancestor, CommitNode descendant) {
        if (ancestor == null || descendant == null) return false;
        if (ancestor.getHash().equals(descendant.getHash())) return true;

        Queue<CommitNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(descendant);
        visited.add(descendant.getHash());

        while (!queue.isEmpty()) {
            CommitNode curr = queue.poll();
            for (CommitNode p : curr.getParents()) {
                if (p.getHash().equals(ancestor.getHash())) return true;
                if (visited.add(p.getHash())) {
                    queue.add(p);
                }
            }
        }
        return false;
    }

    public static CommitNode merge(CommitNode targetTip, CommitNode featureTip, String mergeMsg) {
        if (isAncestor(targetTip, featureTip)) {
            return featureTip; // Fast-forward merge
        }
        return new CommitNode(mergeMsg, targetTip, featureTip); // 3-Way merge
    }
}
