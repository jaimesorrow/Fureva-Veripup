---
name: veripup-trust-status-review
description: Reviews changes to Fureva VeriPup's breeder trust-status lifecycle — the gap between VerificationService.approve's return value, BreederProfile.verifiedStatus, and EnforcementService's active/appealStatus fields, none of which any code in this repo currently reconciles. Use this instead of, or alongside, veripup-policy-review for any change that adds a caller/orchestration layer wiring VerificationService, EnforcementService, or AlertsService.canTriggerEvent's breederVerified param together, or that reads or writes BreederProfile.verifiedStatus.
---

# VeriPup trust-status reconciliation review

**The gap, concretely:** `BreederProfile.verifiedStatus` (`model/Models.kt`) is declared but never
read or written anywhere in `src/main/kotlin` or `src/test/kotlin` — grep confirms it. Nothing turns
a `VerificationService.approve(submission) == true` result into `profile.verifiedStatus = true`;
`VerificationService.approve` doesn't even take a `BreederProfile` as input, so it has no way to.
Symmetrically, `EnforcementService.markOffPlatformViolation`/`resolveAppeal` mutate `active`,
`appealStatus`, `deactivatedAt`, `appealOpenedAt` — but never touch `verifiedStatus`. There is no
function anywhere in this codebase — not in `VerificationService`, not in `EnforcementService`, not
a third combinator — that answers "is this breeder *currently* verified" by ANDing `verifiedStatus`
with `active` and `appealStatus`. `AlertsService.canTriggerEvent` (and, eventually, any "VeriPup
Verified" trust badge shown to users) takes a bare `breederVerified: Boolean` supplied entirely by
the caller, with zero enforcement that it reflects current state.

**Why this is the fraud-relevant gap, not a style nit:** the product's core promise is that a
fraudulent or banned breeder can't wear the verified badge. But because verification and
enforcement are two services that never talk to each other or to a shared "current status"
function, a breeder who is `active = false` / `appealStatus = REJECTED` after
`markOffPlatformViolation` plus a rejected appeal can still have `verifiedStatus = true` sitting on
their profile from an earlier approval — nothing in this repo stops a caller from trusting that
stale field. Concretely: a breeder passes `VerificationService.approve` once, a caller sets
`verifiedStatus = true`; months later they're caught in an off-platform violation and their appeal
is rejected (`active = false`, `appealStatus = REJECTED`) — `verifiedStatus` is never touched by
that path, so it is still `true`. Any caller that reads `profile.verifiedStatus` directly as
`breederVerified` (the obvious, intended use of a field with that exact name) keeps showing the
"VeriPup Verified" badge and keeps letting `canTriggerEvent` fire "Verified Update:" SMS blasts for
a breeder the platform itself has banned. `veripup-policy-review` checks that each service's own
formula is correct in isolation; it does not check that a "currently verified" derivation exists at
all — because none does, anywhere in this repo today.

Checklist for any change that starts wiring these services together (an orchestrator, a repository,
a "current status" helper, or any caller computing `breederVerified`):

- **Demand a single "is currently verified" derivation** before accepting any new caller that
  computes `breederVerified` its own way. It must require `verifiedStatus == true` AND
  `active == true` AND `appealStatus != AppealStatus.REJECTED` (arguably `!= PENDING` too, since a
  breeder under an open fraud investigation shouldn't keep broadcasting verified-only alerts while
  the appeal is pending) — not just one of these fields checked in isolation.
- **`EnforcementService` must gain a way to revoke trust, not just deactivate.** If
  `markOffPlatformViolation`/`resolveAppeal(approved = false)` still leaves `verifiedStatus`
  untouched after a change lands, that's the same gap re-shipped with more code around it — flag it.
  A rejected appeal (or an off-platform violation being filed) should flip `verifiedStatus` false,
  not rely on every future caller remembering to also check `active`/`appealStatus` separately.
- **`VerificationService.approve` returning `true` must be the *only* path that sets
  `verifiedStatus = true`.** Reject any new code path that flips `verifiedStatus` on a
  `BreederProfile` without going through an `approve()` call first (an admin "quick approve"
  shortcut, a migration/backfill script, a default set at signup) — that recreates exactly this
  finding's disconnect, just moved to a different call site.
- **`resolveAppeal(profile, approved = true)` re-activating a breeder (`active = true`) must not
  silently restore a verified badge that was properly revoked for a different reason.** A
  successful appeal reactivation is not the same fact as "still passes the 8
  `VerificationService.approve` checks" — don't let a future `.copy(active = true)` implicitly
  re-grant `verifiedStatus` if the two fields become linked.
- **Any UI/screen surfacing the "VeriPup Verified" badge (`screen.Screens`, or a future Compose
  screen) must call the shared "currently verified" derivation above, not read
  `verifiedStatus` raw.** A screen, DTO, or API response that reads `BreederProfile.verifiedStatus`
  directly is exactly the caller mistake this skill exists to catch.
