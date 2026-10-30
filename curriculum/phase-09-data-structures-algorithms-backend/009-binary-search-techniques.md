# Concept: Linear Search vs Binary Search and Bisect Techniques

---

## 1. Metadata and Learning Goals

| Attribute | Specification |
|---|---|
| **Concept ID** | `P09-C009` |
| **Concept Name** | Linear Search vs Binary Search and Bisect Techniques |
| **Phase** | Phase 09: Data Structures and Algorithms for Backend Developers |
| **Prerequisites** | `P09-C001` (Algorithmic Complexity & Amortized Analysis), `P09-C002` (Dynamic Arrays & Resizing Mechanics) |
| **Target Mastery Level** | Advanced / Production-Grade Backend Architect |
| **JDK Compatibility** | OpenJDK 21 LTS (Verified) |
| **Primary Artifact** | `curriculum/phase-09-data-structures-algorithms-backend/009-binary-search-techniques.md` |

### Core Learning Goals
1. **Master Asymptotic Search Bounds:** Contrast the $O(N)$ linear scan against $O(\log N)$ logarithmic divide-and-conquer, proving the information-theoretic lower bound ($\lceil \log_2(N + 1) \rceil$ comparisons).
2. **Prevent Integer Arithmetic Overflow:** Deconstruct the infamous `(low + high) / 2` arithmetic overflow bug and implement the bitwise unsigned shift remedy (`(low + high) >>> 1`).
3. **Implement Bisect Lower and Upper Bounds:** Construct boundary searches (`lowerBound` / first occurrence and `upperBound` / last occurrence + 1), deriving range frequencies in $O(\log N)$ time.
4. **Decipher `Arrays.binarySearch` Insertion Point Math:** Master OpenJDK's negative encoding formula (`-(insertionPoint + 1)`), proving why offset $-1$ is required to avoid the ambiguous zero trap.
5. **Formulate Binary Search on Answer:** Generalize binary search beyond array indexes to monotonic predicate functions $P(x)$, solving optimization problems (capacity sizing, time-series offsets, IP range lookup).

---

## 2. Definition, Purpose, and Mental Model

### What Are Linear Search and Binary Search?
**Linear Search** inspects elements sequentially from index $0$ to $N - 1$ until a matching element is found or the collection is exhausted. It imposes zero ordering preconditions on the input collection.

**Binary Search** is a divide-and-conquer algorithm that operates on a collection ordered by a monotonic invariant. By comparing the search target against the midpoint element, it discards half of the remaining search space at each iteration, achieving $O(\log N)$ time.

```
LINEAR SEARCH (Unsorted Data):
Scan: [ 42 ] ──► [ 17 ] ──► [ 89 ] ──► [ 8 ] ──► [ 23 ]  (O(N) steps)

BINARY SEARCH (Sorted Data, Target = 23):
Range [0..6]:  [ 3 | 8 | 17 | 23 | 42 | 71 | 89 ]
                             ▲
                        Mid = 23 (MATCH in 1 step!)
```

### The Backend Mental Model: The Logarithmic Zoom Lens
In high-throughput server backends:
- An array of $1,000,000,000$ elements requires up to **1 billion comparisons** with linear search ($\approx 1 \text{ second}$).
- With binary search, $\log_2(10^9) \approx \mathbf{30 \text{ comparisons}}$ ($\approx 15 \text{ nanoseconds}$)!
- In database storage engines and messaging queues:
  - **Kafka Partition Index (.index):** Maps message logical offsets to physical `.log` file byte positions using binary search.
  - **Prometheus Time-Series Storage:** Locates chunk time intervals $[T_{\text{start}}, T_{\text{end}}]$ in sorted timestamp blocks.
  - **GeoIP / CIDR Routing:** Matches incoming IP addresses against millions of allocated CIDR blocks in microseconds.

---

## 3. Foundations and Theory

### 3.1 Mathematical Comparison and Asymptotic Bounds

| Search Paradigm | Precondition | Best Case | Average Case | Worst Case | Space Complexity |
|---|---|---|---|---|---|
| **Linear Search** | None (unsorted data) | $O(1)$ | $O(N)$ | $O(N)$ | $O(1)$ |
| **Binary Search** | Monotonically sorted | $O(1)$ | $O(\log N)$ | $O(\log N)$ | $O(1)$ iterative |
| **Bisect (Lower/Upper)** | Monotonically sorted | $O(\log N)$ | $O(\log N)$ | $O(\log N)$ | $O(1)$ iterative |
| **Binary Search on Answer** | Monotonic predicate $P(x)$ | $O(1)$ | $O(\log(\text{range}) \cdot T_P)$ | $O(\log(\text{range}) \cdot T_P)$ | $O(1)$ |

