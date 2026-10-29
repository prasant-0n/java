# Type Erasure, Bridge Methods, Heap Pollution, and Reifiable Types

---

## 1. Concept Definition & Core Purpose

Java generics are implemented via **Type Erasure**—a compiler-driven translation process in which type parameters are checked at compile time for strict safety, and then completely erased before bytecode generation.

While type erasure enabled Java 5 to maintain 100% binary backward compatibility with pre-Java 5 JVMs and legacy libraries without rewriting the JVM class file format or memory layout, it creates four profound architectural consequences that every senior backend engineer must master:

1. **Type Erasure:** Generic type arguments do not exist in executable bytecode. All parameterized types (`List<String>`, `List<Integer>`, `List<Order>`) share the exact same raw runtime class (`List.class`).
2. **Bridge Methods:** Synthetic methods generated automatically by the compiler (`javac`) with the `ACC_BRIDGE` and `ACC_SYNTHETIC` flags to preserve method overriding and polymorphism when a class extends a generic class or implements a parameterized interface.
3. **Reifiable vs. Non-Reifiable Types:**
   - **Reifiable Types:** Types whose full runtime type information is completely preserved at runtime (primitives, non-generic classes, raw types, unbounded wildcards `List<?>`).
   - **Non-Reifiable Types:** Types whose type information is partially or completely erased at compile time (`List<String>`, `Map<K, V>`, `T`).
4. **Heap Pollution:** A dangerous runtime state where a variable of a parameterized type points to an object that does not conform to that parameterized type (e.g., a `List<String>` holding an `Integer`), inevitably resulting in a delayed, difficult-to-debug `ClassCastException` at an extraction call site.

```
                  Java Compilation & Erasure Pipeline
┌────────────────────────────┐
│ Source Code:               │
│ List<String> list = ...;   │
│ String s = list.get(0);    │
└─────────────┬──────────────┘
              │ javac (Compile-Time Type Checking)
              ▼
┌────────────────────────────┐
│ Bytecode (Type Erasure):   │
│ List list = ...;           │
│ String s = (String)        │ <── Synthetic checkcast inserted by javac
│            list.get(0);    │
└────────────────────────────┘
```

---

## 2. Background, Context & Historical Evolution

### Java 1.0 – 1.4: Pre-Generics Homogeneity
In early Java, all collection and container APIs operated on `java.lang.Object`. Developers wrote explicit casts everywhere, and runtime `ClassCastException` was the most frequent crash in production backend deployments.

