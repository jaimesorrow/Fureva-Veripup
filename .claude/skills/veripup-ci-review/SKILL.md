---
name: veripup-ci-review
description: Reviews changes to CI/build wiring in Fureva VeriPup — this repo currently has NO .github/workflows directory at all, so its 8 JUnit5 test files (AlertsServiceTests, BreederOnboardingServiceTests, CoreFlowTests, EnforcementServiceTests, FeePolicyTests, InventoryServiceTests, StateCityConfigTests, VerificationServiceTests, covering fraud/appeal/fee-sensitive logic) run only if a human remembers to type `./gradlew test`. Use this instead of a generic code review for any change that adds or edits `.github/workflows/**`, or that touches `build.gradle`'s `test { }` block, dependency set, or Kotlin toolchain.
---

# VeriPup CI-enforcement review

**Current state (verify this hasn't silently changed before reviewing anything else):** there is no
`.github/workflows/` directory in this repo. README.md's "Included in this codebase" list claims
"Automated tests for critical behavior from policy requirements," and `build.gradle` wires
JUnit5 (`useJUnitPlatform()`) with 8 test files under `src/test/kotlin/com/fureva/veripup/`, but
none of that is enforced by CI today — a PR can be opened, reviewed, and merged without
`./gradlew test` ever running anywhere except a contributor's own machine. Treat that gap itself as
the finding until a workflow exists; don't let a PR description's "tests pass" stand in for a
machine-checked result.

Checklist for a PR that adds or changes CI/build wiring:

- **A workflow must actually run the tests, on the events that matter.** The first workflow this
  repo gets should trigger on `pull_request` (and ideally `push` to the default branch) and run
  `./gradlew test`. A workflow that only runs `assemble`/`build` without ever invoking the `test`
  task, or that triggers only on `workflow_dispatch`/a schedule, does not close this gap — flag it
  as CI-in-name-only.
- **No failure-masking.** Reject any test step with `continue-on-error: true`, a `|| true` (or
  equivalent) after the gradle invocation, or a job-level `continue-on-error` — any of these let a
  red test run show green in the PR checks, which is worse than no CI because it creates false
  confidence.
- **Toolchain must match `build.gradle`.** This project pins `kotlin { jvmToolchain(17) }`. A
  workflow that sets up a different JDK (8/11/21) may compile or behave differently than local runs
  — call out any JDK version in a `setup-java`/`actions/setup-java` step that isn't 17, and any
  Gradle version pin that diverges from this repo's usage (no wrapper is currently committed here —
  confirm whether one is added alongside the workflow, since `./gradlew` won't exist without it).
- **Don't let `build.gradle`'s `test { }` block quietly narrow coverage.** Flag any new
  `ignoreFailures = true`, `exclude`/`include` filter, or custom `filter { }` block added to the
  `test { }` task — with only 8 test files covering fraud-sensitive logic (deposit/adoption caps in
  `InventoryServiceTests`, the 90-day appeal deadline in `EnforcementServiceTests`, truncating fee
  math in `FeePolicyTests`), a narrowed test-matching pattern can silently drop a whole file's worth
  of assertions from every future CI run without the diff itself looking suspicious.
- **Confirm all 8 known test files stay wired.** If a workflow or Gradle change is paired with a
  test-file rename/move/deletion, check that the count and names of files under
  `src/test/kotlin/com/fureva/veripup/` are accounted for — a rename that silently drops a class
  from the default source set (e.g. a stray file extension, wrong package path) still "compiles
  green" while running fewer tests.
- **A workflow file alone isn't enforcement — say so.** Whether the workflow is a *required* status
  check on the default branch is a GitHub repo-settings concept (branch protection rules), not
  something visible in this diff. When a workflow is first added, explicitly note in review that a
  human still needs to mark it as a required check in GitHub branch protection, or merges can bypass
  it entirely even with the workflow present and green-lit.
- **Don't accept a claim in place of the workflow.** A PR description, commit message, or README
  edit that asserts "CI added" or "tests now run automatically" is not itself evidence — the
  workflow YAML must be present in the diff and must satisfy the checks above.