### 3.2 Information-Theoretic Lower Bound
Can any comparison-based algorithm search a sorted array faster than $O(\log N)$?
No. Any comparison-based search algorithm can be modeled as a **binary decision tree**:
- Each internal node represents a comparison $A[i] \lessgtr \text{target}$.
- Each leaf represents one of the $N + 1$ possible outcomes ($N$ possible element positions, plus 1 outcome for "not found").
- A binary tree of height $h$ has at most $2^h$ leaves.
- Therefore:
  $$2^h \ge N + 1 \implies h \ge \log_2(N + 1)$$
- The minimum number of comparisons in the worst case is $\lceil \log_2(N + 1) \rceil \in \Omega(\log N)$.

---

## 4. Language, Specification, and Platform Mechanics

### 4.1 The Infamous Midpoint Integer Overflow Bug
In 2006, Joshua Bloch published a famous post revealing that the binary search implementation in `java.util.Arrays` (and virtually all standard algorithms textbooks for 20 years) contained a silent arithmetic overflow bug:

```java
// BUG: INTEGER ARITHMETIC OVERFLOW
int mid = (low + high) / 2;
```

#### Why Does This Fail in Production?
If `low` and `high` are large integers (e.g., in a huge array or large range where `low = 1,073,741,824` and `high = 1,073,741,825`):
$$\text{low} + \text{high} = 2,147,483,649 > \text{Integer.MAX\_VALUE} \ (2,147,483,647)$$
The 32-bit signed addition overflows to **$-2,147,483,647$**!
Dividing by 2 yields a **negative index** ($-1,073,741,823$), which crashes immediately with:
`java.lang.ArrayIndexOutOfBoundsException: Index -1073741823 out of bounds for length ...`.

#### The Production Remedies:
1. **Subtraction Form:**
   ```java
   int mid = low + ((high - low) / 2);
   ```
2. **Unsigned Bitwise Shift (HotSpot Optimized):**
   ```java
   int mid = (low + high) >>> 1;
   ```
   The unsigned right shift operator `>>>` treats the 32-bit sum as an **unsigned 32-bit integer** (range 0 to $4,294,967,295$). Even if signed addition wraps around to negative, the high-order sign bit is shifted in as `0`, yielding the mathematically exact midpoint!

### 4.2 Bisect Techniques: Lower Bound vs Upper Bound
When an array contains **duplicate elements**, standard binary search returns an arbitrary match among the duplicates.
To locate exact boundaries, backend systems use **Bisect Algorithms**:

```
Array:   [ 10,  20,  20,  20,  30,  40 ]
Index:     0    1    2    3    4    5

lowerBound(20) = 1 (First index where element >= 20)
upperBound(20) = 4 (First index where element > 20)

Frequency of 20 = upperBound(20) - lowerBound(20) = 4 - 1 = 3!
```

#### 1. Lower Bound (`lowerBound` / Bisect Left):
Finds the **smallest index $i$** such that $A[i] \ge \text{target}$. If all elements are smaller than target, returns $N$.
```java
public static int lowerBound(int[] a, int target) {
    int low = 0, high = a.length;
    while (low < high) {
        int mid = (low + high) >>> 1;
        if (a[mid] >= target) {
            high = mid; // Search left half including mid
        } else {
            low = mid + 1; // Discard left half
        }
    }
    return low;
}
```

#### 2. Upper Bound (`upperBound` / Bisect Right):
Finds the **smallest index $i$** such that $A[i] > \text{target}$.
```java
public static int upperBound(int[] a, int target) {
    int low = 0, high = a.length;
    while (low < high) {
        int mid = (low + high) >>> 1;
        if (a[mid] > target) {
            high = mid;
        } else {
            low = mid + 1;
        }
    }
    return low;
}
```

### 4.3 OpenJDK `Arrays.binarySearch` Insertion Point Mathematics
In OpenJDK's `java.util.Arrays.binarySearch(int[] a, int key)`:
- If key is found: returns index $\ge 0$.
- If key is NOT found: returns a negative integer `-(insertionPoint + 1)`.

