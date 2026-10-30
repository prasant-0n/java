package com.example.algo.cli;

import com.example.algo.dp.TaskResourceOptimizer;
import com.example.algo.engine.AlgorithmicWorkflowEngine;
import com.example.algo.model.Task;
import com.example.algo.model.TenantRequest;
import java.util.List;

/**
 * Terminal Command-Line Interface demonstrating the Algorithmic Workflow Engine.
 */
public class AlgorithmicEngineCli {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println(" High-Throughput Algorithmic Workflow Engine (Phase 09 Capstone) ");
        System.out.println("=================================================================");

        // 1. Initialize Engine (Rate limit: 3 requests / 1000 ms, Ring buffer: 16)
        AlgorithmicWorkflowEngine engine = new AlgorithmicWorkflowEngine(3, 1000L, 16);
        System.out.println("[INIT] Engine initialized with Sliding-Window Rate Limiter & Bitwise Ring Buffer.");

        // 2. Demonstrate Rate Limiting
        System.out.println("\n--- Step 1: Testing Sliding Window Rate Limiter ---");
        long now = 10000L;
        for (int i = 1; i <= 5; i++) {
            TenantRequest req = new TenantRequest("REQ-" + i, "tenant-alpha", now + (i * 50), "payload-" + i);
            boolean accepted = engine.submitRequest(req);
            System.out.printf("  Request %d: %s (Time: %d ms)%n", i, accepted ? "ACCEPTED" : "THROTTLED", req.timestampMs());
        }

        // 3. Demonstrate Task Dependency Graph & Topological Execution Planning
        System.out.println("\n--- Step 2: Task Dependency DAG & Topological Planning ---");
        Task t1 = engine.createTask("tenant-alpha", "ExtractData", 11000L, 128, 50);
        Task t2 = engine.createTask("tenant-alpha", "ValidateData", 11100L, 256, 80);
        Task t3 = engine.createTask("tenant-alpha", "TransformData", 11200L, 512, 120);
        Task t4 = engine.createTask("tenant-alpha", "LoadToWarehouse", 11300L, 256, 90);

        // Dependencies: t1 -> t2 -> t3 -> t4
        engine.addDependency(t1.taskId(), t2.taskId());
        engine.addDependency(t2.taskId(), t3.taskId());
        engine.addDependency(t3.taskId(), t4.taskId());

        List<Task> plan = engine.planDependencyExecution();
        System.out.println("  Topological execution plan: " + plan.stream().map(Task::name).toList());

        // 4. Demonstrate Priority Queue Dispatching
        System.out.println("\n--- Step 3: Priority-Queue-Based Task Dispatcher ---");
        engine.enqueueForExecution(t3);
        engine.enqueueForExecution(t1);
        engine.enqueueForExecution(t4);
        engine.enqueueForExecution(t2);

        long dispatchEpoch = 11150L;
        List<Task> ready = engine.dispatchReadyTasks(dispatchEpoch);
        System.out.printf("  Dispatched ready tasks at t=%d ms: %s%n",
            dispatchEpoch, ready.stream().map(Task::name).toList());

        // 5. Demonstrate 0/1 Knapsack Worker Memory Optimization
        System.out.println("\n--- Step 4: 0/1 Knapsack Worker Batch Optimization ---");
        int workerMemoryLimitMb = 600;
        TaskResourceOptimizer.OptimizationResult opt = engine.optimizeWorkerBatch(List.of(t1, t2, t3, t4), workerMemoryLimitMb);
        System.out.printf("  Worker Memory Budget: %d MB%n", workerMemoryLimitMb);
        System.out.printf("  Optimized Batch: Total Priority=%d, Used Memory=%d MB, Selected Tasks=%s%n",
            opt.totalPriorityWeight(), opt.totalMemoryCostMb(), opt.selectedTasks().stream().map(Task::name).toList());

        // 6. Demonstrate BST Range Queries
        System.out.println("\n--- Step 5: BST Time-Interval Range Query ---");
        List<Task> rangeTasks = engine.queryTasksInTimeWindow(11050L, 11250L);
        System.out.printf("  Tasks scheduled in [11050, 11250] ms: %s%n",
            rangeTasks.stream().map(Task::name).toList());

        // 7. Demonstrate Bisect Audit Log Binary Search
        System.out.println("\n--- Step 6: Bisect Binary Search on Execution Log ---");
        engine.recordExecution(t1.taskId(), 11010L, 1500000L, true);
        engine.recordExecution(t2.taskId(), 11120L, 3000000L, true);
        engine.recordExecution(t3.taskId(), 11230L, 5000000L, true);

        int count = engine.countExecutionsSince(11100L);
        System.out.printf("  Executions logged since 11100 ms: %d records%n", count);

        System.out.println("\n=================================================================");
        System.out.println(" Algorithmic Workflow Engine Simulation Completed Successfully! ");
        System.out.println("=================================================================");
    }
}
