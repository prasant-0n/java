package com.example.algo.ds;

import java.util.Objects;

/**
 * High-throughput Separate Chaining Hash Map.
 * Employs OpenJDK-style high-bit folding spreader: hash ^ (hash >>> 16)
 * and power-of-two capacity masking: (capacity - 1) & hash.
 * Automatically doubles capacity when load factor exceeds 0.75.
 */
public class SeparateChainingMap<K, V> {

    public static class Entry<K, V> {
        public final K key;
        public V value;
        public final int hash;
        public Entry<K, V> next;

        public Entry(K key, V value, int hash, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] table = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
    private int size = 0;

    private static int spread(int h) {
        return h ^ (h >>> 16);
    }

    public V put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");
        int h = spread(key.hashCode());
        int bucket = (table.length - 1) & h;

        for (Entry<K, V> curr = table[bucket]; curr != null; curr = curr.next) {
            if (curr.hash == h && curr.key.equals(key)) {
                V old = curr.value;
                curr.value = value;
                return old;
            }
        }

        table[bucket] = new Entry<>(key, value, h, table[bucket]);
        size++;

        if (size >= table.length * LOAD_FACTOR) {
            resize();
        }
        return null;
    }

    public V get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        int h = spread(key.hashCode());
        int bucket = (table.length - 1) & h;

        for (Entry<K, V> curr = table[bucket]; curr != null; curr = curr.next) {
            if (curr.hash == h && curr.key.equals(key)) {
                return curr.value;
            }
        }
        return null;
    }

    public V remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        int h = spread(key.hashCode());
        int bucket = (table.length - 1) & h;

        Entry<K, V> prev = null;
        for (Entry<K, V> curr = table[bucket]; curr != null; prev = curr, curr = curr.next) {
            if (curr.hash == h && curr.key.equals(key)) {
                if (prev == null) {
                    table[bucket] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return curr.value;
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public int capacity() { return table.length; }

    @SuppressWarnings("unchecked")
    private void resize() {
        int newCap = table.length << 1;
        Entry<K, V>[] newTable = (Entry<K, V>[]) new Entry[newCap];

        for (int i = 0; i < table.length; i++) {
            Entry<K, V> curr = table[i];
            while (curr != null) {
                Entry<K, V> next = curr.next;
                int newBucket = (newCap - 1) & curr.hash;
                curr.next = newTable[newBucket];
                newTable[newBucket] = curr;
                curr = next;
            }
        }
        this.table = newTable;
    }
}