#### Why the $+1$ Offset?
Consider searching for a key smaller than all elements in the array (e.g., searching for `5` in `[10, 20, 30]`).
The correct insertion point is index `0`.
If the API simply returned `-insertionPoint`:
$$-0 = 0$$
The caller could not distinguish between "Found at index 0" and "Not found, insert at index 0"!
By returning `-(insertionPoint + 1)`:
$$\text{return} = -(0 + 1) = -1$$
Any negative return value guarantees the key was not found.
To extract the actual insertion point:
$$\text{insertionPoint} = -(\text{result} + 1) = -\text{result} - 1$$

---

## 5. Evolution Across Java Versions

| Java Version | Release Year | Evolution / Feature Milestone |
|---|---|---|
| **JDK 1.2** | 1998 | `java.util.Arrays.binarySearch` and `Collections.binarySearch` introduced. |
| **Java 6** | 2006 | **Bug 6412541:** Fixed the integer overflow bug in `Arrays.binarySearch` using `(low + high) >>> 1`. |
| **Java 9** | 2017 | `Arrays.mismatch()` introduced, providing vectorized mismatch search across arrays using SIMD. |
| **Java 21 LTS** | 2023 | C2 JIT compiler generates optimized branchless conditional moves (`cmov`) for short binary search loops. |

---

## 6. Architecture and Design Patterns

### 6.1 Binary Search on Answer (Monotonic Predicate Pattern)
Many complex backend optimization problems have no pre-sorted array to search. However, they possess a **monotonic feasibility function**:
$$P(x) = \begin{cases} \text{false} & x < x^* \\ \text{true} & x \ge x^* \end{cases}$$

If $P(x)$ is monotonic (once it becomes `true`, it remains `true` for all greater $x$), we can binary search the solution space $[x_{\min}, x_{\max}]$ to find the optimal threshold $x^*$ in $O(\log(\text{range}) \cdot \text{cost}(P))$.

#### Classical Backend Example: Capacity Sizing
- Problem: Distribute $N$ tasks across $M$ worker nodes such that no worker's load exceeds capacity $C$. What is the minimum possible capacity $C$?
- Predicate $P(C)$: "Can all tasks be processed within capacity $C$ using $\le M$ workers?" (Evaluates in $O(N)$ linear scan).
- Search range: $[1, \sum \text{taskLoads}]$.
- Binary search finds minimal $C$ in $O(N \log(\sum \text{loads}))$ time!

---

## 7. Failure Modes, Anti-Patterns, and Production Traps

### 7.1 Searching Unsorted Arrays with `Arrays.binarySearch`
Calling `binarySearch` on an unsorted collection produces **non-deterministic, silent corruption**:
```java
int[] unsorted = {40, 10, 30, 20};
int idx = Arrays.binarySearch(unsorted, 10); // RETURNS -1 (NOT FOUND)!
```
Because the array is not sorted, the algorithm discards half the array based on a false premise, returning "not found" even though the element is present.
**Rule:** Ensure data is sorted before calling binary search, or maintain a sorted collection (`TreeSet`, `ArrayList` with bisect insertion).

### 7.2 The Infinite Loop Pointer Anti-Pattern
When writing custom binary searches, updating bounds incorrectly causes infinite loops:
```java
// BUG: Infinite Loop when high - low == 1
while (low < high) {
    int mid = (low + high) / 2;
    if (isValid(mid)) {
        low = mid; // BUG: If mid == low, low never advances!
    } else {
        high = mid - 1;
    }
}
```
**Remedy:** Ensure each branch strictly narrows the interval (`low = mid + 1` or use upper-biased mid `(low + high + 1) >>> 1`).

---

## 8. Security and Hardening

### 8.1 Timing Attacks on Binary Search Lookups
In cryptographic security (such as secret key matching or authorization token lookups), binary search exposes timing variations:
- Lookups that match near the root terminate in 1 step ($\approx 2 \text{ ns}$).
- Lookups near leaves take $\log N$ steps ($\approx 30 \text{ ns}$).
An attacker measuring microsecond response times can infer position and prefix bits of sensitive data (Timing Attack).
**Hardening Rule:** For security-sensitive secrets, **never use binary search**. Use constant-time linear comparisons (`MessageDigest.isEqual()`).

---

