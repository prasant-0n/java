package com.example.algo.ds;

import com.example.algo.model.Task;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Binary Min-Heap priority queue tailored for background task dispatching.
 * Uses pointer-free 1D array indexing with bit-shift parent/child arithmetic.
 * Guarantees O(log N) insert/poll and O(1) peek.
 */
public class TaskMinHeap {
    private Task[] heap;
    private int size = 0;

    public TaskMinHeap(int initialCapacity) {
        this.heap = new Task[Math.max(4, initialCapacity)];
    }

    public TaskMinHeap(Collection<Task> tasks) {
        this.heap = new Task[Math.max(4, tasks.size())];
        for (Task task : tasks) {
            this.heap[size++] = task;
        }
        buildHeap();
    }

    /**
     * Floyd's linear-time O(N) heap construction.
     */
    private void buildHeap() {
        for (int i = (size >>> 1) - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    public void insert(Task task) {
        if (task == null) throw new NullPointerException("Task cannot be null");
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, heap.length << 1);
        }
        heap[size] = task;
        siftUp(size);
        size++;
    }

    public Task poll() {
        if (size == 0) return null;
        Task root = heap[0];
        Task last = heap[size - 1];
        heap[size - 1] = null; // Prevent memory loitering
        size--;

        if (size > 0) {
            heap[0] = last;
            siftDown(0);
        }
        return root;
    }

    public Task peek() {
        if (size == 0) return null;
        return heap[0];
    }

    private void siftUp(int index) {
        Task target = heap[index];
        while (index > 0) {
            int parentIdx = (index - 1) >>> 1;
            Task parent = heap[parentIdx];
            if (target.compareTo(parent) >= 0) {
                break;
            }
            heap[index] = parent;
            index = parentIdx;
        }
        heap[index] = target;
    }

    private void siftDown(int index) {
        Task target = heap[index];
        int half = size >>> 1; // Loop while node has at least one child

        while (index < half) {
            int leftChildIdx = (index << 1) + 1;
            int rightChildIdx = leftChildIdx + 1;
            int smallestChildIdx = leftChildIdx;

            if (rightChildIdx < size && heap[rightChildIdx].compareTo(heap[leftChildIdx]) < 0) {
                smallestChildIdx = rightChildIdx;
            }

            if (target.compareTo(heap[smallestChildIdx]) <= 0) {
                break;
            }

            heap[index] = heap[smallestChildIdx];
            index = smallestChildIdx;
        }
        heap[index] = target;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}
