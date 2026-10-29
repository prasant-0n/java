# Phase 19: Application Security

---

## 1. Phase Overview and Purpose

### 1.1 Goal
Master authentication, authorization, Spring Security filter chains, password hashing, JWTs, OAuth2/OIDC, and OWASP Top 10 defenses.

### 1.2 Context in Backend Engineering
Security breaches destroy companies and careers. Backend engineers must protect sensitive data, prevent broken access control (BOLA/IDOR), enforce least privilege, and prevent injection attacks.

---

## 2. Prerequisites and Co-requisites

- **Direct Prerequisites:**
  - Phase 15 (HTTP/REST), Phase 18 (Spring Boot).
- **Downstream Dependent Phases:**
  - Phase 20 (Production Backend), Phase 21 (Distributed Systems).

---

## 3. Measurable Phase Learning Outcomes

By completing Phase 19, the learner will independently be able to:
1. Explain Authentication vs Authorization and Identity and Access Management principles.
2. Configure the Spring Security 6 filter chain and security rules using SecurityFilterChain.
3. Hash and verify passwords securely with BCrypt, Argon2, and salt factors.
4. Implement stateless JWT authentication, validating claims, signatures, and handling token rotation.
5. Defend against OWASP Top 10 vulnerabilities: BOLA/IDOR, SQL injection, CSRF, CORS misconfigurations, and XSS.


---

## 4. Concept Inventory and Study Order

| Concept ID | Concept Title | File Name | Importance | Status |
|---|---|---|---|---|
| `P19-C001` | Authentication vs Authorization and Identity & Access Management (IAM) Principles | [`001-authentication-vs-authorization.md`](001-authentication-vs-authorization.md) | Essential | NOT_STARTED |
| `P19-C002` | Cryptographic Password Hashing: Salts, Work Factors, BCrypt, and Argon2 | [`002-password-hashing-bcrypt-argon2.md`](002-password-hashing-bcrypt-argon2.md) | Essential | NOT_STARTED |
| `P19-C003` | Spring Security Architecture: SecurityFilterChain, AuthenticationManager, and Context | [`003-spring-security-filter-chain.md`](003-spring-security-filter-chain.md) | Essential | NOT_STARTED |
| `P19-C004` | Role-Based Access Control (RBAC) vs Attribute-Based Access Control (ABAC) | [`004-rbac-vs-abac-authorization.md`](004-rbac-vs-abac-authorization.md) | Essential | NOT_STARTED |
| `P19-C005` | Method Security: @PreAuthorize, @PostAuthorize, and SpEL Expressions | [`005-method-security-preauthorize.md`](005-method-security-preauthorize.md) | Essential | NOT_STARTED |
| `P19-C006` | JSON Web Tokens (JWT) Architecture: Header, Payload, Signature, and Claims | [`006-jwt-architecture-claims.md`](006-jwt-architecture-claims.md) | Essential | NOT_STARTED |
| `P19-C007` | JWT Security Pitfalls: Weak Keys, 'none' Algorithm, Token Expiry, and Revocation | [`007-jwt-security-pitfalls-revocation.md`](007-jwt-security-pitfalls-revocation.md) | Essential | NOT_STARTED |
| `P19-C008` | OAuth 2.0 and OpenID Connect (OIDC) Protocols: Authorization Code Flow and Scopes | [`008-oauth2-oidc-protocols.md`](008-oauth2-oidc-protocols.md) | Essential | NOT_STARTED |
| `P19-C009` | CORS vs CSRF: Browser Security Mechanisms, SameSite Cookies, and API Security | [`009-cors-vs-csrf-mechanics.md`](009-cors-vs-csrf-mechanics.md) | Essential | NOT_STARTED |
| `P19-C010` | Broken Object-Level Authorization (BOLA/IDOR) and Multi-Tenant Isolation | [`010-bola-idor-tenant-isolation.md`](010-bola-idor-tenant-isolation.md) | Essential | NOT_STARTED |
| `P19-C011` | Security Logging, Audit Trails, Rate Limiting, and Secret Management | [`011-security-logging-rate-limiting.md`](011-security-logging-rate-limiting.md) | Essential | NOT_STARTED |
| `P19-C012` | Dependency Vulnerability Scanning: OWASP Dependency-Check, Snyk, and SBOMs | [`012-dependency-scanning-sboms.md`](012-dependency-scanning-sboms.md) | Recommended | NOT_STARTED |


