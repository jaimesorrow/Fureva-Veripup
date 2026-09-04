---
name: data-model
description: Reference for Fureva VeriPup's domain model (com.fureva.veripup.model.Models.kt) — the data classes, enums, and anti-fraud validation constants every service is built around. Use this when adding a field, a new entity, or a new service method, or when reviewing a change for consistency with the existing shape of the model.
---

# VeriPup data model

All entities live in one file: `src/main/kotlin/com/fureva/veripup/model/Models.kt`. There is no
database or ORM — every type is a plain immutable `data class`, and "the model" means exactly
what's declared there.

**Primary entities**
- `BreederProfile(id, name, stateCode, city, verifiedStatus, legacyStatus, active, deactivatedAt,
  appealStatus, appealOpenedAt)` — the enforcement/trust record for a breeder.
- `VerificationSubmission(breederId, governmentIdReadable, idMatchesSignup,
  firstLiveVideoPassedDeepfake, secondLiveVideoPassedDeepfake, vetDocsUploaded,
  vetDocsMatchBreed, optionalAkcNumber, clinicPhone, roiSigned)` — input to `VerificationService.approve`.
- `BreederOnboardingSubmission(breederId, governmentIdUploaded, photoHoldingGovernmentIdUploaded,
  vetRecordsUploaded, vetRecordsCoverBreedingDogs, acceptedAgreements, signedAt)` — input to
  `BreederOnboardingService`.
- `LitterRecord(breederId, expectedLitterCount, dueDateConfirmed, verifiedByVet,
  listedAvailablePuppies, reservedDeposits, completedAdoptions)` — the anti-fraud inventory source
  of truth consumed by `InventoryService`.
- `SmsPreference(userId, phoneNumber, optedInVerifiedAlerts)` — input to `AlertsService`.

**Enums:** `Role`, `TrustBadge`, `AppealStatus`, `VerifiedEventType`, `OnboardingAgreementType`
(5 required members — see `BreederOnboardingService.requiredAgreements`).

**Relationships:** every entity is keyed by a raw `breederId`/`userId` string, not an object
reference or foreign key — there's no join layer. A `BreederProfile` and a `LitterRecord` for the
same breeder are two independent objects a caller must fetch/pass separately.

**Validation rules (the actual gating constants):** `VerificationService.approve` requires 8
boolean checks true, including *two separate* deepfake passes; `optionalAkcNumber` is the only
field that's genuinely optional. `InventoryService` enforces
`reservedDeposits < expectedLitterCount - completedAdoptions` (deposits) and
`completedAdoptions < reservedDeposits` (adoptions can't outrun deposits) — this is the core
anti-fraud rule. `EnforcementService.isAdminDecisionOverdue` uses a strict `> 90` days. See
`veripup-policy-review` for the full list — don't duplicate it here.

**Query patterns:** there is no repository/DAO/database. Every "query" is a plain constructor call
or a service method taking a fully-populated entity as a parameter — callers are responsible for
assembling a `LitterRecord`/`VerificationSubmission`/etc. from wherever their data actually lives
(there is no persistence layer in this repo at all).

**Mutations:** entities are never mutated in place. `EnforcementService` is the only place that
"changes" a `BreederProfile`, and it does so via `.copy(...)`, returning a new instance.
