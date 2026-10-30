package com.example.algo.dp;

import com.example.algo.model.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 0/1 Knapsack resource optimizer for scheduling task batches onto resource-constrained workers.
 * Selects the subset of tasks that maximizes total priority weight without exceeding worker memory limits.
 */
public class TaskResourceOptimizer {

    public record OptimizationResult(
        int totalPriorityWeight,
        int totalMemoryCostMb,
        List<Task> selectedTasks
    ) {}

    /**
     * Solves 0/1 Knapsack with full item reconstruction via backtracking.
     */
    public static OptimizationResult optimizeBatch(List<Task> candidates, int workerMemoryCapacityMb) {
        int n = candidates.size();
        int[][] dp = new int[n + 1][workerMemoryCapacityMb + 1];

        // 1. Bottom-up 2D tabulation
        for (int i = 1; i <= n; i++) {
            Task task = candidates.get(i - 1);
            int weight = task.memoryCostMb();
            int value = task.priorityWeight();

            for (int c = 0; c <= workerMemoryCapacityMb; c++) {
                if (weight > c) {
                    dp[i][c] = dp[i - 1][c];
                } else {
                    dp[i][c] = Math.max(dp[i - 1][c], dp[i - 1][c - weight] + value);
                }
            }
        }

        int maxPriority = dp[n][workerMemoryCapacityMb];

        // 2. Backtracking to reconstruct selected tasks
        List<Task> selected = new ArrayList<>();
        int currCap = workerMemoryCapacityMb;
        int usedMemory = 0;

        for (int i = n; i > 0 && currCap > 0; i--) {
            if (dp[i][currCap] != dp[i - 1][currCap]) {
                Task task = candidates.get(i - 1);
                selected.add(task);
                currCap -= task.memoryCostMb();
                usedMemory += task.memoryCostMb();
            }
        }
        Collections.reverse(selected);

        return new OptimizationResult(maxPriority, usedMemory, selected);
    }

    /**
     * Space-optimized 1D backward rolling array returning maximum achievable priority weight.
     */
    public static int optimizeBatchSpaceEfficient(List<Task> candidates, int workerMemoryCapacityMb) {
        int[] dp = new int[workerMemoryCapacityMb + 1];

        for (Task task : candidates) {
            int weight = task.memoryCostMb();
            int value = task.priorityWeight();

            for (int c = workerMemoryCapacityMb; c >= weight; c--) {
                dp[c] = Math.max(dp[c], dp[c - weight] + value);
            }
        }
        return dp[workerMemoryCapacityMb];
    }
}