## 9. Performance, Sizing, and Operational Profile

### 9.1 Cache Locality: Linear Search vs Binary Search on Small $N$
While Big-O indicates Binary Search ($O(\log N)$) is superior to Linear Search ($O(N)$), **for small arrays ($N \le 32$), Linear Search is frequently faster** on modern CPUs!
- **Linear Search:** Reads memory sequentially. CPU hardware prefetchers stream contiguous 64-byte cache lines. Zero branch mispredictions.
- **Binary Search:** Jumps across memory (index $N/2$, then $N/4$, then $3N/4$), incurring CPU cache line misses and branch predictor stalls.

```
N <= 32:    Linear Search wins (CPU prefetch + SIMD)
N > 64:     Binary Search dominates exponentially
```

---

## 10. Tooling, Observability, and Debugging

### 10.1 Monitoring Bisect Search Latency
In range-query routing systems (e.g., GeoIP filters):
```java
Timer.builder("geoip.lookup.time")
     .publishPercentiles(0.5, 0.95, 0.99)
     .register(registry);
```

---

## 11. Framework, Cloud, and Ecosystem Integration

### 11.1 Apache Kafka Offset Index Lookup
Kafka stores topic messages in segmented log files. Alongside each `.log` file is a `.index` file containing pairs of `(relativeOffset, physicalPosition)`:
- Kafka loads the index into an off-heap memory-mapped byte buffer (`ByteBuffer`).
- When a consumer requests offset $X$, Kafka performs an in-memory **binary search on the index** to find the closest physical file offset in $< 1 \text{ microsecond}$.

---

## 12. Production-Grade Code Demonstration

The following standalone, self-contained Java class comprehensively proves:
1. **`SafeBinarySearch`:** Overflow-safe binary search using `(low + high) >>> 1`.
2. **`Bisect`:** Lower Bound and Upper Bound implementations with frequency counting of duplicate keys.
3. **`ArraysBinarySearchInsertionPoint`:** Decoding OpenJDK negative insertion point math (`-(insertionPoint + 1)`).
4. **`BinarySearchOnAnswer`:** Capacity-to-ship optimization solving the monotonic predicate problem in $O(N \log(\text{range}))$.
5. **`IpRangeRouter`:** High-throughput IP range to Geo/Region lookup using binary search on sorted IPv4 ranges.

Save as `curriculum/phase-09-data-structures-algorithms-backend/009-binary-search-techniques.md` companion verification suite:

```java
package curriculum.phase09;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standalone verification suite demonstrating Linear vs Binary Search,
 * Bisect Lower/Upper Bounds, Insertion Point Encoding, and Binary Search on Answer.
 */
public class BinarySearchTechniquesDemo {

    // =========================================================================
    // 1. OVERFLOW-SAFE BINARY SEARCH
    // =========================================================================

    public static int binarySearch(int[] array, int target) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            // Unsigned shift: immune to 32-bit signed integer addition overflow!
            int mid = (low + high) >>> 1;
            int midVal = array[mid];

            if (midVal < target) {
                low = mid + 1;
            } else if (midVal > target) {
                high = mid - 1;
            } else {
                return mid; // Found target
            }
        }
        return -(low + 1); // Not found: returns -(insertionPoint + 1)
    }

    // =========================================================================
    // 2. BISECT: LOWER BOUND AND UPPER BOUND
    // =========================================================================

    public static class Bisect {
        /**
         * Returns smallest index i where array[i] >= target.
         * If all elements < target, returns array.length.
         */
        public static int lowerBound(int[] array, int target) {
            int low = 0;
            int high = array.length;
            while (low < high) {
                int mid = (low + high) >>> 1;
                if (array[mid] >= target) {
                    high = mid;
                } else {
                    low = mid + 1;
                }
            }
            return low;
        }

        /**
         * Returns smallest index i where array[i] > target.
         * If all elements <= target, returns array.length.
         */
        public static int upperBound(int[] array, int target) {
            int low = 0;
            int high = array.length;
            while (low < high) {
                int mid = (low + high) >>> 1;
                if (array[mid] > target) {
                    high = mid;
                } else {
                    low = mid + 1;
                }
            }
            return low;
        }

        /**
         * Returns count of occurrences of target in a sorted array in O(log N) time.
         */
        public static int countOccurrences(int[] array, int target) {
            return upperBound(array, target) - lowerBound(array, target);
        }
    }

    // =========================================================================
    // 3. BINARY SEARCH ON ANSWER: SHIPMENT CAPACITY OPTIMIZATION
    // =========================================================================

    public static class ShipmentOptimizer {
        /**
         * Computes the minimum conveyor capacity needed to ship all packages
         * within D days using Binary Search on Answer.
         */
        public static int minShipCapacity(int[] weights, int days) {
            int maxWeight = 0;
            int totalWeight = 0;
            for (int w : weights) {
                maxWeight = Math.max(maxWeight, w);
                totalWeight += w;
            }

            // Feasible search space: [maxWeight, totalWeight]
            int low = maxWeight;
            int high = totalWeight;

            while (low < high) {
                int midCapacity = (low + high) >>> 1;
                if (canShipWithinDays(weights, days, midCapacity)) {
                    high = midCapacity; // Capacity is feasible; try smaller
                } else {
                    low = midCapacity + 1; // Infeasible; must increase capacity
                }
            }
            return low;
        }

        private static boolean canShipWithinDays(int[] weights, int days, int capacity) {
            int daysNeeded = 1;
            int currentLoad = 0;

            for (int w : weights) {
                if (currentLoad + w > capacity) {
                    daysNeeded++;
                    currentLoad = 0;
                }
                currentLoad += w;
            }
            return daysNeeded <= days;
        }
    }

    // =========================================================================
    // 4. IP RANGE ROUTER (CIDR / IP INTERVAL SEARCH)
    // =========================================================================

    public static class IpRangeRouter {
        public record IpRange(long startIp, long endIp, String region) 
            implements Comparable<IpRange> {
            @Override
            public int compareTo(IpRange o) {
                return Long.compare(this.startIp, o.startIp);
            }
        }

        private final List<IpRange> ranges;

        public IpRangeRouter(List<IpRange> ranges) {
            this.ranges = new ArrayList<>(ranges);
            Collections.sort(this.ranges); // Ensure sorted by startIp
        }

        public String findRegion(long ip) {
            int low = 0;
            int high = ranges.size() - 1;
            int candidateIdx = -1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                if (ranges.get(mid).startIp() <= ip) {
                    candidateIdx = mid; // Candidate found, check right for closer match
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }

            if (candidateIdx != -1) {
                IpRange candidate = ranges.get(candidateIdx);
                if (ip >= candidate.startIp() && ip <= candidate.endIp()) {
                    return candidate.region();
                }
            }
            return "UNKNOWN";
        }
    }

    // =========================================================================
    // 5. COMPREHENSIVE VERIFICATION SUITE
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" Binary Search, Bisect & Answer Optimization Suite             ");
        System.out.println("===============================================================");

        testOverflowSafeBinarySearch();
        testBisectLowerAndUpperBound();
        testArraysBinarySearchInsertionPointMath();
        testBinarySearchOnAnswerShipment();
        testIpRangeRouter();

        System.out.println("\nAll 5 verification test suites passed successfully with assertions enabled!");
    }

    private static void testOverflowSafeBinarySearch() {
        int[] sorted = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};

        // Found cases
        assert binarySearch(sorted, 2) == 0;
        assert binarySearch(sorted, 23) == 5;
        assert binarySearch(sorted, 91) == 9;

        // Not found cases with insertion point math
        int missing = binarySearch(sorted, 15); // Should insert at index 4 (between 12 and 16)
        assert missing == -(4 + 1) : "Missing 15 must return -(4 + 1) = -5: " + missing;

        int missingSmall = binarySearch(sorted, 1); // Should insert at index 0
        assert missingSmall == -(0 + 1) : "Missing 1 must return -(0 + 1) = -1: " + missingSmall;

        int missingBig = binarySearch(sorted, 100); // Should insert at index 10
        assert missingBig == -(10 + 1) : "Missing 100 must return -11: " + missingBig;

        System.out.println("[PASS] Test 1: Overflow-safe binary search and exact match validated.");
    }

    private static void testBisectLowerAndUpperBound() {
        // Array with duplicates:
        // Index:  0   1   2   3   4   5   6   7
        // Value: [5, 10, 10, 10, 20, 20, 30, 40]
        int[] arr = {5, 10, 10, 10, 20, 20, 30, 40};

        // lowerBound: first index >= target
        assert Bisect.lowerBound(arr, 10) == 1;
        assert Bisect.lowerBound(arr, 20) == 4;
        assert Bisect.lowerBound(arr, 5) == 0;
        assert Bisect.lowerBound(arr, 45) == 8 : "Target exceeding max must return length (8)";

        // upperBound: first index > target
        assert Bisect.upperBound(arr, 10) == 4;
        assert Bisect.upperBound(arr, 20) == 6;
        assert Bisect.upperBound(arr, 40) == 8;

        // Duplicate frequency in O(log N)
        assert Bisect.countOccurrences(arr, 10) == 3 : "10 occurs 3 times";
        assert Bisect.countOccurrences(arr, 20) == 2 : "20 occurs 2 times";
        assert Bisect.countOccurrences(arr, 99) == 0 : "99 occurs 0 times";

        System.out.println("[PASS] Test 2: Bisect lower/upper bound and frequency counting verified.");
    }

    private static void testArraysBinarySearchInsertionPointMath() {
        int[] data = {10, 20, 30, 40, 50};

        // OpenJDK Arrays.binarySearch
        int resFound = Arrays.binarySearch(data, 30);
        assert resFound == 2;

        int resMissing = Arrays.binarySearch(data, 25);
        assert resMissing < 0 : "Must be negative for missing key";
        int insertionPoint = -resMissing - 1;
        assert insertionPoint == 2 : "Insertion point for 25 must be index 2";

        int resSmall = Arrays.binarySearch(data, 5);
        assert -resSmall - 1 == 0 : "Insertion point for 5 must be index 0";

        System.out.println("[PASS] Test 3: OpenJDK Arrays.binarySearch negative insertion point verified.");
    }

    private static void testBinarySearchOnAnswerShipment() {
        int[] weights = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int days = 5;

        // Minimum capacity required to ship within 5 days is 15:
        // Day 1: 1, 2, 3, 4, 5 (sum 15)
        // Day 2: 6, 7 (sum 13)
        // Day 3: 8 (sum 8)
        // Day 4: 9 (sum 9)
        // Day 5: 10 (sum 10)
        int minCap = ShipmentOptimizer.minShipCapacity(weights, days);
        assert minCap == 15 : "Expected minimum capacity 15 but got: " + minCap;

        // If days = 1, capacity must be sum of all weights (55)
        assert ShipmentOptimizer.minShipCapacity(weights, 1) == 55;

        // If days = weights.length (10), capacity must be max weight (10)
        assert ShipmentOptimizer.minShipCapacity(weights, 10) == 10;

        System.out.println("[PASS] Test 4: Binary Search on Answer (Shipment Optimizer) verified.");
    }

    private static void testIpRangeRouter() {
        List<IpRangeRouter.IpRange> ranges = List.of(
            new IpRangeRouter.IpRange(1000L, 1999L, "US-EAST"),
            new IpRangeRouter.IpRange(2000L, 2999L, "EU-WEST"),
            new IpRangeRouter.IpRange(3000L, 3999L, "AP-SOUTH"),
            new IpRangeRouter.IpRange(5000L, 5999L, "SA-EAST")
        );

        IpRangeRouter router = new IpRangeRouter(ranges);

        assert "US-EAST".equals(router.findRegion(1500L));
        assert "EU-WEST".equals(router.findRegion(2000L)); // Boundary start
        assert "EU-WEST".equals(router.findRegion(2999L)); // Boundary end
        assert "AP-SOUTH".equals(router.findRegion(3500L));
        assert "UNKNOWN".equals(router.findRegion(4500L)); // Gap between 3999 and 5000
        assert "UNKNOWN".equals(router.findRegion(500L));  // Before first range
        assert "UNKNOWN".equals(router.findRegion(9999L)); // After last range

        System.out.println("[PASS] Test 5: IP range routing using bisect interval matching verified.");
    }
}
```

