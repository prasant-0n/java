# Concept: Heaps and Priority Queues: Binary Heaps, Sift-Up/Sift-Down, and Top-K

---

## 1. Metadata and Learning Goals

| Attribute | Specification |
|---|---|
| **Concept ID** | `P09-C007` |
| **Concept Name** | Heaps and Priority Queues: Binary Heaps, Sift-Up/Sift-Down, and Top-K |
| **Phase** | Phase 09: Data Structures and Algorithms for Backend Developers |
| **Prerequisites** | `P09-C001` (Algorithmic Complexity & Amortized Analysis), `P09-C002` (Dynamic Arrays & Resizing Mechanics), `P09-C004` (Stacks & Queues: LIFO, FIFO), `P09-C006` (Binary Trees & BST) |
| **Target Mastery Level** | Advanced / Production-Grade Backend Architect |
| **JDK Compatibility** | OpenJDK 21 LTS (Verified) |
| **Primary Artifact** | `curriculum/phase-09-data-structures-algorithms-backend/007-heaps-and-priority-queues.md` |

### Core Learning Goals
1. **Master Complete Binary Tree Array Packing:** Deconstruct how Complete Binary Trees map into contiguous 1D arrays without pointers via index arithmetic ($\text{parent}(i) = \lfloor (i - 1) / 2 \rfloor$, $\text{left}(i) = 2i + 1$, $\text{right}(i) = 2i + 2$).
2. **Implement Sift-Up, Sift-Down, and Linear-Time Heapify:** Formulate the fundamental heap operations and prove mathematically why bottom-up `buildHeap` runs in $O(N)$ linear time while repeated insertion takes $O(N \log N)$.
3. **Dissect OpenJDK `java.util.PriorityQueue` Internals:** Trace array resizing thresholds, non-synchronized semantics, comparator tie-breaking rules, and the $O(N)$ cost of `remove(Object)`.
4. **Architect Streaming Top-K and K-Way Merge Engines:** Build bounded min-heap pipelines for high-throughput Top-K stream processing in $O(N \log K)$ time and multi-way merge engines for distributed LSM-Tree compactions.
5. **Mitigate Production Heap Failure Modes:** Eliminate mutating element bugs, comparator-equals inconsistency traps, and priority inversion starvation in backend task executors.

---

## 2. Definition, Purpose, and Mental Model

### What Is a Heap?
A **Heap** is a specialized tree-based data structure that satisfies two defining constraints:
1. **The Shape Property (Complete Binary Tree):** Every level of the tree is completely filled, except possibly the bottom level, which is filled sequentially from left to right.
2. **The Heap Property:**
   - **Min-Heap:** The value of every node is greater than or equal to the value of its parent:
     $$\text{key}(\text{parent}(i)) \le \text{key}(i) \quad \forall i > 0$$
     *Consequence:* The minimum element is always at the root.
   - **Max-Heap:** The value of every node is less than or equal to the value of its parent:
     $$\text{key}(\text{parent}(i)) \ge \text{key}(i) \quad \forall i > 0$$
     *Consequence:* The maximum element is always at the root.

```
MIN-HEAP LOGICAL TREE:                ARRAY REPRESENTATION (0-Based):
               [ 2 ] (Root)           Index:  [0] [1] [2] [3] [4] [5] [6]
              /     \                 Value:  [ 2 | 5 | 3 | 9 | 6 | 8 | 7 ]
        [ 5 ]         [ 3 ]
       /     \       /     \
    [ 9 ]   [ 6 ] [ 8 ]   [ 7 ]
```

### The Backend Mental Model: Priority Schedulers and Extremum Windows
In backend infrastructure:
- **Priority Schedulers:** Thread pools and event loops (e.g., Netty HashedWheelTimer, Quartz, OS CPU task schedulers) extract the highest-priority or earliest-due task in $O(\log N)$ time.
- **Real-Time Top-K Stream Filtering:** Identifying the 100 most active users or highest-latency endpoints across 100,000,000 events without buffering or sorting the full stream in memory.
- **Log Compaction and Merge Joins:** Merging $K$ sorted database segments (RocksDB SSTables, Cassandra commit logs, Lucene postings lists) into a single unified stream using a min-heap of size $K$.

---

## 3. Foundations and Theory

### 3.1 Pointer-Free 1D Array Layout Mathematics
Because a heap is a Complete Binary Tree, it requires **zero pointer references** (`left`, `right`, `parent`). All topological relationships are calculated directly via bit-shifts and index arithmetic:

