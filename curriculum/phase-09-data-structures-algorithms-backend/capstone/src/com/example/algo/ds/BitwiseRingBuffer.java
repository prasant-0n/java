package com.example.algo.ds;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * High-performance circular ring buffer FIFO queue.
 * Requires capacity to be a power of two, replacing expensive integer division remainder (%)
 * with single-cycle bitwise AND masking: (index) & (capacity - 1).
 * Features defensive slot nulling to prevent GC loitering.
 */
public class BitwiseRingBuffer<E> {
    private Object[] elements;
    private int head = 0; // Index of next element to poll
    private int tail = 0; // Index of next element to offer
    private int size = 0;
    private int mask;

    public BitwiseRingBuffer(int initialCapacity) {
        int cap = roundToPowerOfTwo(Math.max(4, initialCapacity));
        this.elements = new Object[cap];
        this.mask = cap - 1;
    }

    private static int roundToPowerOfTwo(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 1 << 30) ? 1 << 30 : n + 1;
    }

    public boolean offer(E element) {
        if (element == null) throw new NullPointerException("Null elements prohibited");
        if (size == elements.length) {
            doubleCapacity();
        }
        elements[tail] = element;
        tail = (tail + 1) & mask;
        size++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public E poll() {
        if (size == 0) return null;
        E item = (E) elements[head];
        elements[head] = null; // Prevent object loitering memory leaks
        head = (head + 1) & mask;
        size--;
        return item;
    }

    @SuppressWarnings("unchecked")
    public E peek() {
        if (size == 0) return null;
        return (E) elements[head];
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public int capacity() { return elements.length; }

    private void doubleCapacity() {
        int oldCap = elements.length;
        int newCap = oldCap << 1;
        Object[] newElements = new Object[newCap];

        // Linearize circular buffer into new contiguous array
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[(head + i) & mask];
        }

        this.elements = newElements;
        this.head = 0;
        this.tail = size;
        this.mask = newCap - 1;
    }
}
