package com.example.profiler.leak;

import com.example.profiler.model.SessionPayload;

import java.util.*;

/**
 * Defective session manager implementation intentionally reproducing canonical
 * Java memory leak archetypes for profiling and root-cause analysis.
 */
public class LeakingSessionManager {

    // ARCHETYPE 1: Unbounded static collection without eviction bounds
    private static final Map<String, SessionPayload> UNBOUNDED_SESSION_CACHE = new HashMap<>();

    // ARCHETYPE 2: ThreadLocal without cleanup hygiene
    private static final ThreadLocal<SessionPayload> THREAD_SESSION_CONTEXT = new ThreadLocal<>();

    // ARCHETYPE 3: Mutable keys in hash collection violating hashCode() contract
    public static class MutableSessionKey {
        private String sessionId;
        private int status;

        public MutableSessionKey(String sessionId, int status) {
            this.sessionId = sessionId;
            this.status = status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getSessionId() {
            return sessionId;
        }

        public int getStatus() {
            return status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MutableSessionKey that)) return false;
            return status == that.status && Objects.equals(sessionId, that.sessionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sessionId, status);
        }
    }

    private final Set<MutableSessionKey> mutableKeyIndex = new HashSet<>();

    // ARCHETYPE 4: Strong reference event listener registry
    public interface SessionEventListener {
        void onSessionEvent(String eventType, String sessionId);
    }

    private final List<SessionEventListener> strongListeners = new ArrayList<>();

    // Operations
    public void putSession(String key, SessionPayload payload) {
        UNBOUNDED_SESSION_CACHE.put(key, payload);
    }

    public SessionPayload getSession(String key) {
        return UNBOUNDED_SESSION_CACHE.get(key);
    }

    public int getCachedSessionCount() {
        return UNBOUNDED_SESSION_CACHE.size();
    }

    public void bindThreadSession(SessionPayload payload) {
        // Omits removal: state leaks on pooled worker threads!
        THREAD_SESSION_CONTEXT.set(payload);
    }

    public SessionPayload getThreadSession() {
        return THREAD_SESSION_CONTEXT.get();
    }

    public void registerKey(MutableSessionKey key) {
        mutableKeyIndex.add(key);
    }

    public boolean removeKey(MutableSessionKey key) {
        return mutableKeyIndex.remove(key);
    }

    public boolean containsKey(MutableSessionKey key) {
        return mutableKeyIndex.contains(key);
    }

    public int getKeyCount() {
        return mutableKeyIndex.size();
    }

    public void registerListener(SessionEventListener listener) {
        strongListeners.add(listener);
    }

    public int getListenerCount() {
        return strongListeners.size();
    }

    public void clearAllForReset() {
        UNBOUNDED_SESSION_CACHE.clear();
        THREAD_SESSION_CONTEXT.remove();
        mutableKeyIndex.clear();
        strongListeners.clear();
    }
}