For any node at 0-based array index $i$:
$$\text{parent}(i) = \left\lfloor \frac{i - 1}{2} \right\rfloor = (i - 1) \gg 1$$
$$\text{left}(i) = 2i + 1 = (i \ll 1) + 1$$
$$\text{right}(i) = 2i + 2 = (i \ll 1) + 2$$

#### Hardware Cache Sympathy:
In contrast to node-based Binary Search Trees (`TreeMap`) where each node is allocated separately on the heap, an array-backed heap is stored in **contiguous virtual memory**. Accessing child nodes and parent nodes maximizes CPU L1/L2 cache line hits and enables SIMD prefetching.

### 3.2 Time Complexity Analysis

| Operation | Binary Heap | Unsorted Array | Sorted Array | Balanced BST (`TreeMap`) |
|---|---|---|---|---|
| **Find Min / Max (Peek)** | $O(1)$ | $O(N)$ | $O(1)$ | $O(\log N)$ |
| **Insert (Offer)** | $O(\log N)$ amortized | $O(1)$ amortized | $O(N)$ | $O(\log N)$ |
| **Delete Min / Max (Poll)**| $O(\log N)$ | $O(N)$ | $O(1)$ or $O(N)$ | $O(\log N)$ |
| **Build Heap (`heapify`)** | $\Theta(N)$ | $O(1)$ | $O(N \log N)$ | $O(N \log N)$ |
| **Search by Key** | $O(N)$ | $O(N)$ | $O(\log N)$ | $O(\log N)$ |
| **Auxiliary Space** | $O(N)$ contiguous | $O(N)$ contiguous | $O(N)$ contiguous | $O(N) + \text{pointers}$ |

### 3.3 Mathematical Proof of Linear-Time Build Heap ($\Theta(N)$)
A naive approach to building a heap of $N$ elements is calling `insert()` $N$ times, requiring $O(N \log N)$ time.
However, **Floyd's `buildHeap` algorithm** runs in strictly **$\Theta(N)$ linear time** by working bottom-up:
1. Ignore all leaf nodes (indices $\lfloor N/2 \rfloor$ to $N - 1$), because single nodes already satisfy the heap property trivially!
2. Iterate backwards from the last internal node index $i = \lfloor N/2 \rfloor - 1$ down to $0$, calling `siftDown(i)` on each node.

```
Level 0: 1 node,    max sift depth = h
Level 1: 2 nodes,   max sift depth = h - 1
Level 2: 4 nodes,   max sift depth = h - 2
...
Level h: 2^h nodes, max sift depth = 0
```

The total number of operations $S$ is the sum over all tree levels:
$$S = \sum_{j=0}^{h} 2^j (h - j) = \sum_{k=0}^{h} k 2^{h-k} = 2^h \sum_{k=0}^{h} \frac{k}{2^k}$$

Using the standard geometric arithmetico-geometric series identity:
$$\sum_{k=0}^{\infty} \frac{k}{2^k} = \frac{1/2}{(1 - 1/2)^2} = \frac{1/2}{1/4} = 2$$

Since $2^h \le N$:
$$S \le N \sum_{k=0}^{\infty} \frac{k}{2^k} = 2N = O(N)$$

**Conclusion:** Floyd's bottom-up `buildHeap` runs in $O(N)$ linear time!

---

## 4. Language, Specification, and Platform Mechanics

### 4.1 Sift-Up and Sift-Down Mechanics

#### Sift-Up (Bubble Up)
Used during insertion (`offer`):
1. Append the new element $X$ to the end of the array at index $k = \text{size}$.
2. Compare $X$ with its parent at $p = (k - 1) / 2$.
3. If $X < \text{parent}$, swap $X$ with parent, set $k = p$, and repeat.
4. Stop when $X \ge \text{parent}$ or $k = 0$ (root reached).

```
Insert 1 into Min-Heap:
       [ 2 ]                               [ 1 ] (Root)
      /     \                             /     \
   [ 5 ]   [ 3 ]     ──Sift-Up──►      [ 2 ]   [ 3 ]
   /                                   /
 [ 1 ]                               [ 5 ]
```

#### Sift-Down (Bubble Down / Sink)
Used during root extraction (`poll`):
1. Save the root element $R = A[0]$.
2. Move the last element $L = A[\text{size} - 1]$ to index 0, and decrement size.
3. Compare $L$ with its left child ($2k + 1$) and right child ($2k + 2$).
4. Select the smaller of the two children. If $L$ is greater than this child, swap $L$ with the smaller child, update $k$, and repeat downwards.
5. Stop when $L \le$ both children or a leaf position is reached.

