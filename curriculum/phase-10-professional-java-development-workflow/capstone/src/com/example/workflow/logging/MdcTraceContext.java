package com.example.workflow.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapped Diagnostic Context (MDC) ThreadLocal state manager with try-with-resources
 * auto-closeable scoping preventing thread pool context pollution.
 */
public class MdcTraceContext {
    private static final ThreadLocal<Map<String, String>> contextHolder =
        ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, String value) {
        if (key == null) throw new IllegalArgumentException("MDC key cannot be null");
        if (value == null) {
            remove(key);
        } else {
            contextHolder.get().put(key, value);
        }
    }

    public static String get(String key) {
        return contextHolder.get().get(key);
    }

    public static void remove(String key) {
        contextHolder.get().remove(key);
    }

    public static void clear() {
        contextHolder.get().clear();
    }

    public static Map<String, String> getCopyOfContextMap() {
        return new HashMap<>(contextHolder.get());
    }

    public static void setContextMap(Map<String, String> map) {
        if (map == null) {
            clear();
        } else {
            contextHolder.set(new HashMap<>(map));
        }
    }

    public static Scope openScope(String traceId, String tenantId, String userId) {
        put("traceId", traceId);
        put("tenantId", tenantId);
        put("userId", userId);
        return new Scope();
    }

    public static class Scope implements AutoCloseable {
        @Override
        public void close() {
            clear();
        }
    }
}
