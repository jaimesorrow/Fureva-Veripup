---
name: kotlin-service-conventions
description: Structural and testing conventions for this repo's plain Kotlin/JVM service layer (package layout, class naming, immutability, construction, and the 8 JUnit5 test files' style) — distinct from veripup-policy-review's business-rule invariants. Use when adding a new service, config object, or test file, or reviewing one for consistency with the rest of the codebase.
---

# Kotlin service-layer conventions

This is a small (~8 main files) plain Kotlin/JVM library — no Spring, no Android, no framework DI.
Everything is a constructor-injected class or a stateless `object`.

**File/package structure:** `src/main/kotlin/com/fureva/veripup/` splits by role, one concern per
package: `model/` (data classes/enums, one `Models.kt`), `service/` (business logic),
`integration/` (external-system interfaces + `Mock*` stand-ins, one `Providers.kt`), `config/`
(static reference data, e.g. `StateCityConfig`), `design/` (brand tokens), `screen/` (route
scaffolding). Tests mirror this under `src/test/kotlin/com/fureva/veripup/` but are **not**
package-split — all 8 classes sit flat in the root test package, one per service, plus
`CoreFlowTests` for cross-service scenarios.

**Class naming:** a service class is `<Noun>Service` (e.g. `InventoryService`); a static/constant
holder that isn't really a "service" is a Kotlin `object` (`FeePolicy`, `StateCityConfig`,
`Brand`) — don't add a no-op constructor to something that should be an `object`.

**Construction / DI:** no framework — services take dependencies as plain constructor params,
typed to an `integration` interface, never a concrete `Mock*` (e.g. `VerificationService(
clinicVerificationProvider: ClinicVerificationProvider, akcVerificationProvider:
AkcVerificationProvider)`). Some services (`InventoryService`, `BreederOnboardingService`,
`EnforcementService`) have no dependencies and take a no-arg constructor — don't add unnecessary
params to a service that's really a pure function holder.

**Immutability / result flow:** every entity is an immutable `data class`; a "mutation" is always
`.copy(...)` returning a new instance (see `EnforcementService`). Return types are plain
`Boolean`/`Int`/`List<String>`/entity — no sealed `Result`/`Either` wrapper anywhere in this repo.

**Testing conventions:** JUnit5 via `kotlin.test` (`kotlin.test.Test`, `assertTrue`/`assertFalse`/
`assertEquals`), run through Gradle's `useJUnitPlatform()`. Tests use ASCII camelCase names that
read as a sentence (`cannotAcceptDepositWhenAllSlotsReserved`), not backtick-quoted strings. Files
use `// ── Section ──` banners grouping tests by method under test, and a private top-of-class
factory function (`verifiedRecord(...)`, `validSubmission(...)`) with sensible defaults that
individual tests override via named args or `.copy(...)` — follow this for new test files.