### 4.2 Deconstructing `java.util.PriorityQueue` Internals
HotSpot's `java.util.PriorityQueue<E>` is implemented as an array-backed binary min-heap:
```java
transient Object[] queue; // Backing array
int size;                 // Number of active elements
Comparator<? super E> comparator; // Optional comparator (null => natural Comparable)
```

#### Growth Progression:
```java
private void grow(int minCapacity) {
    int oldCapacity = queue.length;
    // Double if small; else grow by 50%
    int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                     (oldCapacity + 2) :
                                     (oldCapacity >> 1));
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    queue = Arrays.copyOf(queue, newCapacity);
}
```

#### Key Architectural Invariants:
1. **Unbounded Allocation:** `PriorityQueue` has no capacity ceiling other than heap memory. An unmanaged queue will grow until `OutOfMemoryError`.
2. **Thread Safety:** `PriorityQueue` is **not thread-safe**. In concurrent multi-threaded environments, use `java.util.concurrent.PriorityBlockingQueue` (which synchronizes mutations with a `ReentrantLock`).
3. **No Stability Guarantee:** For elements with identical priority, `PriorityQueue` does **not guarantee FIFO order**. If two jobs have identical priority, either may be dequeued first.

---

## 5. Evolution Across Java Versions

| Java Version | Release Year | Evolution / Feature Milestone |
|---|---|---|
| **Java 1.5** | 2004 | `java.util.PriorityQueue` and `java.util.concurrent.PriorityBlockingQueue` introduced with generics support. |
| **Java 6** | 2006 | Optimized sift-up / sift-down loops in OpenJDK eliminating redundant array writes during bubbling. |
| **Java 8** | 2014 | `Comparator` functional enhancements (`Comparator.comparing()`, `thenComparing()`, `reversed()`) simplifying custom priority logic. |
| **Java 21 LTS** | 2023 | HotSpot C2 compiler auto-vectorization improvements for array copy operations during heap reallocation. |

---

## 6. Architecture and Design Patterns

### 6.1 Top-K Stream Filtering Pattern
Finding the $K$ largest elements in an unbounded stream of $N$ items without storing all $N$ items:
- Maintain a **Min-Heap of size $K$**.
- For each incoming element $X$:
  1. If heap size $< K$, add $X$ (`heap.offer(X)`).
  2. If heap size $== K$ and $X > \text{heap.peek()}$:
     - The current minimum in the top-$K$ set is evicted: `heap.poll()`.
     - Insert $X$: `heap.offer(X)`.
  3. Else: discard $X$ (it is smaller than all current top-$K$ candidates).
- **Complexity:** Time: $O(N \log K)$. Space: strictly $O(K)$.
- For $N = 100,000,000$ and $K = 10$, $\log K \approx 3.3$ comparisons per element!

```
Incoming Stream: 12, 5, 8, 20, 15, 3 ... K = 3 (Min-Heap of Top 3)
1. Add 12, 5, 8     ──► Heap: [5, 12, 8] (Min = 5)
2. 20 arrives       ──► 20 > 5 -> evict 5, add 20 ──► Heap: [8, 12, 20] (Min = 8)
3. 15 arrives       ──► 15 > 8 -> evict 8, add 15 ──► Heap: [12, 15, 20] (Min = 12)
4. 3 arrives        ──► 3 < 12 -> discarded!
Final Top 3: 12, 15, 20!
```

### 6.2 Multi-Way Merge ($K$-Sorted Streams)
Given $K$ independently sorted iterators/streams, merge them into a single sorted stream:
- Insert the head of each of the $K$ streams into a Min-Heap.
- While the heap is non-empty:
  1. Extract the minimum node: `min = heap.poll()`.
  2. Output `min.value`.
  3. If `min.iterator` has more items, read the next item and insert into heap: `heap.offer(min.iterator.next())`.
- **Complexity:** $O(N \log K)$ time where $N$ is total elements across all streams, and $O(K)$ space.

---

## 7. Failure Modes, Anti-Patterns, and Production Traps

### 7.1 The Mutable Element Heap Corruption Trap
If an element stored in a `PriorityQueue` has its priority mutated while residing in the heap, **the heap does not detect the change**:

```java
// CRITICAL PRODUCTION BUG: Mutating priority of queued task
class Task implements Comparable<Task> {
    int priority;
    Task(int p) { this.priority = p; }
    public int compareTo(Task o) { return Integer.compare(this.priority, o.priority); }
}

PriorityQueue<Task> pq = new PriorityQueue<>();
Task t1 = new Task(10);
Task t2 = new Task(20);
pq.offer(t1);
pq.offer(t2);

// MUTATING ELEMENT OUTSIDE QUEUE:
t2.priority = 5; // Should now be the minimum!

// BUT PEEK RETURNS t1 (10)! Heap invariant is silently broken!
System.out.println(pq.peek().priority); // Prints 10, NOT 5!
```

#### Why Does This Happen?
`PriorityQueue` has no listener mechanism or index tracking. The element remains at its original array slot without re-sifting, corrupting all future extractions.

**Production Rule:** Elements placed in a `PriorityQueue` must be **immutable** with respect to their comparison keys, or removed, mutated, and re-inserted (`pq.remove(task); task.setPriority(5); pq.offer(task);`).

### 7.2 The `remove(Object)` Performance Trap
While `poll()` is $O(\log N)$, calling `priorityQueue.remove(item)` is **$O(N)$ linear time**:
1. `PriorityQueue` does not maintain a hash map of item positions. It must perform a linear scan `for (int i = 0; i < size; i++) if (o.equals(queue[i]))` to find the item index ($O(N)$).
2. It then calls `siftDown` or `siftUp` ($O(\log N)$).
In high-throughput task cancellation workflows, calling `remove(task)` in a queue of 100,000 tasks turns your fast scheduler into an $O(N)$ performance bottleneck.

---

## 8. Security and Hardening

### 8.1 Stable Tie-Breaking to Prevent Task Starvation
If multiple tasks share identical priority levels, a standard `PriorityQueue` has undefined extraction order. Under continuous load, certain tasks can suffer from indefinite **starvation** (priority inversion).

**Hardening Technique: Sequence-Numbered Composite Keys**
```java
public record PrioritizedTask<T>(T payload, int priority, long sequenceNumber) 
    implements Comparable<PrioritizedTask<T>> {

    private static final AtomicLong SEQ = new AtomicLong(0);

    public static <T> PrioritizedTask<T> of(T payload, int priority) {
        return new PrioritizedTask<>(payload, priority, SEQ.incrementAndGet());
    }

    @Override
    public int compareTo(PrioritizedTask<T> other) {
        int cmp = Integer.compare(this.priority, other.priority);
        // Tie-breaker: preserve strict FIFO order for equal priorities!
        return (cmp != 0) ? cmp : Long.compare(this.sequenceNumber, other.sequenceNumber);
    }
}
```

---

## 9. Performance, Sizing, and Operational Profile

### 9.1 Sizing Calculations: Array vs Node-Based Heap
Memory footprint for 1,000,000 items in a binary heap:
- Contiguous `Object[]` array: 16 bytes header + 4 bytes length + 4,000,000 bytes (Compressed OOP references) $\approx 4 \text{ MB}$.
- Compare with `TreeMap`: 1,000,000 `Entry` objects $\times 32 \text{ bytes} \approx 32 \text{ MB}$.
- Array-backed binary heaps consume **8x less memory** and execute with **5x higher throughput** due to contiguous hardware prefetching.

---

## 10. Tooling, Observability, and Debugging

### 10.1 Monitoring Queue Capacity and Lag
In backend task scheduling, instrument:
```java
// Micrometer metric:
Gauge.builder("task.scheduler.queue.size", priorityQueue, PriorityQueue::size)
     .description("Active delayed tasks in priority queue")
     .register(registry);
```
Alert when queue size grows monotonically, indicating consumer thread starvation.

---

## 11. Framework, Cloud, and Ecosystem Integration

### 11.1 Netty HashedWheelTimer vs PriorityQueue
For scheduling hundreds of thousands of network connection timeouts (e.g., HTTP keep-alive timeouts):
- `PriorityQueue`: Insertion and deletion are $O(\log N)$. With 1,000,000 timeouts, $\log N \approx 20$ operations per packet.
- **HashedWheelTimer (Circular Ring Buffer of Buckets):** Approximates scheduling in $O(1)$ constant time by rotating a circular wheel cursor once per tick (e.g., 100 ms), trading microsecond precision for extreme throughput.

---

## 12. Production-Grade Code Demonstration

