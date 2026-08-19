# Fureva Veripup

Kotlin domain foundation for **Fureva Veripup**, a mobile-first Alaska puppy marketplace for
verified breeders, litter marketing, buyer screening, protected deposits, and statewide
pickup/transport coordination. The core product concept is a shareable **Litter Page** that lets
a breeder market a full litter before creating individual puppy listings.

## Included in this codebase

- Domain models covering the full litter-marketing data model: users/roles, breeder profiles,
  verification submissions, parent/puppy dogs, litters, litter media, litter updates, health
  records, applications, waitlist entries, reservations/deposits, messaging, and reports
  (`model/Models.kt`).
- `AlaskaRegionConfig`: Alaska region -> city map (Anchorage, Mat-Su, Fairbanks North Star, Kenai
  Peninsula, Juneau, and more) used for onboarding, search, and discovery filters.
- Brand constants and verification badge copy (`design/Brand.kt`).
- Provider interfaces for external integrations (object storage/CDN, malware scanning, payments,
  SMS, email), each with an in-memory mock for tests.
- Core policy services:
  - `BreederVerificationService` — admin-only badge progression
    (`unverified -> identity_verified -> documents_verified -> trusted_breeder`), litter-creation
    gating on email/phone verification, and the no-unverified-USDA-claim rule.
  - `LitterCampaignService` — campaign stage ordering, required puppy listing fields, the
    cautious 8-week minimum go-home age default, available-count recalculation, and publish
    gating.
  - `ApplicationWaitlistService` — application status transitions and waitlist ranking.
  - `ReservationService` — reservation deposits that require an explicit refund-policy
    acknowledgement, idempotent payment webhook handling, and admin freeze/refund.
  - `ModerationService` — reports intake and an immutable, reason-required audit log.
  - `ListingComplianceService` — prohibited-claim and prohibited-species text checks, and
    public location display (city/region only, never a street address).
  - `MessagingService` — account-gated conversations and messages.
- Role-based screen route scaffolding for Buyer, Breeder, Admin, and Transport-provider app
  areas (`screen/Screens.kt`).
- Automated tests for every service plus an end-to-end flow test that walks an Anchorage litter
  from breeder onboarding through publish, deposit, and a resolved report.

## Quick start

```bash
./gradlew test
```

## Notes

- This repository intentionally keeps external integrations as interfaces so production
  providers (S3/R2, Stripe, Twilio, a transactional email service) can be swapped in later.
  Database persistence, the REST API layer, and the actual front end are not implemented here —
  this is the shared domain/policy layer they would sit on top of.
- The 8-week minimum go-home age is a cautious platform default, not a representation of Alaska
  law; confirm the correct rule with legal review before launch.
- Never represent a breeder as "USDA approved" without a verified license, and never use
  blanket "health certified" language — use specific, document-backed statements instead.
  `ListingComplianceService` enforces both.

## Credibility & Attribution

This project was developed with AI-assisted co-creation focused on system architecture,
compliance design, and enforcement logic.

AI-assisted contributions supported verification workflows, documentation structure,
and platform governance. All implementation, deployment, and operational decisions
remain the responsibility of the project owner.
