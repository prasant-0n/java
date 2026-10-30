package com.example.profiler.safe;

import com.example.profiler.model.SessionPayload;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hardened, leak-resistant session manager implementing bounded LRU caching,
 * guarded ThreadLocal scopes, immutable keys, and weak-reference listener registries.
 */
public class GuardedSessionManager {

    // 1. BOUNDED LRU CACHE WITH AUTOMATIC EVICTION
    private static class BoundedLruCache<K, V> extends LinkedHashMap<K, V> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        private final int maxEntries;

        public BoundedLruCache(int maxEntries) {
            super(16, 0.75f, true); // access-order = true
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxEntries;
        }
    }

    private final BoundedLruCache<String, SessionPayload> boundedSessionCache;

    // 2. GUARDED THREADLOCAL SCOPE PATTERN
    private static final ThreadLocal<SessionPayload> THREAD_SESSION_CONTEXT = new ThreadLocal<>();

    public static final class ThreadSessionScope implements AutoCloseable {
        private ThreadSessionScope(SessionPayload payload) {
            THREAD_SESSION_CONTEXT.set(payload);
        }

        public static ThreadSessionScope bind(SessionPayload payload) {
            Objects.requireNonNull(payload, "SessionPayload cannot be null");
            return new ThreadSessionScope(payload);
        }

        public static Optional<SessionPayload> current() {
            return Optional.ofNullable(THREAD_SESSION_CONTEXT.get());
        }

        @Override
        public void close() {
            THREAD_SESSION_CONTEXT.remove(); // Guaranteed cleanup
        }
    }

    // 3. IMMUTABLE RECORD KEY
    public record ImmutableSessionKey(UUID sessionId, int status) {
        public ImmutableSessionKey {
            Objects.requireNonNull(sessionId, "sessionId cannot be null");
        }
    }

    private final Set<ImmutableSessionKey> immutableKeyIndex = new HashSet<>();

    // 4. WEAK REFERENCE EVENT LISTENER REGISTRY
    public interface SessionEventListener {
        void onSessionEvent(String eventType, String sessionId);
    }

    private final List<WeakReference<SessionEventListener>> weakListeners = new CopyOnWriteArrayList<>();

    // Constructor
    public GuardedSessionManager(int maxCapacity) {
        this.boundedSessionCache = new BoundedLruCache<>(maxCapacity);
    }

    public synchronized void putSession(String key, SessionPayload payload) {
        boundedSessionCache.put(key, payload);
    }

    public synchronized SessionPayload getSession(String key) {
        return boundedSessionCache.get(key);
    }

    public synchronized int getCachedSessionCount() {
        return boundedSessionCache.size();
    }

    public synchronized boolean containsSession(String key) {
        return boundedSessionCache.containsKey(key);
    }

    public synchronized void registerKey(ImmutableSessionKey key) {
        immutableKeyIndex.add(key);
    }

    public synchronized boolean removeKey(ImmutableSessionKey key) {
        return immutableKeyIndex.remove(key);
    }

    public synchronized boolean containsKey(ImmutableSessionKey key) {
        return immutableKeyIndex.contains(key);
    }

    public synchronized int getKeyCount() {
        return immutableKeyIndex.size();
    }

    public void registerListener(SessionEventListener listener) {
        weakListeners.add(new WeakReference<>(listener));
    }

    public void purgeDeadListeners() {
        weakListeners.removeIf(ref -> ref.get() == null);
    }

    public int getActiveListenerCount() {
        purgeDeadListeners();
        return weakListeners.size();
    }

    public void dispatchEvent(String type, String sessionId) {
        purgeDeadListeners();
        for (WeakReference<SessionEventListener> ref : weakListeners) {
            SessionEventListener listener = ref.get();
            if (listener != null) {
                listener.onSessionEvent(type, sessionId);
            }
        }
    }
}
