package com.example.algo.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Graph dependency manager for workflow tasks.
 * Uses an Adjacency List to model dependency constraints (u -> v indicates u must execute before v).
 * Implements Three-Color DFS cycle detection to reject circular dependencies
 * and Kahn's In-Degree Algorithm for topological sorting.
 */
public class TaskDependencyGraph {
    private final Map<Long, List<Long>> adj = new HashMap<>();
    private final Map<Long, Integer> inDegree = new HashMap<>();
    private final Set<Long> allVertices = new HashSet<>();

    public void addVertex(long taskId) {
        allVertices.add(taskId);
        adj.putIfAbsent(taskId, new ArrayList<>());
        inDegree.putIfAbsent(taskId, 0);
    }

    /**
     * Adds dependency edge: 'fromTaskId' must execute BEFORE 'toTaskId'.
     */
    public void addDependency(long fromTaskId, long toTaskId) {
        addVertex(fromTaskId);
        addVertex(toTaskId);
        adj.get(fromTaskId).add(toTaskId);
        inDegree.put(toTaskId, inDegree.get(toTaskId) + 1);
    }

    /**
     * Three-Color DFS Cycle Detection (WHITE=0, GRAY=1, BLACK=2).
     * Returns true if a circular dependency is detected.
     */
    public boolean hasCycle() {
        Map<Long, Integer> colors = new HashMap<>();
        for (long v : allVertices) {
            colors.put(v, 0); // WHITE
        }

        for (long v : allVertices) {
            if (colors.get(v) == 0) {
                if (dfsCheckCycle(v, colors)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsCheckCycle(long u, Map<Long, Integer> colors) {
        colors.put(u, 1); // Mark GRAY (currently in recursion stack)

        for (long v : adj.getOrDefault(u, Collections.emptyList())) {
            int color = colors.getOrDefault(v, 0);
            if (color == 1) {
                return true; // Found BACK-EDGE: Cycle detected!
            }
            if (color == 0 && dfsCheckCycle(v, colors)) {
                return true;
            }
        }

        colors.put(u, 2); // Mark BLACK (fully explored)
        return false;
    }

    /**
     * Computes valid execution order using Kahn's In-Degree Algorithm.
     * Throws IllegalStateException if graph contains a cycle.
     */
    public List<Long> getTopologicalOrder() {
        if (hasCycle()) {
            throw new IllegalStateException("Cannot produce topological order: graph contains circular dependencies!");
        }

        Map<Long, Integer> inDegrees = new HashMap<>(this.inDegree);
        Queue<Long> queue = new ArrayDeque<>();

        for (long v : allVertices) {
            if (inDegrees.getOrDefault(v, 0) == 0) {
                queue.offer(v);
            }
        }

        List<Long> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            long u = queue.poll();
            order.add(u);

            for (long v : adj.getOrDefault(u, Collections.emptyList())) {
                int newDeg = inDegrees.get(v) - 1;
                inDegrees.put(v, newDeg);
                if (newDeg == 0) {
                    queue.offer(v);
                }
            }
        }

        if (order.size() != allVertices.size()) {
            throw new IllegalStateException("Topological sort failed to resolve all nodes");
        }
        return order;
    }

    public int vertexCount() { return allVertices.size(); }
}