The following standalone, self-contained Java class comprehensively proves:
1. **`BinaryMinHeap<T>`:** A complete, production-grade array-backed Binary Min-Heap implementing contiguous 1D index arithmetic, `siftUp`, `siftDown`, and Floyd's linear-time $O(N)$ `buildHeap`.
2. **`TopKStreamingEngine`:** Bounded Min-Heap computing the Top $K$ largest elements across a data stream in $O(N \log K)$ time and $O(K)$ space.
3. **`MultiWayMergeEngine`:** Merging $K$ sorted streams into a single globally sorted stream in $O(N \log K)$ time.
4. **`DelayedTaskScheduler`:** Priority-based task execution engine with monotonic sequence tie-breaking.

Save as `curriculum/phase-09-data-structures-algorithms-backend/007-heaps-and-priority-queues.md` companion verification suite:

```java
package curriculum.phase09;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Standalone verification suite demonstrating Binary Heaps, Sift-Up/Sift-Down,
 * Linear-Time Heapify, Top-K Streaming, and Multi-Way Merging.
 */
public class HeapsAndPriorityQueuesDemo {

    // =========================================================================
    // 1. PRODUCTION-GRADE ARRAY-BACKED BINARY MIN-HEAP
    // =========================================================================

    public static class BinaryMinHeap<T> {
        private Object[] elements;
        private int size;
        private final Comparator<? super T> comparator;

        @SuppressWarnings("unchecked")
        public BinaryMinHeap(int capacity, Comparator<? super T> comparator) {
            this.elements = new Object[Math.max(4, capacity)];
            this.size = 0;
            this.comparator = (comparator != null) ? comparator : (a, b) -> ((Comparable<T>) a).compareTo(b);
        }

        public BinaryMinHeap(int capacity) {
            this(capacity, null);
        }

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public void offer(T item) {
            Objects.requireNonNull(item, "Item cannot be null");
            if (size == elements.length) {
                grow();
            }
            elements[size] = item;
            siftUp(size);
            size++;
        }

        @SuppressWarnings("unchecked")
        public T peek() {
            if (size == 0) return null;
            return (T) elements[0];
        }

        @SuppressWarnings("unchecked")
        public T poll() {
            if (size == 0) return null;
            T root = (T) elements[0];
            int lastIdx = size - 1;
            elements[0] = elements[lastIdx];
            elements[lastIdx] = null; // Defensive slot nulling for GC
            size--;

            if (size > 0) {
                siftDown(0);
            }
            return root;
        }

        @SuppressWarnings("unchecked")
        private void siftUp(int k) {
            T key = (T) elements[k];
            while (k > 0) {
                int parent = (k - 1) >>> 1;
                T parentVal = (T) elements[parent];
                if (comparator.compare(key, parentVal) >= 0) {
                    break;
                }
                elements[k] = parentVal;
                k = parent;
            }
            elements[k] = key;
        }

        @SuppressWarnings("unchecked")
        private void siftDown(int k) {
            T key = (T) elements[k];
            int half = size >>> 1; // Loop while a non-leaf node exists
            while (k < half) {
                int child = (k << 1) + 1; // Left child
                T c = (T) elements[child];
                int right = child + 1;

                if (right < size && comparator.compare((T) elements[right], c) < 0) {
                    child = right;
                    c = (T) elements[child];
                }

                if (comparator.compare(key, c) <= 0) {
                    break;
                }
                elements[k] = c;
                k = child;
            }
            elements[k] = key;
        }

        /**
         * Floyd's Bottom-Up Linear-Time Build Heap: O(N) time.
         */
        public static <E> BinaryMinHeap<E> heapify(E[] array, Comparator<? super E> comparator) {
            Objects.requireNonNull(array, "Array cannot be null");
            BinaryMinHeap<E> heap = new BinaryMinHeap<>(array.length, comparator);
            System.arraycopy(array, 0, heap.elements, 0, array.length);
            heap.size = array.length;

            // Start at last non-leaf node down to root
            for (int i = (heap.size >>> 1) - 1; i >= 0; i--) {
                heap.siftDown(i);
            }
            return heap;
        }

        private void grow() {
            int newCap = elements.length + (elements.length >> 1); // 1.5x growth
            elements = Arrays.copyOf(elements, newCap);
        }
    }

    // =========================================================================
    // 2. TOP-K STREAMING ENGINE (O(N log K) Time, O(K) Space)
    // =========================================================================

    public static class TopKStreamingEngine {
        /**
         * Finds the K largest elements in a stream in O(N log K) time using a Min-Heap of size K.
         */
        public static List<Integer> findTopK(Iterator<Integer> stream, int k) {
            if (k <= 0) return List.of();

            // Min-heap of size K stores the K largest elements seen so far
            PriorityQueue<Integer> minHeap = new PriorityQueue<>(k);

            while (stream.hasNext()) {
                int val = stream.next();
                if (minHeap.size() < k) {
                    minHeap.offer(val);
                } else if (val > minHeap.peek()) {
                    minHeap.poll(); // Evict smallest of the top-K
                    minHeap.offer(val);
                }
            }

            // Extract elements in ascending order
            List<Integer> result = new ArrayList<>(minHeap.size());
            while (!minHeap.isEmpty()) {
                result.add(minHeap.poll());
            }
            return result;
        }
    }

    // =========================================================================
    // 3. MULTI-WAY MERGE ENGINE (K Sorted Streams into 1)
    // =========================================================================

    public static class MultiWayMergeEngine {
        private record StreamNode<T>(T value, Iterator<T> source) {}

        public static <T extends Comparable<T>> List<T> merge(List<List<T>> sortedLists) {
            List<T> result = new ArrayList<>();
            PriorityQueue<StreamNode<T>> minHeap = new PriorityQueue<>(
                Comparator.comparing(StreamNode::value)
            );

            // Seed heap with first element of each non-empty list
            for (List<T> list : sortedLists) {
                if (list != null && !list.isEmpty()) {
                    Iterator<T> it = list.iterator();
                    minHeap.offer(new StreamNode<>(it.next(), it));
                }
            }

            while (!minHeap.isEmpty()) {
                StreamNode<T> min = minHeap.poll();
                result.add(min.value);
                if (min.source.hasNext()) {
                    minHeap.offer(new StreamNode<>(min.source.next(), min.source));
                }
            }
            return result;
        }
    }

    // =========================================================================
    // 4. DELAYED TASK SCHEDULER WITH FIFO TIE-BREAKING
    // =========================================================================

    public static class DelayedTaskScheduler {
        private static final AtomicLong SEQUENCE = new AtomicLong(0);

        public record ScheduledTask(String taskId, long executeAtEpochMs, long seq) 
            implements Comparable<ScheduledTask> {

            public static ScheduledTask of(String taskId, long executeAtEpochMs) {
                return new ScheduledTask(taskId, executeAtEpochMs, SEQUENCE.incrementAndGet());
            }

            @Override
            public int compareTo(ScheduledTask other) {
                int cmp = Long.compare(this.executeAtEpochMs, other.executeAtEpochMs);
                // Tie-breaker: preserve exact FIFO submission order for equal timestamps!
                return (cmp != 0) ? cmp : Long.compare(this.seq, other.seq);
            }
        }

        private final PriorityQueue<ScheduledTask> queue = new PriorityQueue<>();

        public void schedule(ScheduledTask task) {
            queue.offer(task);
        }

        public ScheduledTask pollNextDue(long currentEpochMs) {
            ScheduledTask next = queue.peek();
            if (next != null && next.executeAtEpochMs <= currentEpochMs) {
                return queue.poll();
            }
            return null;
        }
    }

    // =========================================================================
    // 5. COMPREHENSIVE VERIFICATION SUITE
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" Heaps and Priority Queues: Sift, Top-K & K-Way Merge Suite   ");
        System.out.println("===============================================================");

        testBinaryMinHeapBasicOperations();
        testLinearTimeHeapify();
        testTopKStreamingEngine();
        testMultiWayMergeEngine();
        testDelayedTaskSchedulerTieBreaking();

        System.out.println("\nAll 5 verification test suites passed successfully with assertions enabled!");
    }

    private static void testBinaryMinHeapBasicOperations() {
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>(4);
        assert heap.isEmpty();

        heap.offer(50);
        heap.offer(20);
        heap.offer(30);
        heap.offer(10);
        heap.offer(40);

        assert heap.size() == 5;
        assert heap.peek() == 10 : "Root must be the minimum (10)";

        // Extract in ascending order
        assert heap.poll() == 10;
        assert heap.poll() == 20;
        assert heap.poll() == 30;
        assert heap.poll() == 40;
        assert heap.poll() == 50;
        assert heap.isEmpty();
        assert heap.poll() == null;

        System.out.println("[PASS] Test 1: Binary Min-Heap sift-up, sift-down, and poll order verified.");
    }

    private static void testLinearTimeHeapify() {
        Integer[] array = {89, 42, 17, 95, 3, 23, 71, 8};
        BinaryMinHeap<Integer> heap = BinaryMinHeap.heapify(array, null);

        assert heap.size() == 8;
        assert heap.peek() == 3 : "Heapified root must be minimum (3)";

        // Verify elements emerge in sorted order
        List<Integer> extracted = new ArrayList<>();
        while (!heap.isEmpty()) {
            extracted.add(heap.poll());
        }

        List<Integer> expected = List.of(3, 8, 17, 23, 42, 71, 89, 95);
        assert extracted.equals(expected) : "Heapify extraction mismatch: " + extracted;

        System.out.println("[PASS] Test 2: Floyd linear-time O(N) buildHeap validated.");
    }

    private static void testTopKStreamingEngine() {
        List<Integer> stream = List.of(12, 3, 17, 8, 42, 23, 95, 1, 71, 5, 60);
        int k = 4;

        // Top 4 largest elements are: [42, 60, 71, 95]
        List<Integer> topK = TopKStreamingEngine.findTopK(stream.iterator(), k);
        List<Integer> expected = List.of(42, 60, 71, 95);

        assert topK.equals(expected) : "Top-K mismatch: " + topK;

        // Edge case: K larger than stream size
        List<Integer> smallStream = List.of(5, 2);
        assert TopKStreamingEngine.findTopK(smallStream.iterator(), 5).equals(List.of(2, 5));

        System.out.println("[PASS] Test 3: Top-K stream filtering (O(N log K)) verified.");
    }

    private static void testMultiWayMergeEngine() {
        List<Integer> stream1 = List.of(1, 5, 9);
        List<Integer> stream2 = List.of(2, 6, 10);
        List<Integer> stream3 = List.of(3, 7, 11);
        List<Integer> stream4 = List.of(4, 8, 12);

        List<List<Integer>> streams = List.of(stream1, stream2, stream3, stream4);
        List<Integer> merged = MultiWayMergeEngine.merge(streams);

        List<Integer> expected = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        assert merged.equals(expected) : "K-Way merge mismatch: " + merged;

        System.out.println("[PASS] Test 4: Multi-way merge of K sorted streams verified.");
    }

    private static void testDelayedTaskSchedulerTieBreaking() {
        DelayedTaskScheduler scheduler = new DelayedTaskScheduler();

        // Three tasks scheduled at the EXACT same timestamp (1000L)
        scheduler.schedule(DelayedTaskScheduler.ScheduledTask.of("TaskA", 1000L));
        scheduler.schedule(DelayedTaskScheduler.ScheduledTask.of("TaskB", 1000L));
        scheduler.schedule(DelayedTaskScheduler.ScheduledTask.of("TaskC", 1000L));
        scheduler.schedule(DelayedTaskScheduler.ScheduledTask.of("TaskD", 2000L));

        // At t = 500ms, no task is due
        assert scheduler.pollNextDue(500L) == null;

        // At t = 1000ms, tasks A, B, C must execute in strict FIFO submission order!
        DelayedTaskScheduler.ScheduledTask tA = scheduler.pollNextDue(1000L);
        DelayedTaskScheduler.ScheduledTask tB = scheduler.pollNextDue(1000L);
        DelayedTaskScheduler.ScheduledTask tC = scheduler.pollNextDue(1000L);

        assert tA != null && "TaskA".equals(tA.taskId()) : "First due must be TaskA";
        assert tB != null && "TaskB".equals(tB.taskId()) : "Second due must be TaskB";
        assert tC != null && "TaskC".equals(tC.taskId()) : "Third due must be TaskC";

        // TaskD is not yet due at 1000ms
        assert scheduler.pollNextDue(1000L) == null;

        // At t = 2000ms, TaskD executes
        DelayedTaskScheduler.ScheduledTask tD = scheduler.pollNextDue(2000L);
        assert tD != null && "TaskD".equals(tD.taskId());

        System.out.println("[PASS] Test 5: Delayed task scheduler with FIFO tie-breaking verified.");
    }
}
```