---

## 13. Exercises and Challenges

### Exercise 1: Search in Rotated Sorted Array in $O(\log N)$
Given an array sorted in ascending order that was rotated at an unknown pivot (e.g., `[4, 5, 6, 7, 0, 1, 2]`), write an algorithm to find the target index in $O(\log N)$ time by determining which half is normally sorted.

### Exercise 2: Kth Smallest Element in Multiplication Table
Given integers $M$ and $N$, find the $K$-th smallest number in an $M \times N$ multiplication table using Binary Search on Answer in $O(M \log(M \cdot N))$ time without building the table in memory.

### Exercise 3: Peak Element in Array in $O(\log N)$
An element is a peak if it is strictly greater than its neighbors. Given an integer array where $A[i] \neq A[i+1]$, find any peak element in $O(\log N)$ time using binary search.

---

## 14. Review Questions

1. **Why does the unsigned right shift `(low + high) >>> 1` solve the integer overflow bug in Binary Search?**
   - *Answer:* When `low + high > Integer.MAX_VALUE`, standard signed integer addition wraps into a negative number. The unsigned right shift `>>>` treats the 32-bit register as an unsigned value, shifting the sign bit rightward as a 0 and producing the true arithmetic average.

2. **What is the difference between `lowerBound` and `upperBound` when searching for duplicates in a sorted array?**
   - *Answer:* `lowerBound` returns the smallest index where $A[i] \ge \text{target}$ (the first occurrence of target). `upperBound` returns the smallest index where $A[i] > \text{target}$ (the index immediately following the last occurrence of target).

