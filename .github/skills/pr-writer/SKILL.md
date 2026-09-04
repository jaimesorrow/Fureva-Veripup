---
name: pr-writer
description: Write pull request descriptions for Fureva VeriPup in the structure this repo's actual merged PRs use — Motivation/Description/Testing, one logical change per PR, honest disclosure of what testing actually happened. Use when opening or updating a PR in this repo.
---

# PR descriptions for this repo

This repo's merged PRs (#1 scaffolding the core, #3 adding breeder onboarding, #9 adding the full
test suite) consistently use a 3-section body. Match it:

```markdown
### Motivation
- Why this change is needed, in product/policy terms ("reduce fraud," "close a coverage gap"),
  not just what the diff does.

### Description
- One bullet per file or unit of change, naming the actual type/function added or changed
  (`Added BreederOnboardingService with isReadyForVerification and missingRequirements`) — not
  vague summaries like "improved onboarding."

### Testing
- State exactly what was run and what the result was, including failures. PR #1 and #3 both
  honestly reported `./gradlew test` failing in the sandbox (`Unsupported class file major
  version`) rather than claiming green tests — do the same rather than asserting "tests pass"
  without running them. If `./gradlew test` (or a `--tests` filter) did pass, name the command.
```

**Scope discipline:** every merged PR here is one logical change — one new service, one new test
suite, one doc addition — not a grab-bag. #3 touched 4 files for one feature; #9 touched 8 files
but all were new test files for one "add coverage" goal. Don't bundle an unrelated fix or refactor
into a PR whose title describes something else.

**Title:** short, imperative; prefix a conventional-commit tag for test-only changes (`test: add
comprehensive unit tests for core services`), otherwise a plain phrase (`Add breeder onboarding
verification and agreement checks`).

**What to leave out:** don't fabricate a "Testing" section claiming a clean CI run or review
sign-off, and skip task-tracker/tool link footers — those are automation artifacts from how some
past PRs here were filed, not a format to imitate. If `veripup-ci-review` or `veripup-policy-review`
applies to the diff, say so in the body so a reviewer knows which invariants to re-check.