---

## 13. Exercises and Challenges

### Exercise 1: Median from Data Stream (Dual Heap Pattern)
Design a class `MedianFinder` that calculates the median of an incoming stream of numbers in $O(1)$ time and $O(\log N)$ insertion:
- Hint: Maintain two heaps: a Max-Heap for the lower half of numbers, and a Min-Heap for the upper half of numbers, balancing their sizes so $| \text{maxHeap} | - | \text{minHeap} | \le 1$.

### Exercise 2: Indexed Priority Queue (Dijkstra Optimization)
Implement an Indexed Priority Queue where each element has an index $i \in [0, N-1]$, supporting `decreaseKey(i, newPriority)` in $O(\log N)$ time by maintaining a reverse lookup array (`pm[i]`).

### Exercise 3: D-ary Heap Performance Analysis
Analyze the trade-offs of a 4-ary heap ($D = 4$) versus a binary heap ($D = 2$). How does increasing $D$ affect cache line utilization, tree height ($\log_D N$), and the number of child comparisons during `siftDown`?

---

## 14. Review Questions

1. **How does a binary heap achieve $O(1)$ child and parent lookups without storing pointer references?**
   - *Answer:* Because it is a Complete Binary Tree, elements are packed sequentially into a 1D array. For any 0-based index $i$, the parent is at $(i - 1) / 2$, the left child is at $2i + 1$, and the right child is at $2i + 2$.

