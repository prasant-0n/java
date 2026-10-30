package com.example.algo.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search Tree index providing range queries and ordering for tasks.
 * Includes Hibbard Deletion and subtree pruning for efficient O(h + M) range queries.
 */
public class BinarySearchTreeIndex<K extends Comparable<K>, V> {

    public static class BstNode<K, V> {
        public K key;
        public V value;
        public BstNode<K, V> left;
        public BstNode<K, V> right;

        public BstNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private BstNode<K, V> root;
    private int size = 0;

    public void insert(K key, V value) {
        root = insertRecursive(root, key, value);
    }

    private BstNode<K, V> insertRecursive(BstNode<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new BstNode<>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insertRecursive(node.left, key, value);
        } else if (cmp > 0) {
            node.right = insertRecursive(node.right, key, value);
        } else {
            node.value = value; // update
        }
        return node;
    }

    public V find(K key) {
        BstNode<K, V> curr = root;
        while (curr != null) {
            int cmp = key.compareTo(curr.key);
            if (cmp < 0) curr = curr.left;
            else if (cmp > 0) curr = curr.right;
            else return curr.value;
        }
        return null;
    }

    /**
     * Range query with subtree pruning in O(h + M) time.
     * Only traverses branches that could potentially intersect [lowKey, highKey].
     */
    public List<V> queryRange(K lowKey, K highKey) {
        List<V> results = new ArrayList<>();
        queryRangeRecursive(root, lowKey, highKey, results);
        return results;
    }

    private void queryRangeRecursive(BstNode<K, V> node, K lowKey, K highKey, List<V> results) {
        if (node == null) return;

        // Pruning: traverse left subtree only if current key > lowKey
        if (node.key.compareTo(lowKey) > 0) {
            queryRangeRecursive(node.left, lowKey, highKey, results);
        }

        // Include current node if within [lowKey, highKey]
        if (node.key.compareTo(lowKey) >= 0 && node.key.compareTo(highKey) <= 0) {
            results.add(node.value);
        }

        // Pruning: traverse right subtree only if current key < highKey
        if (node.key.compareTo(highKey) < 0) {
            queryRangeRecursive(node.right, lowKey, highKey, results);
        }
    }

    public boolean delete(K key) {
        int initialSize = size;
        root = deleteRecursive(root, key);
        return size < initialSize;
    }

    private BstNode<K, V> deleteRecursive(BstNode<K, V> node, K key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = deleteRecursive(node.left, key);
        } else if (cmp > 0) {
            node.right = deleteRecursive(node.right, key);
        } else {
            // Case 1 & 2: 0 or 1 child
            if (node.left == null) {
                size--;
                return node.right;
            }
            if (node.right == null) {
                size--;
                return node.left;
            }
            // Case 3: 2 children - replace with in-order successor (min of right subtree)
            BstNode<K, V> successor = findMin(node.right);
            node.key = successor.key;
            node.value = successor.value;
            node.right = deleteRecursive(node.right, successor.key);
        }
        return node;
    }

    private BstNode<K, V> findMin(BstNode<K, V> node) {
        BstNode<K, V> curr = node;
        while (curr.left != null) {
            curr = curr.left;
        }
        return curr;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}
