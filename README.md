# Fureva VeriPup

Policy-focused Kotlin foundation for **Fureva VeriPup**, designed for Android + iOS-ready product architecture with shared domain rules, configurable state/city limits, verification stubs, and critical-flow tests.

## Included in this codebase

- Hard-coded `top 7 cities` map for all 50 U.S. states (`StateCityConfig`) used as canonical onboarding/search/filter source.
- Brand + messaging constants including required hero copy and palette tokens.
- Core domain models for breeders, verification submissions, litters, alerts, and appeals.
- Provider interfaces for external integrations (deepfake/liveness, clinic verification, AKC verification, SMS, payments).
- Core policy services:
  - verification gating,
  - anti-fraud inventory/deposit/adoption caps,
  - off-platform enforcement + 90-day appeal timing,
  - verified SMS alert gating and message prefixing,
  - fee policy (7% deposit, 8% final adoption, $29.99 subscription baseline).
- Role-based screen route scaffolding for User, Breeder, and Admin app areas.
- Automated tests for critical behavior from policy requirements.

## Quick start

```bash
./gradlew test
```

## Notes

- This repository intentionally keeps external integrations as interfaces so production providers can be swapped in later.
- Weekly reminder orchestration, payment processor settlement specifics, and document storage are represented at policy/service layer and can be wired into mobile/API apps.
- Use "VeriPup Verified breeders" or "verified breeders" wording in UI copy.
