package com.example.algo.test;

import com.example.algo.dp.TaskResourceOptimizer;
import com.example.algo.ds.BinarySearchTreeIndex;
import com.example.algo.ds.BitwiseRingBuffer;
import com.example.algo.ds.SentinelDoublyLinkedList;
import com.example.algo.ds.SeparateChainingMap;
import com.example.algo.ds.TaskMinHeap;
import com.example.algo.engine.AlgorithmicWorkflowEngine;
import com.example.algo.graph.TaskDependencyGraph;
import com.example.algo.limiter.SlidingWindowRateLimiter;
import com.example.algo.model.ExecutionMetrics;
import com.example.algo.model.Task;
import com.example.algo.model.TenantRequest;
import com.example.algo.search.BisectLogSearcher;
import com.example.algo.search.TaskSorter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Comprehensive 10-Point Integration Test Suite for the Phase 09 Capstone.
 * Executed with assertions enabled (-ea).
 */
public class AlgorithmicEngineIntegrationTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println(" Phase 09 Capstone 10-Point Integration Verification Test Suite  ");
        System.out.println("=================================================================");

        test1_BitwiseRingBuffer();
        test2_SentinelDoublyLinkedList();
        test3_SeparateChainingMap();
        test4_BinarySearchTreeRangeIndex();
        test5_TaskMinHeapAndTieBreaking();
        test6_TaskDependencyGraphAndCycleDetection();
        test7_SlidingWindowRateLimiter();
        test8_KnapsackResourceOptimization();
        test9_SortingAlgorithms();
        test10_EndToEndWorkflowEngine();

        System.out.println("\n=================================================================");
        System.out.println(" ALL 10 INTEGRATION TEST SUITES PASSED CLEANLY (EXIT CODE 0)     ");
        System.out.println("=================================================================");
    }

    private static void test1_BitwiseRingBuffer() {
        System.out.print("[TEST 1/10] Bitwise Ring Buffer wrap-around and doubling... ");
        BitwiseRingBuffer<String> buffer = new BitwiseRingBuffer<>(4);
        assert buffer.capacity() == 4 : "Initial capacity must round to 4";

        buffer.offer("A");
        buffer.offer("B");
        buffer.offer("C");
        assert buffer.size() == 3;

        assert "A".equals(buffer.poll());
        assert "B".equals(buffer.poll());
        assert buffer.size() == 1;

        // Wrap-around
        buffer.offer("D");
        buffer.offer("E");
        buffer.offer("F");
        assert buffer.size() == 4;

        // Trigger capacity doubling to 8
        buffer.offer("G");
        assert buffer.capacity() == 8 : "Buffer must double to capacity 8";
        assert buffer.size() == 5;

        assert "C".equals(buffer.poll());
        assert "D".equals(buffer.poll());
        assert "E".equals(buffer.poll());
        assert "F".equals(buffer.poll());
        assert "G".equals(buffer.poll());
        assert buffer.isEmpty() : "Buffer must be empty after draining";
        System.out.println("PASSED");
    }

    private static void test2_SentinelDoublyLinkedList() {
        System.out.print("[TEST 2/10] Sentinel Doubly Linked List and Fast/Slow Pointers... ");
        SentinelDoublyLinkedList<String> list = new SentinelDoublyLinkedList<>();
        list.addLast("Alpha");
        list.addLast("Beta");
        list.addLast("Gamma");
        list.addLast("Delta");
        list.addLast("Epsilon");

        assert list.size() == 5;
        // Fast/slow pointer middle node discovery
        String middle = list.findMiddleItem();
        assert "Gamma".equals(middle) : "Middle item of 5 elements must be Gamma";

        assert "Alpha".equals(list.removeFirst());
        assert "Epsilon".equals(list.removeLast());
        assert list.size() == 3;
        System.out.println("PASSED");
    }

    private static void test3_SeparateChainingMap() {
        System.out.print("[TEST 3/10] Separate Chaining Map with high-bit spreading... ");
        SeparateChainingMap<String, Integer> map = new SeparateChainingMap<>();

        for (int i = 0; i < 50; i++) {
            map.put("key-" + i, i * 10);
        }

        assert map.size() == 50 : "Map size must be 50";
        assert map.get("key-25") == 250 : "Lookup must return 250";
        assert map.capacity() > 16 : "Map must have resized";

        assert map.remove("key-25") == 250;
        assert map.get("key-25") == null : "Removed key must return null";
        assert map.size() == 49;
        System.out.println("PASSED");
    }

    private static void test4_BinarySearchTreeRangeIndex() {
        System.out.print("[TEST 4/10] BST Index insertion, Hibbard deletion & range queries... ");
        BinarySearchTreeIndex<Integer, String> bst = new BinarySearchTreeIndex<>();
        bst.insert(50, "Fifty");
        bst.insert(30, "Thirty");
        bst.insert(70, "Seventy");
        bst.insert(20, "Twenty");
        bst.insert(40, "Forty");
        bst.insert(60, "Sixty");
        bst.insert(80, "Eighty");

        assert bst.size() == 7;
        assert "Forty".equals(bst.find(40));

        // Range query [35, 75] -> 40, 50, 60, 70
        List<String> range = bst.queryRange(35, 75);
        assert range.size() == 4 : "Range [35, 75] must return 4 items";
        assert range.contains("Forty") && range.contains("Fifty") && range.contains("Sixty") && range.contains("Seventy");

        // Hibbard deletion on node with 2 children (50)
        boolean deleted = bst.delete(50);
        assert deleted : "Node 50 must be successfully deleted";
        assert bst.find(50) == null : "50 must no longer exist";
        assert bst.size() == 6;
        assert "Sixty".equals(bst.find(60)) : "Tree invariants must be preserved";
        System.out.println("PASSED");
    }

    private static void test5_TaskMinHeapAndTieBreaking() {
        System.out.print("[TEST 5/10] Task Min-Heap Priority Queue & FIFO Tie-Breaking... ");
        TaskMinHeap heap = new TaskMinHeap(8);

        // Same timestamp, different sequence numbers
        Task t1 = new Task(1L, "t-1", "Task1", 1000L, 1L, 100, 10, 5L);
        Task t2 = new Task(2L, "t-1", "Task2", 1000L, 2L, 100, 20, 5L);
        Task t3 = new Task(3L, "t-1", "Task3", 500L, 3L, 100, 30, 5L);

        heap.insert(t2);
        heap.insert(t1);
        heap.insert(t3);

        assert heap.poll().taskId() == 3L : "Task 3 has earliest timestamp (500L)";
        assert heap.poll().taskId() == 1L : "Task 1 has lower sequence number (1L) than Task 2 (2L)";
        assert heap.poll().taskId() == 2L : "Task 2 is polled last";
        assert heap.isEmpty();
        System.out.println("PASSED");
    }

    private static void test6_TaskDependencyGraphAndCycleDetection() {
        System.out.print("[TEST 6/10] Task DAG Topological Sort & Cycle Detection... ");
        TaskDependencyGraph graph = new TaskDependencyGraph();
        graph.addDependency(10L, 20L);
        graph.addDependency(20L, 30L);
        graph.addDependency(10L, 30L);

        assert !graph.hasCycle() : "Acyclic graph must have no cycles";
        List<Long> order = graph.getTopologicalOrder();
        assert order.equals(List.of(10L, 20L, 30L)) : "Order must be 10 -> 20 -> 30";

        // Add back-edge to create cycle: 30 -> 10
        TaskDependencyGraph cyclicGraph = new TaskDependencyGraph();
        cyclicGraph.addDependency(1L, 2L);
        cyclicGraph.addDependency(2L, 3L);
        cyclicGraph.addDependency(3L, 1L);

        assert cyclicGraph.hasCycle() : "Cyclic graph must be detected";
        try {
            cyclicGraph.getTopologicalOrder();
            assert false : "Must throw IllegalStateException on cyclic graph";
        } catch (IllegalStateException expected) {
            // Success
        }
        System.out.println("PASSED");
    }

    private static void test7_SlidingWindowRateLimiter() {
        System.out.print("[TEST 7/10] Sliding Window Rate Limiter Burst Protection... ");
        // Limit: 3 requests per 1000 ms window
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, 1000L);

        assert limiter.allowRequest("tenant-A", 100L);
        assert limiter.allowRequest("tenant-A", 200L);
        assert limiter.allowRequest("tenant-A", 300L);
        assert !limiter.allowRequest("tenant-A", 400L) : "4th request at 400ms must be throttled";

        // Request at 1101ms (100ms has expired from window)
        assert limiter.allowRequest("tenant-A", 1101L) : "Request at 1101ms must be allowed";
        // Distinct tenant must be isolated
        assert limiter.allowRequest("tenant-B", 1101L) : "Tenant B must not be throttled";
        System.out.println("PASSED");
    }

    private static void test8_KnapsackResourceOptimization() {
        System.out.print("[TEST 8/10] 0/1 Knapsack Task Resource Optimization... ");
        List<Task> candidates = List.of(
            new Task(1L, "t-1", "ETL", 100, 60, 100, 60, 5L),
            new Task(2L, "t-1", "Report", 200, 100, 200, 100, 5L),
            new Task(3L, "t-1", "Backup", 300, 120, 300, 120, 5L)
        );
        int workerMemoryCapacity = 500;

        TaskResourceOptimizer.OptimizationResult result =
            TaskResourceOptimizer.optimizeBatch(candidates, workerMemoryCapacity);

        assert result.totalPriorityWeight() == 220 : "Max priority must be 220 (100 + 120)";
        assert result.totalMemoryCostMb() == 500 : "Total memory cost must be 500 MB";
        assert result.selectedTasks().size() == 2;

        int spaceOptWeight = TaskResourceOptimizer.optimizeBatchSpaceEfficient(candidates, workerMemoryCapacity);
        assert spaceOptWeight == result.totalPriorityWeight() : "1D rolling array value must match 2D table";
        System.out.println("PASSED");
    }

    private static void test9_SortingAlgorithms() {
        System.out.print("[TEST 9/10] Dual-Pivot QuickSort and Stable MergeSort... ");
        // 1. Primitive Dual-Pivot QuickSort
        long[] timestamps = { 900L, 100L, 500L, 200L, 800L, 300L, 700L, 400L, 600L };
        TaskSorter.dualPivotQuickSort(timestamps, 0, timestamps.length - 1);
        for (int i = 0; i < timestamps.length - 1; i++) {
            assert timestamps[i] <= timestamps[i + 1] : "Array must be sorted in ascending order";
        }

        // 2. Stable MergeSort on composite records
        List<Task> tasks = new ArrayList<>(List.of(
            new Task(1L, "t-1", "TaskA", 1000L, 1L, 100, 50, 5L),
            new Task(2L, "t-1", "TaskB", 1000L, 2L, 200, 50, 5L),
            new Task(3L, "t-1", "TaskC", 500L, 3L, 300, 80, 5L)
        ));
        TaskSorter.mergeSort(tasks, Comparator.comparingLong(Task::scheduledEpochMs));
        assert tasks.get(0).taskId() == 3L : "TaskC (500L) must be first";
        assert tasks.get(1).taskId() == 1L : "TaskA must precede TaskB due to stable sorting";
        assert tasks.get(2).taskId() == 2L : "TaskB must be third";
        System.out.println("PASSED");
    }

    private static void test10_EndToEndWorkflowEngine() {
        System.out.print("[TEST 10/10] End-to-End Algorithmic Workflow Engine... ");
        AlgorithmicWorkflowEngine engine = new AlgorithmicWorkflowEngine(5, 1000L, 32);

        // 1. Rate-limited request submission
        TenantRequest req1 = new TenantRequest("R1", "tenant-alpha", 1000L, "payload");
        assert engine.submitRequest(req1) : "Request 1 must be accepted";

        // 2. Task lifecycle
        Task taskA = engine.createTask("tenant-alpha", "Init", 2000L, 64, 40);
        Task taskB = engine.createTask("tenant-alpha", "Process", 2100L, 128, 80);
        engine.addDependency(taskA.taskId(), taskB.taskId());

        List<Task> executionPlan = engine.planDependencyExecution();
        assert executionPlan.size() == 2;
        assert executionPlan.get(0).taskId() == taskA.taskId();
        assert executionPlan.get(1).taskId() == taskB.taskId();

        // 3. Priority Queue Dispatch
        engine.enqueueForExecution(taskB);
        engine.enqueueForExecution(taskA);

        List<Task> dispatched = engine.dispatchReadyTasks(2050L);
        assert dispatched.size() == 1 : "Only taskA (2000L) should be ready at 2050L";
        assert dispatched.get(0).taskId() == taskA.taskId();

        // 4. Audit Log and Bisect Binary Search
        engine.recordExecution(taskA.taskId(), 2010L, 500000L, true);
        engine.recordExecution(taskB.taskId(), 2110L, 800000L, true);
        assert engine.countExecutionsSince(2050L) == 1 : "Exactly 1 execution since 2050L";
        assert engine.countExecutionsSince(2000L) == 2 : "Exactly 2 executions since 2000L";

        System.out.println("PASSED");
    }
}