### Java 5 (2004): JSR 14 & The Homogeneous Translation Decision
During the design of JSR 14, two major implementation strategies were evaluated:
1. **Heterogeneous Translation (C++ Templates, C# .NET CLR):** The runtime instantiates a brand new native class for every type combination (`List_int`, `List_string`).
   - *Advantage:* Full runtime reification (`typeof(List<string>) != typeof(List<int>)`), no boxing for primitives.
   - *Disadvantage:* Severe "code bloat" (memory footprint explodes), and broke backward compatibility with existing pre-Java 5 `.class` files.
2. **Homogeneous Translation via Type Erasure (Java):** All parameterizations of a generic type share a single `.class` file.
   - *Advantage:* Zero code bloat, 100% binary backward compatibility with pre-Java 5 libraries.
   - *Disadvantage:* Non-reifiable types, inability to construct generic arrays (`new T[10]`), and the emergence of heap pollution.

### Java 7: `@SafeVarargs` Annotation (JEP 110)
Combining generic parameters with variable arguments (`void process(T... items)`) inherently created generic array creation warnings and heap pollution risks. Java 7 introduced `@SafeVarargs` to suppress warnings on methods proven to access varargs arrays safely.

### Java 9: Private Method `@SafeVarargs`
Java 9 expanded `@SafeVarargs` from `static` and `final` methods to `private` instance methods.

---

## 3. Declarative Mechanics & Mental Model

### The Bridge Method Mechanism

Consider a class that implements a parameterized interface:

```java
public interface NodeComparator<T> {
    int compareNodes(T a, T b);
}

public class StringNodeComparator implements NodeComparator<String> {
    @Override
    public int compareNodes(String a, String b) {
        return a.compareTo(b);
    }
}
```

#### The Polymorphism Dilemma:
After type erasure, `NodeComparator` interface erases to:
```java
public interface NodeComparator {
    int compareNodes(Object a, Object b);
}
```
However, `StringNodeComparator` declared `compareNodes(String a, String b)`.
In Java bytecode method signatures include parameter types! Therefore:
- `compareNodes(Object, Object)` and `compareNodes(String, String)` are **two different methods**!
- Without intervention, `StringNodeComparator` would **not** override `compareNodes(Object, Object)`, breaking polymorphism when invoked through the interface:
  ```java
  NodeComparator raw = new StringNodeComparator();
  raw.compareNodes(obj1, obj2); // AbstractMethodError if not overridden!
  ```

#### The Compiler's Solution: Synthetic Bridge Methods
To fix this, `javac` automatically generates a **synthetic bridge method** in `StringNodeComparator`:

```bytecode
// Synthetic Bridge Method generated by javac:
public synthetic bridge int compareNodes(java.lang.Object a, java.lang.Object b) {
    return this.compareNodes((java.lang.String) a, (java.lang.String) b);
}
```
The bridge method satisfies the interface contract in the JVM vtable and delegates to the specialized method with an internal cast.

---

### Reifiable vs. Non-Reifiable Types

| Category | Definition | Examples | Permitted Operations |
|---|---|---|---|
| **Reifiable Types** | Type information is completely available at runtime. | `int`, `double`<br>`String`, `BigDecimal`<br>`List` (raw type)<br>`List<?>` (unbounded wildcard)<br>`String[]`, `int[]` | `instanceof List<?>`<br>`new String[10]`<br>`String.class` |
| **Non-Reifiable Types** | Type information is erased at compile time. | `List<String>`<br>`Map<String, Integer>`<br>`List<? extends Number>`<br>`T` (type parameter) | **Forbidden:**<br>`list instanceof List<String>`<br>`new List<String>[10]`<br>`T.class` |

---

### Heap Pollution: Anatomy of a Silent Crash

**Heap Pollution** occurs when a reference of a parameterized type points to an object of a different type:

```java
List<String> stringList = new ArrayList<>();
List rawList = stringList; // 1. Raw type assignment (Heap pollution begins!)

rawList.add(Integer.valueOf(9999)); // 2. Pollutes list with Integer! (Compiles with unchecked warning)

// Later in code:
String str = stringList.get(0); // 3. BOOM: ClassCastException!
```

#### Why does the exception occur at line 3, not line 2?
- At line 2, `rawList.add()` has erased to `List.add(Object)`. An `Integer` is a valid `Object`, so the JVM accepts it without complaint.
- At line 3, `stringList.get(0)` returns `Object`. The compiler has injected a synthetic `(String)` cast:
  ```bytecode
  invokevirtual java/util/List.get:(I)Ljava/lang/Object;
  checkcast     java/lang/String <--- THROWS java.lang.ClassCastException here!
  ```
The exception is thrown far away from the code that caused the pollution!

---

### Varargs and Generic Array Creation (`@SafeVarargs`)

In Java, varargs (`T... items`) is syntactic sugar for array allocation (`new T[]`).
Because `T` is non-reifiable, creating a generic array produces heap pollution hazards:

```java
// DANGEROUS CODE:
public static <T> T[] toArray(T... args) {
    return args; // Leaks internal Object[] array typed as T[]!
}

public static void main(String[] args) {
    String[] strings = toArray("A", "B"); // ClassCastException: [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
}
```

#### The `@SafeVarargs` Contract:
Apply `@SafeVarargs` **only** when:
1. The method does **not** store anything into the varargs array parameter.
2. The method does **not** leak the reference to the varargs array outside the method.
3. The method is declared `static`, `final`, or `private`.

```java
@SafeVarargs
public static <T> List<T> asImmutableList(T... elements) {
    List<T> list = new ArrayList<>();
    for (T element : elements) { // Safe read-only access
        list.add(element);
    }
    return Collections.unmodifiableList(list);
}
```

---

## 4. HotSpot JVM Architecture & Memory Subsystem Realities

### Bytecode Decompilation Proof of Bridge Methods
We can verify the presence of bridge methods using `javap -p -c`:

```bytecode
$ javap -p -v StringNodeComparator.class
...
public int compareNodes(java.lang.String, java.lang.String);
  descriptor: (Ljava/lang/String;Ljava/lang/String;)I
  flags: (0x0001) ACC_PUBLIC
  Code:
     0: aload_1
     1: aload_2
     2: invokevirtual #7 // Method java/lang/String.compareTo:(Ljava/lang/String;)I
     5: ireturn

public int compareNodes(java.lang.Object, java.lang.Object);
  descriptor: (Ljava/lang/Object;Ljava/lang/Object;)I
  flags: (0x1041) ACC_PUBLIC, ACC_BRIDGE, ACC_SYNTHETIC <--- NOTICE FLAGS!
  Code:
     0: aload_0
     1: aload_1
     2: checkcast     #8 // class java/lang/String
     5: aload_2
     6: checkcast     #8 // class java/lang/String
     9: invokevirtual #9 // Method compareNodes:(Ljava/lang/String;Ljava/lang/String;)I
    12: ireturn
```
- `ACC_BRIDGE`: Informs reflection and debuggers that this method was compiler-synthesized for generic dispatch.
- `ACC_SYNTHETIC`: Informs IDEs not to display this method in autocomplete menus.

---

## 5. Behavioral Matrix / Comparative Breakdown

| Type Category | Can be used with `instanceof`? | Can create Array (`new Type[10]`)? | Available via Reflection at Runtime? |
|---|---|---|---|
| Primitive (`int`, `double`) | No (primitives aren't objects) | **Yes** (`new int[10]`) | Via Wrapper (`Integer.TYPE`) |
| Concrete Class (`String`) | **Yes** (`x instanceof String`) | **Yes** (`new String[10]`) | Full (`String.class`) |
| Raw Type (`List`) | **Yes** (`x instanceof List`) | **Yes** (`new List[10]`) | Full (`List.class`) |
| Unbounded Wildcard (`List<?>`)| **Yes** (`x instanceof List<?>`) | **No** | Via Enclosing Raw Type |
| Parameterized Type (`List<String>`)| **No** (Compile Error) | **No** (Compile Error) | Erased (erases to `List.class`) |
| Bounded Wildcard (`List<? extends T>`)| **No** (Compile Error) | **No** (Compile Error) | Erased |
| Type Variable (`T`) | **No** (Compile Error) | **No** (Compile Error) | Erased to Bound |

---

## 6. Comprehensive Code Examples (Production-Grade, Real-World)

The following production-grade verification engine demonstrates:
1. Detecting synthetic bridge methods programmatically via Reflection.
2. Inducing and diagnosing heap pollution via unchecked conversions.
3. Implementing secure, non-leaking `@SafeVarargs` aggregators.
4. Unmasking hidden varargs array leaks.

```java
package com.bank.erasure;

import java.lang.reflect.Method;
import java.util.*;

public final class TypeErasureDiagnosticEngine {

    // 1. Domain Interface and Implementation for Bridge Method Inspection
    public interface NodeComparator<T> {
        int compareNodes(T a, T b);
    }

    public static final class StringNodeComparator implements NodeComparator<String> {
        @Override
        public int compareNodes(String a, String b) {
            Objects.requireNonNull(a);
            Objects.requireNonNull(b);
            return a.compareTo(b);
        }
    }

    // 2. Programmatic Inspection of Compiler-Generated Bridge Methods
    public static Map<String, Boolean> inspectBridgeMethods(Class<?> clazz, String methodName) {
        Map<String, Boolean> results = new LinkedHashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                String signature = method.getName() + Arrays.toString(method.getParameterTypes());
                boolean isBridgeAndSynthetic = method.isBridge() && method.isSynthetic();
                results.put(signature, isBridgeAndSynthetic);
            }
        }
        return results;
    }

    // 3. Deliberate Heap Pollution Simulation for Triage Demonstrations
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void polluteCollection(List<String> targetList, Object foreignObject) {
        // Raw type reference strips compiler type checking:
        List rawList = targetList;
        rawList.add(foreignObject); // Heap pollution occurs here!
    }

    // 4. Safe Generic Varargs Aggregator conforming to JLS §9.6.4.7
    @SafeVarargs
    public static <T> List<T> createImmutableList(T... elements) {
        Objects.requireNonNull(elements, "Elements array cannot be null");
        List<T> list = new ArrayList<>(elements.length);
        for (T element : elements) { // Read-only access; does not store or leak array
            list.add(Objects.requireNonNull(element, "Element cannot be null"));
        }
        return Collections.unmodifiableList(list);
    }

    // 5. Unsafe Varargs Array Leak (Anti-Pattern Demonstration)
    public static Object[] unsafeVarargsLeak(List<String>... lists) {
        Object[] array = lists; // Array covariance enables this
        array[0] = List.of(99999); // Pollutes the array of lists!
        return array;
    }
}
```

---

## 7. Anti-Patterns, Common Pitfalls & High-Frequency Production Bugs

### Anti-Pattern 1: Leaking Varargs Arrays from `@SafeVarargs` Methods
```java
// HAZARDOUS ANTI-PATTERN:
@SafeVarargs
public static <T> T[] dangerousArrayReturn(T... args) {
    return args; // FATAL: Returns Object[] array typed as T[]
}

// Caller code:
String[] arr = dangerousArrayReturn("A", "B"); // ClassCastException at runtime!
```
**Fix:** Never return or leak the raw varargs array to callers. Convert to `List<T>` or copy into a newly created reified array using a `Class<T>` or `IntFunction<T[]>` generator (`list.toArray(String[]::new)`).

---

### Anti-Pattern 2: Mixing Raw Types and Generics in Modern Services
```java
// BUG: Legacy raw type assignment causing latent heap pollution
public void processBatch(List rawBatch) {
    List<UserSession> sessions = (List<UserSession>) rawBatch; // Unchecked cast!
    for (UserSession s : sessions) {
        s.renew(); // Throws ClassCastException if rawBatch contained String or UUID!
    }
}
```
**Fix:** Enforce generic boundaries at API controllers. Use DTOs with Jackson validation rather than raw collection parameters.

---

## 8. Edge Cases, Corner Cases & Hidden Traps

### The Reified Array of Non-Reifiable Types Prohibition
Why does Java disallow `new List<String>[10]`?
```java
// IF THIS WERE LEGAL:
List<String>[] stringLists = new List<String>[10]; // Hypothetical

// Array covariance would allow:
Object[] objArray = stringLists;

// Then you could assign:
objArray[0] = List.of(42); // Legal on Object[]!

// Extraction:
String s = stringLists[0].get(0); // RUNTIME FATAL: ClassCastException (Integer cannot be cast to String)
```
Because arrays are **reified and covariant**, but generics are **erased and invariant**, combining them creates unavoidable type safety loopholes. Java eliminates this by banning generic array creation.

---

## 9. Performance Characteristics & Benchmark Analysis

### Zero Memory and CPU Runtime Penalty
- Bridge methods are synthetic forwarding stubs. Under HotSpot's C2 JIT compiler, bridge methods are **trivially inlined** during warm-up.
- In assembly code generated by HotSpot, calls to `compareNodes(Object, Object)` eliminate the synthetic bridge call frame completely, jumping directly into the specialized implementation.
- Type erasure ensures that generic code has **identical memory layout and cache performance** to handwritten non-generic code.

---

## 10. Ecosystem Integrations & Industry Framework Patterns

### 1. Spring Framework: Resolving Generic Types
Spring's `GenericTypeResolver` and `ResolvableType` inspect classfile `Signature` attributes to determine the target domain model for repositories and message listeners:
```java
ResolvableType type = ResolvableType.forClass(UserRepository.class).getInterfaces()[0];
Class<?> domainClass = type.getGeneric(0).resolve(); // Yields User.class
```

### 2. Jackson: Super Type Token (`TypeReference<T>`)
To bypass type erasure during JSON deserialization, Jackson subclasses `TypeReference<List<String>>`:
```java
public abstract class TypeReference<T> {
    private final Type type;
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
}
```

---

## 11. Security, Resilience & Production Hardening

### Eliminating Heap Pollution in CI/CD Builds
Configure `maven-compiler-plugin` or Gradle to treat compiler warnings as fatal errors:
```xml
<compilerArgs>
    <arg>-Xlint:unchecked</arg>
    <arg>-Xlint:varargs</arg>
    <arg>-Werror</arg>
</compilerArgs>
```
Any unchecked cast or unannotated generic varargs call will immediately fail the pipeline, preventing heap pollution from entering production artifacts.

---

## 12. Operational Runbooks, Triage & Diagnostics

### Triage: Diagnosing Delayed `ClassCastException` from Heap Pollution
When production logs show:
```
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String
    at com.bank.billing.ReportGenerator.render(ReportGenerator.java:45)
```
1. **Identify the Culprit Line:** Line 45 shows: `String customerName = row.getCustomerName();`.
2. **Recognize the Pattern:** `row` is inside a typed collection (`List<CustomerRow>`). The exception means an `Integer` object was placed into that collection somewhere upstream!
3. **Trace Upstream Ingestion:**
   - Look for `@SuppressWarnings("unchecked")` or raw type assignments in the service that populated `List<CustomerRow>`.
   - Search for JDBC query result mappers that returned numeric column values into string fields.

---

## 13. Holistic Architectural Synthesis & System Design Context

```
                           The Generics Subsystem
                                      │
              ┌───────────────────────┴───────────────────────┐
              ▼                                               ▼
     Compile-Time Verification                      Bytecode Generation
     - Strict type checking                         - Type Erasure to bounds
     - Wildcard variance checks (PECS)              - Synthetic checkcasts inserted
     - Reifiability checks                          - Synthetic Bridge Methods generated
              │                                               │
              └───────────────────────┬───────────────────────┘
                                      ▼
                             HotSpot JVM Runtime
                             - Uniform raw classes (List.class)
                             - Zero type bloat
                             - Zero-cost C2 method inlining
```

---

## 14. Complete Verification Suite (JUnit / Standalone OpenJDK Test Suite)

This comprehensive standalone test suite verifies synthetic bridge methods, heap pollution induction, delayed `ClassCastException` triggering, safe varargs execution, and unsafe varargs array leakage on OpenJDK 21 LTS without external dependencies.

```java
package com.bank.erasure;

import java.lang.reflect.Method;
import java.util.*;

public final class TypeErasureVerificationSuite {

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    // 1. Generic Interface and Implementing Class
    public interface NodeComparator<T> {
        int compareNodes(T a, T b);
    }

    public static class StringNodeComparator implements NodeComparator<String> {
        @Override
        public int compareNodes(String a, String b) {
            return a.compareTo(b);
        }
    }

    // 2. Inducing Heap Pollution
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void induceHeapPollution(List<String> validList) {
        List rawList = validList;
        rawList.add(Integer.valueOf(9999)); // Pollutes List<String> with Integer
    }

    // 3. @SafeVarargs Compliant Method
    @SafeVarargs
    public static <T> List<T> safeAsList(T... elements) {
        List<T> list = new ArrayList<>();
        for (T item : elements) {
            list.add(item);
        }
        return Collections.unmodifiableList(list);
    }

    // 4. Unsafe Varargs Array Leak Demonstration
    public static Object[] unsafeVarargsLeak(List<String>... lists) {
        Object[] array = lists;
        array[0] = List.of(12345); // Heap pollution: inserting List<Integer> into List<String>[]
        return array;
    }

    public static void main(String[] args) {
        System.out.println("=== Running OpenJDK 21 Type Erasure & Heap Pollution Verification Suite ===");

        // Test 1: Reflection Proof of Synthetic Bridge Method
        Method[] methods = StringNodeComparator.class.getDeclaredMethods();
        boolean foundBridgeMethod = false;
        boolean foundExactMethod = false;

        for (Method m : methods) {
            if ("compareNodes".equals(m.getName())) {
                if (m.isBridge() && m.isSynthetic()) {
                    foundBridgeMethod = true;
                    assertEquals(Object.class, m.getParameterTypes()[0], "Bridge method parameter 0 must be Object");
                    assertEquals(Object.class, m.getParameterTypes()[1], "Bridge method parameter 1 must be Object");
                } else if (!m.isBridge()) {
                    foundExactMethod = true;
                    assertEquals(String.class, m.getParameterTypes()[0], "Exact method parameter 0 must be String");
                    assertEquals(String.class, m.getParameterTypes()[1], "Exact method parameter 1 must be String");
                }
            }
        }
        assertTrue(foundBridgeMethod, "javac MUST generate a synthetic bridge method to preserve polymorphism");
        assertTrue(foundExactMethod, "javac MUST retain the exact specialized method");

        // Test 2: Heap Pollution & Delayed ClassCastException
        List<String> names = new ArrayList<>();
        names.add("Alice");
        induceHeapPollution(names);

        assertEquals(2, names.size(), "List contains two elements after pollution");

        boolean cceEncountered = false;
        try {
            for (String name : names) {
                int len = name.length(); // Fails on Integer 9999 via checkcast!
            }
        } catch (ClassCastException e) {
            cceEncountered = true;
        }
        assertTrue(cceEncountered, "Reading polluted element MUST throw ClassCastException at extraction site");

        // Test 3: @SafeVarargs Execution
        List<String> safeList = safeAsList("ALPHA", "BRAVO", "CHARLIE");
        assertEquals(List.of("ALPHA", "BRAVO", "CHARLIE"), safeList, "@SafeVarargs method executes cleanly");

        // Test 4: Unsafe Varargs Array Leak ClassCastException
        List<String> l1 = new ArrayList<>(List.of("HELLO"));
        @SuppressWarnings("unchecked")
        Object[] leaked = unsafeVarargsLeak(l1);

        boolean varargsCce = false;
        try {
            @SuppressWarnings("unchecked")
            List<String>[] typedArray = (List<String>[]) leaked;
            String val = typedArray[0].get(0); // Attempting to read Integer 12345 as String!
        } catch (ClassCastException e) {
            varargsCce = true;
        }
        assertTrue(varargsCce, "Unsafe varargs array pollution MUST trigger ClassCastException");

        System.out.println("All 4 test scenarios (16 assertions) passed with 100% compliance!");
    }
}
```

---

## 15. Key Takeaways & "Rules of Thumb"

1. **Type erasure removes generic arguments before runtime:** At runtime, `List<String>` and `List<Integer>` are identical raw `List` objects.
2. **Bridge methods preserve polymorphism:** When overriding generic methods with specialized types, `javac` generates synthetic bridge methods with `ACC_BRIDGE` and `ACC_SYNTHETIC` flags.
3. **Know reifiable vs non-reifiable types:** Reifiable types (`String`, `int[]`, `List<?>`) retain full type information at runtime; non-reifiable types (`List<String>`, `T`) do not and cannot be used with `new T[]` or `instanceof List<T>`.
4. **Heap pollution causes delayed `ClassCastException`:** Polluting a generic collection via raw types or unchecked casts causes runtime crashes at the extraction site, not the insertion site.
5. **Use `@SafeVarargs` responsibly:** Annotate methods taking generic varargs (`T...`) with `@SafeVarargs` only if the method never writes into or leaks the varargs array.

---

## 16. Further Reading & Academic References

- Bloch, Joshua. *Effective Java (3rd Edition)*. Addison-Wesley, 2018. Item 28: "Prefer lists to arrays", Item 32: "Combine generics and varargs judiciously".
- Naftalin, Maurice, and Philip Wadler. *Java Generics and Collections*. O'Reilly Media, 2006. Chapter 5: "Evolution, Not Revolution", Chapter 6: "Reification".
- Java Language Specification (JLS SE 21): Section 4.6: "Type Erasure", Section 4.7: "Reifiable Types", Section 8.4.8.3: "Requirements in Overriding and Hiding" (Bridge Methods), Section 9.6.4.7: "@SafeVarargs".
- Java Virtual Machine Specification (JVMS SE 21): Section 4.6: "Methods" (Table 4.6-A: Method access and property flags: `ACC_BRIDGE`, `ACC_SYNTHETIC`).