### Recommended Pedagogical Sequence
```mermaid
flowchart LR
    P19C001["P19-C001: Authentication vs Authori..."] --> P19C002["P19-C002: Cryptographic Password Ha..."]
    P19C002["P19-C002: Cryptographic Password Ha..."] --> P19C003["P19-C003: Spring Security Architect..."]
    P19C003["P19-C003: Spring Security Architect..."] --> P19C004["P19-C004: Role-Based Access Control..."]
    P19C004["P19-C004: Role-Based Access Control..."] --> P19C005["P19-C005: Method Security: @PreAuth..."]
    P19C005["P19-C005: Method Security: @PreAuth..."] --> P19C006["P19-C006: JSON Web Tokens (JWT) Arc..."]
    P19C006["P19-C006: JSON Web Tokens (JWT) Arc..."] --> P19C007["P19-C007: JWT Security Pitfalls: We..."]
    P19C007["P19-C007: JWT Security Pitfalls: We..."] --> P19C008["P19-C008: OAuth 2.0 and OpenID Conn..."]
    P19C008["P19-C008: OAuth 2.0 and OpenID Conn..."] --> P19C009["P19-C009: CORS vs CSRF: Browser Sec..."]
    P19C009["P19-C009: CORS vs CSRF: Browser Sec..."] --> P19C010["P19-C010: Broken Object-Level Autho..."]
    P19C010["P19-C010: Broken Object-Level Autho..."] --> P19C011["P19-C011: Security Logging, Audit T..."]
    P19C011["P19-C011: Security Logging, Audit T..."] --> P19C012["P19-C012: Dependency Vulnerability ..."]

```

---

## 5. Critical Distinctions and Common Misconceptions

| Misconception / Simplification | Technical Reality from First Principles |
|---|---|
| "Storing authentication tokens in a stateless JWT means the backend never needs a revocation check." | If a user's account is compromised, rights revoked, or password changed, a stateless JWT remains valid until its expiration time unless a token revocation list, blocklist, or token version is checked. |
| "CORS is a security feature that protects your backend API from unauthorized requests." | CORS is a browser-enforced policy that prevents unauthorized client-side scripts from reading responses. It provides zero protection against direct curl, Postman, or server-to-server attacks. |


---

## 6. Phase Integration Project / Capstone Exercise

### Scenario Overview
Secure a Spring Boot API with Spring Security 6 using JWT bearer token authentication, strict method-level BOLA/IDOR ownership checks, BCrypt password hashing, and rate limiting.

### Requirements and Acceptance Criteria
- [ ] Requirements fully implemented and demonstrated with executable Java code.
- [ ] Zero unhandled exceptions or edge-case crashes.
- [ ] Verification through automated tests and terminal execution.

---

## 7. Phase Completion Gate and Self-Assessment

Before marking Phase 19 as complete:
- [ ] All concept modules in this phase are thoroughly studied and exercises completed.
- [ ] You can explain every concept in the table from memory without AI assistance.
- [ ] The capstone exercise compiles, passes automated tests, and handles edge cases.
- [ ] Common failure modes can be diagnosed and resolved using standard tooling.

---

## 8. Status and Progress Reconciler

- **Total Planned Concepts:** `12`
- **Completed Concepts:** `0`
- **In Progress:** `0`
- **Completion Rate:** `0%`
- **Last Verified Date:** `2026-10-29`
