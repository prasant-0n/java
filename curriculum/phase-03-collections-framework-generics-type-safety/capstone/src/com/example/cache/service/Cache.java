package com.example.cache.service;

import com.example.cache.model.CacheStats;

import java.time.Duration;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface Cache<K, V> {

    void put(K key, V value);

    void put(K key, V value, Duration ttl);

    Optional<V> get(K key);

    boolean containsKey(K key);

    boolean remove(K key);

    void clear();

    int size();

    boolean isEmpty();

    // PECS: Function consumes K, produces V
    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

    // PECS: Function consumes K, produces V with custom TTL
    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, Duration ttl);

    // PECS: Map produces entries (? extends K, ? extends V)
    void putAll(Map<? extends K, ? extends V> entries);

    // PECS: Map produces entries with custom TTL
    void putAll(Map<? extends K, ? extends V> entries, Duration ttl);

    // PECS: Collection produces keys (? extends K)
    Map<K, V> getAll(Collection<? extends K> keys);

    // PECS: Predicate consumes K and V
    int evictIf(BiPredicate<? super K, ? super V> predicate);

    // Java 21 Sequenced Collections: Access-ordered list of values
    SequencedCollection<V> getAccessOrderedValues();

    // Truly immutable snapshot export
    Map<K, V> exportSnapshot();

    CacheStats getStats();

    void purgeExpired();
}