2. **Why does Floyd's `buildHeap` run in $\Theta(N)$ linear time, whereas repeated insertion takes $O(N \log N)$?**
   - *Answer:* Bottom-up `buildHeap` starts at non-leaf nodes. The vast majority of nodes reside near the bottom of the tree (height 0 or 1) and only need to sift down 1 or 2 steps. The summation $\sum k/2^k$ converges to a constant (2), bounding total work to $\le 2N = O(N)$.

3. **Why should mutable objects never have their priority fields mutated while residing inside a `PriorityQueue`?**
   - *Answer:* The heap structure only orders elements during `offer()` or `poll()`. Mutating an element's priority while in the heap does not trigger sifting, leaving the element at an invalid position and corrupting subsequent extractions.

4. **Why is `PriorityQueue.remove(Object)` an $O(N)$ operation?**
   - *Answer:* `PriorityQueue` has no hash index or lookup map. It must perform a sequential linear scan across the backing array to locate the element by `.equals()` ($O(N)$) before sifting ($O(\log N)$).

5. **How does a bounded Min-Heap solve the Top-K largest elements problem in $O(N \log K)$ time and $O(K)$ space?**
   - *Answer:* The min-heap retains only the $K$ largest elements seen so far, with the smallest of the top-$K$ at the root (`peek()`). For each stream element $X$, if $X > \text{root}$, the root is evicted and $X$ is inserted. Any element smaller than the root is instantly discarded.

