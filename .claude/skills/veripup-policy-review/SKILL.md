---
name: veripup-policy-review
description: Reviews changes to Fureva VeriPup's Kotlin policy layer (com.fureva.veripup.service — VerificationService, BreederOnboardingService, InventoryService, EnforcementService, AlertsService/FeePolicy — plus config.StateCityConfig, integration provider interfaces, and design.Brand copy) against this codebase's actual anti-fraud gating rules, immutability, and required-copy conventions. Use this instead of a generic code review for any change touching src/main/kotlin/com/fureva/veripup.
---

# VeriPup policy-layer review

This repo (`fureva-veripup`) is a small policy-only Kotlin/JVM library (no Android/Compose code
yet — plain `org.jetbrains.kotlin.jvm`, JUnit5/kotlin-test). Its whole job is to encode Fureva
VeriPup's breeder-verification and anti-fraud rules as plain services with no I/O; every rule
below is inferred from the actual service code and its existing tests, not generic advice. Keep
review scope proportionate — this is ~8 main files and ~8 test files, not a large app.

Checklist:

- **`VerificationService.approve`** requires *all* of: readable government ID, ID-matches-signup,
  a **first** `firstLiveVideoPassedDeepfake` AND a separate **second**
  `secondLiveVideoPassedDeepfake`, vet docs uploaded + matching breed, ROI signed, and
  `clinicVerificationProvider.clinicExistsByPhone`. Flag any change that lets one deepfake/liveness
  pass stand in for both, or that short-circuits before checking all eight `requiredChecks`.
  `optionalAkcNumber` is the only genuinely optional field — absent, it must not block approval;
  present, it must go through `akcVerificationProvider.verifyMemberNumber` and can still fail
  approval. A change that treats a bad/absent AKC number the same way is wrong.
- **`BreederOnboardingService`** keeps `isReadyForVerification` and `missingRequirements` as two
  independently-written checklists over the same fields/`requiredAgreements` set. If a review adds
  a new required field or a new `OnboardingAgreementType`, verify **both** methods were updated —
  it's easy to add a check to one and silently leave the other one out of sync (e.g. a submission
  that reads "ready" but reports no missing requirements, or vice versa).
- **`InventoryService`** enforces the anti-fraud litter cap: `canAcceptDeposit` requires
  `reservedDeposits < expectedLitterCount - completedAdoptions`, and — this is the fraud-specific
  rule — `canCompleteAdoption` requires `completedAdoptions < reservedDeposits` in addition to
  `completedAdoptions < expectedLitterCount`, i.e. **a puppy cannot be adopted out ahead of a
  deposit being reserved for it**. `cappedAvailability` must never exceed
  `max(0, expectedLitterCount - completedAdoptions)` even when `listedAvailablePuppies` is
  breeder-supplied and larger. Any change that lets adoptions, deposits, or listed availability
  outrun the vet-confirmed `expectedLitterCount` defeats the point of this service.
- **`EnforcementService`**: `markOffPlatformViolation`/`resolveAppeal` must stay pure (`.copy(...)`,
  no mutation of the input `BreederProfile` — tests assert the original is untouched). The 90-day
  appeal window in `isAdminDecisionOverdue` is a strict `> 90` days, not `>= 90` — day 90 exactly is
  not overdue (see `decisionNotOverdueOnExactlyDay90`); watch for off-by-one changes here since it's
  a compliance deadline, not a UI nicety. A profile with no `appealOpenedAt` must return `false`,
  never throw.
- **`AlertsService`**: `sendVerifiedUpdate` must stay gated on `preference.optedInVerifiedAlerts`
  (no send when opted out) and must keep prefixing every outgoing message with the literal
  `"Verified Update: "` string — this is the "verified alert" promise, not incidental formatting.
  `canTriggerEvent` gates every `VerifiedEventType` on `breederVerified`; don't let a new event type
  bypass that gate as the `when` is extended.
- **`FeePolicy`**: `depositFeeRate` (0.07), `adoptionFeeRate` (0.08), and `subscriptionMonthlyUsd`
  (29.99) are product-fixed constants — a change to any of them is a pricing decision, not a
  refactor, and should be called out explicitly. Fee math is `(amountCents * rate).toLong()`,
  i.e. truncation toward zero, not rounding; keep that behavior unless the change deliberately
  addresses it.
- **`StateCityConfig`**: `topCitiesByState` must keep exactly the 50 U.S. state codes (no DC/
  territories, per `allFiftyStatesArePresent`) with at least 7 cities each; `citiesFor` must stay
  case-insensitive (`.uppercase()` lookup) and return `emptyList()` — never `null` or a thrown
  exception — for an unknown or blank code.
- **`design.Brand`**: the README states UI copy must use "VeriPup Verified breeders" or "verified
  breeders" wording; treat any edit to `heroPrimary`/`heroSecondary`/`heroTertiary` or the palette
  as a deliberate copy/brand decision needing sign-off, not a drive-by tweak.
- **`integration` package**: `DeepfakeProvider`, `ClinicVerificationProvider`,
  `AkcVerificationProvider`, `SmsGateway`, `PaymentProcessor` are meant to stay thin external-facing
  interfaces; the `Mock*` implementations are test/dev stubs only (e.g.
  `MockDeepfakeProvider` just checks a `"live_"` prefix). Reject any approval/fraud/eligibility
  *decision* logic added inside a provider or its mock — that belongs in the `service` layer above,
  which is what the zero-hallucination-adjacent separation here is for: providers answer yes/no
  facts, services decide policy.
- **`screen.Screens`**: `UserScreen`/`BreederScreen`/`AdminScreen` are route scaffolding split by
  role. A new admin-only capability (fraud, appeals, enforcement, audit) belongs under
  `AdminScreen`, not `UserScreen`/`BreederScreen` — check role placement on any new route.
