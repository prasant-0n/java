package com.example.algo.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Doubly Linked List with Sentinel (dummy head and dummy tail) nodes.
 * Eliminates edge-case null checks during insertion and deletion.
 * Supports O(1) prepend, append, removal, and fast/slow pointer middle node discovery.
 */
public class SentinelDoublyLinkedList<E> {

    public static class Node<E> {
        public E item;
        public Node<E> prev;
        public Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }

    private final Node<E> headSentinel;
    private final Node<E> tailSentinel;
    private int size = 0;

    public SentinelDoublyLinkedList() {
        headSentinel = new Node<>(null);
        tailSentinel = new Node<>(null);
        headSentinel.next = tailSentinel;
        tailSentinel.prev = headSentinel;
    }

    public Node<E> addFirst(E item) {
        Node<E> newNode = new Node<>(item);
        newNode.next = headSentinel.next;
        newNode.prev = headSentinel;
        headSentinel.next.prev = newNode;
        headSentinel.next = newNode;
        size++;
        return newNode;
    }

    public Node<E> addLast(E item) {
        Node<E> newNode = new Node<>(item);
        newNode.prev = tailSentinel.prev;
        newNode.next = tailSentinel;
        tailSentinel.prev.next = newNode;
        tailSentinel.prev = newNode;
        size++;
        return newNode;
    }

    public E removeFirst() {
        if (isEmpty()) throw new NoSuchElementException("List is empty");
        return unlink(headSentinel.next);
    }

    public E removeLast() {
        if (isEmpty()) throw new NoSuchElementException("List is empty");
        return unlink(tailSentinel.prev);
    }

    public E unlink(Node<E> node) {
        if (node == headSentinel || node == tailSentinel) {
            throw new IllegalArgumentException("Cannot unlink sentinel nodes");
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        E value = node.item;
        node.item = null;
        node.prev = null;
        node.next = null;
        size--;
        return value;
    }

    /**
     * Fast and slow pointer algorithm to discover the middle element in a single pass O(N).
     */
    public E findMiddleItem() {
        if (isEmpty()) return null;
        Node<E> slow = headSentinel.next;
        Node<E> fast = headSentinel.next;

        while (fast != tailSentinel && fast.next != tailSentinel) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow.item;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public List<E> toList() {
        List<E> list = new ArrayList<>(size);
        Node<E> curr = headSentinel.next;
        while (curr != tailSentinel) {
            list.add(curr.item);
            curr = curr.next;
        }
        return list;
    }
}