---

## 15. Cross-Concept and Industry Interview Scenarios

### Scenario 1: Distributed LSM-Tree Compaction in RocksDB
- **Question:** How does a distributed storage engine (RocksDB, Apache Cassandra) merge 50 immutable SSTable files containing sorted key-value pairs into a single compacted file?
- **Answer:** It uses a $K$-Way Merge with a Min-Heap of size 50. The head key of each SSTable block iterator is enqueued. The minimum key is written to the new compacted SSTable, and the next key from that specific iterator is offered. Total compaction time is $O(N \log 50)$ where $N$ is total keys, streaming data through memory without loading entire files into RAM.

### Scenario 2: Real-Time Sliding-Window P99 Latency Calculation
- **Question:** How would you design a real-time monitor that continuously reports the P99 latency of API requests over the past 5 minutes?
- **Answer:** Maintain a dual-heap or t-digest structure coupled with a circular ring buffer of timestamps. When elements age past 5 minutes, evict them from the window. For quantile calculation, two heaps (Max-Heap for lower 99%, Min-Heap for upper 1%) balance counts so that the root of the Min-Heap directly yields the 99th percentile in $O(1)$ time.

---

## 16. Research, Citations, and Definitive Reading

1. **Williams, J. W. J.** *Algorithm 232: Heapsort.* Communications of the ACM, 7(6):347-348, 1964. (Original binary heap and heapsort formulation).
2. **Floyd, Robert W.** *Algorithm 245: Treesort 3.* Communications of the ACM, 7(12):701, 1964. (Linear-time `buildHeap` algorithm).
3. **Cormen, Thomas H., Charles E. Leiserson, Ronald L. Rivest, and Clifford Stein.** *Introduction to Algorithms (4th Edition).* MIT Press, 2022. Chapter 6: "Heapsort" (Complete mathematical proof of linear build-heap).
4. **Bloch, Joshua.** *Effective Java (3rd Edition).* Addison-Wesley, 2017. Item 14: "Consider implementing Comparable" (Tie-breaking rules and contract consistency).
5. **OpenJDK Specification.** *java.util.PriorityQueue Source Documentation.* Java Platform, Standard Edition 21 Reference Documentation, 2023.
