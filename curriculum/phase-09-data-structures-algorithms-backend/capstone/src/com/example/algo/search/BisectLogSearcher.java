package com.example.algo.search;

import com.example.algo.model.ExecutionMetrics;
import java.util.List;

/**
 * High-performance Bisect Binary Search engine for auditing historical execution logs.
 * Employs unsigned right shift ((low + high) >>> 1) for overflow-safe midpoint calculations.
 */
public class BisectLogSearcher {

    /**
     * Finds the first index i such that metrics[i].executedTimestampMs >= targetTimestamp.
     */
    public static int bisectLowerBound(List<ExecutionMetrics> metrics, long targetTimestamp) {
        int low = 0;
        int high = metrics.size();

        while (low < high) {
            int mid = (low + high) >>> 1;
            if (metrics.get(mid).executedTimestampMs() >= targetTimestamp) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * Finds the first index i such that metrics[i].executedTimestampMs > targetTimestamp.
     */
    public static int bisectUpperBound(List<ExecutionMetrics> metrics, long targetTimestamp) {
        int low = 0;
        int high = metrics.size();

        while (low < high) {
            int mid = (low + high) >>> 1;
            if (metrics.get(mid).executedTimestampMs() > targetTimestamp) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * Counts the number of execution records matching targetTimestamp exactly in O(log N) time.
     */
    public static int countExactTimestampMatches(List<ExecutionMetrics> metrics, long targetTimestamp) {
        int lower = bisectLowerBound(metrics, targetTimestamp);
        int upper = bisectUpperBound(metrics, targetTimestamp);
        return upper - lower;
    }
}
