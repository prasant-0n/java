package com.example.algo.engine;

import com.example.algo.dp.TaskResourceOptimizer;
import com.example.algo.ds.BinarySearchTreeIndex;
import com.example.algo.ds.BitwiseRingBuffer;
import com.example.algo.ds.TaskMinHeap;
import com.example.algo.graph.TaskDependencyGraph;
import com.example.algo.limiter.SlidingWindowRateLimiter;
import com.example.algo.model.ExecutionMetrics;
import com.example.algo.model.Task;
import com.example.algo.model.TenantRequest;
import com.example.algo.search.BisectLogSearcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unified Algorithmic Workflow Engine integrating Rate Limiting, Graph Dependency Scheduling,
 * Min-Heap Priority Dispatching, Dynamic Resource Optimization, and BST Range Indexing.
 */
public class AlgorithmicWorkflowEngine {

    private final SlidingWindowRateLimiter rateLimiter;
    private final BitwiseRingBuffer<TenantRequest> ingestionBuffer;
    private final TaskDependencyGraph dependencyGraph = new TaskDependencyGraph();
    private final Map<Long, Task> taskRegistry = new HashMap<>();
    private final TaskMinHeap scheduledPriorityQueue = new TaskMinHeap(16);
    private final BinarySearchTreeIndex<Long, Task> timeIndex = new BinarySearchTreeIndex<>();
    private final List<ExecutionMetrics> executionAuditLog = new ArrayList<>();
    private final AtomicLong sequenceGenerator = new AtomicLong(0);

    public AlgorithmicWorkflowEngine(int rateLimitPerWindow, long windowMs, int bufferCapacity) {
        this.rateLimiter = new SlidingWindowRateLimiter(rateLimitPerWindow, windowMs);
        this.ingestionBuffer = new BitwiseRingBuffer<>(bufferCapacity);
    }

    /**
     * Ingests an inbound tenant request through the sliding-window rate limiter.
     * Returns true if accepted into ingestion buffer, false if throttled.
     */
    public boolean submitRequest(TenantRequest request) {
        if (!rateLimiter.allowRequest(request.tenantId(), request.timestampMs())) {
            return false; // Throttled
        }
        return ingestionBuffer.offer(request);
    }

    /**
     * Registers a new background task.
     */
    public Task createTask(String tenantId, String name, long scheduledEpochMs, int memoryCostMb, int priorityWeight) {
        long seq = sequenceGenerator.incrementAndGet();
        Task task = new Task(seq, tenantId, name, scheduledEpochMs, seq, memoryCostMb, priorityWeight, 10L);
        taskRegistry.put(task.taskId(), task);
        timeIndex.insert(task.scheduledEpochMs(), task);
        dependencyGraph.addVertex(task.taskId());
        return task;
    }

    /**
     * Declares a dependency: parentTaskId must complete BEFORE childTaskId can run.
     */
    public void addDependency(long parentTaskId, long childTaskId) {
        dependencyGraph.addDependency(parentTaskId, childTaskId);
    }

    /**
     * Computes valid topological execution order for registered tasks.
     * Throws IllegalStateException if a cycle is present.
     */
    public List<Task> planDependencyExecution() {
        List<Long> orderedIds = dependencyGraph.getTopologicalOrder();
        List<Task> orderedTasks = new ArrayList<>(orderedIds.size());
        for (long id : orderedIds) {
            Task t = taskRegistry.get(id);
            if (t != null) {
                orderedTasks.add(t);
            }
        }
        return orderedTasks;
    }

    /**
     * Schedules a task into the Min-Heap priority queue.
     */
    public void enqueueForExecution(Task task) {
        scheduledPriorityQueue.insert(task);
    }

    /**
     * Dispatches tasks from the priority queue that are ready to run at or before currentTimestamp.
     */
    public List<Task> dispatchReadyTasks(long currentTimestamp) {
        List<Task> ready = new ArrayList<>();
        while (!scheduledPriorityQueue.isEmpty()) {
            Task top = scheduledPriorityQueue.peek();
            if (top.scheduledEpochMs() <= currentTimestamp) {
                ready.add(scheduledPriorityQueue.poll());
            } else {
                break;
            }
        }
        return ready;
    }

    /**
     * Optimizes task batch allocation for worker memory limits using 0/1 Knapsack.
     */
    public TaskResourceOptimizer.OptimizationResult optimizeWorkerBatch(List<Task> candidates, int workerCapacityMb) {
        return TaskResourceOptimizer.optimizeBatch(candidates, workerCapacityMb);
    }

    /**
     * Queries tasks scheduled within a time window [startEpoch, endEpoch] using BST subtree pruning.
     */
    public List<Task> queryTasksInTimeWindow(long startEpoch, long endEpoch) {
        return timeIndex.queryRange(startEpoch, endEpoch);
    }

    /**
     * Records execution completion and appends to sorted audit log.
     */
    public void recordExecution(long taskId, long executedTimestamp, long durationNanos, boolean success) {
        executionAuditLog.add(new ExecutionMetrics(taskId, executedTimestamp, durationNanos, success));
        Collections.sort(executionAuditLog); // Maintain sorted order for bisect searches
    }

    /**
     * Audits executions occurring at or after startTimestampMs using Bisect Binary Search.
     */
    public int countExecutionsSince(long startTimestampMs) {
        int idx = BisectLogSearcher.bisectLowerBound(executionAuditLog, startTimestampMs);
        return executionAuditLog.size() - idx;
    }

    public int getAuditLogSize() {
        return executionAuditLog.size();
    }
}