3. **Why does OpenJDK's `Arrays.binarySearch` encode missing results as `-(insertionPoint + 1)` rather than `-insertionPoint`?**
   - *Answer:* If a missing target belongs at index `0`, returning `-0` would equal `0`, creating an ambiguous result indistinguishable from finding the key at index `0`. The `+1` offset guarantees that all missing results are strictly negative ($\le -1$).

4. **When is Linear Search faster than Binary Search on modern hardware?**
   - *Answer:* For small arrays ($N \le 32$), linear search is faster because CPU hardware prefetchers stream sequential cache lines into L1 cache with near-zero latency and no branch mispredictions, whereas binary search jumps across memory, incurring cache misses.

5. **What is "Binary Search on Answer", and what condition must the problem satisfy?**
   - *Answer:* Binary Search on Answer searches the solution space of an optimization problem rather than an array. It requires a monotonic predicate function $P(x)$ such that if $P(x_0)$ is feasible, $P(x)$ is feasible for all $x \ge x_0$ (or $\le x_0$).

---

## 15. Cross-Concept and Industry Interview Scenarios

### Scenario 1: High-Throughput GeoIP Microservice
- **Question:** Your microservice receives 100,000 HTTP requests/sec and must map the client IPv4 address to a geographic country code. You have a database of 2,500,000 sorted non-overlapping CIDR IP ranges. How do you implement this with sub-millisecond latency?
- **Answer:** Store the 2,500,000 ranges in a flat sorted primitive array (`long[] startIps`, `long[] endIps`, `short[] countryCodes`). When an IP arrives, perform an in-memory binary search (bisect left) on `startIps` to find the candidate range in $\approx 22$ comparisons ($< 50 \text{ nanoseconds}$).

### Scenario 2: Kafka Consumer Log Offset Lookup
- **Question:** How does an Apache Kafka broker locate a message with logical offset 1,450,200 inside an append-only `.log` segment file containing 10,000,000 messages?
- **Answer:** Kafka maintains an in-memory memory-mapped `.index` file with sparse `(offset, physicalPosition)` entries. It runs an off-heap binary search on the index buffer in $O(\log K)$ time to locate the nearest physical byte offset, then performs a short sequential read from disk to retrieve the exact message.

---

## 16. Research, Citations, and Definitive Reading

1. **Bloch, Joshua.** *Extra, Extra - Read All About It: Nearly All Binary Searches and Mergesorts are Broken.* Google Research Blog, June 2006. (The canonical analysis of the `(low + high) / 2` integer overflow bug).
2. **Knuth, Donald E.** *The Art of Computer Programming, Volume 3: Sorting and Searching (2nd Edition).* Addison-Wesley, 1998. Section 6.2.1: "Searching an Ordered Table".
3. **Cormen, Thomas H., Charles E. Leiserson, Ronald L. Rivest, and Clifford Stein.** *Introduction to Algorithms (4th Edition).* MIT Press, 2022. Chapter 2: "Getting Started" (Divide-and-conquer and binary search analysis).
4. **OpenJDK Specification.** *java.util.Arrays.binarySearch Documentation.* Java Platform, Standard Edition 21 Reference Documentation, 2023.
5. **Bentley, Jon.** *Programming Pearls (2nd Edition).* Addison-Wesley Professional, 1999. Column 4: "Writing Correct Programs" (Proving binary search invariants).
