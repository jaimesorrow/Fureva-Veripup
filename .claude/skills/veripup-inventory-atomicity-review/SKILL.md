---
name: veripup-inventory-atomicity-review
description: Reviews changes to Fureva VeriPup's InventoryService double-sell guard for the gap between "the formula is correct" and "the formula is safe to call concurrently" — canAcceptDeposit/canCompleteAdoption are pure check-then-act functions over a caller-supplied LitterRecord snapshot with no version/lock, so nothing in this repo stops two concurrent callers from both passing the check and over-committing the same litter. Use this instead of, or alongside, veripup-policy-review's InventoryService section for any change that adds a caller/persistence layer around InventoryService, or that adds a write path for reservedDeposits/completedAdoptions.
---

# VeriPup inventory check-then-act review

**The gap, concretely:** `InventoryService.canAcceptDeposit`/`canCompleteAdoption`
(`service/InventoryService.kt`) are pure functions over an immutable `LitterRecord` snapshot the
caller assembles and passes in — there is no persistence layer in this repo at all (see the
`data-model` skill: "there is no repository/DAO/database... every 'query' is a plain constructor
call"). The formulas themselves are correct (`veripup-policy-review` already covers that). What no
existing skill covers: the API shape gives a caller no way to make "check, then write the
incremented count" atomic. `LitterRecord` has no version/etag field and `InventoryService` has no
compare-and-swap method — it's `Boolean canAcceptDeposit(record)`, full stop. The actual write-back
of `reservedDeposits + 1` (or `completedAdoptions + 1`) necessarily happens in code this repo
doesn't contain, on the unstated assumption that whoever calls it serializes "read record → check →
write" as a single transaction.

**Why this is the fraud-relevant gap, not a generic concurrency nit:** the entire point of
`canAcceptDeposit`/`canCompleteAdoption` is the product's other core anti-fraud promise — a puppy
can't be double-sold, can't be adopted out ahead of a reserved deposit, can't exceed the
vet-confirmed litter count. But because the check is a snapshot read with no atomicity primitive,
two concurrent deposit requests for the last open slot in a litter (e.g.
`reservedDeposits == expectedLitterCount - completedAdoptions - 1`) can both read the same
`LitterRecord`, both get `canAcceptDeposit == true`, and both proceed — if the caller increments
`reservedDeposits` independently for each rather than re-reading and re-checking inside one
transaction, the litter ends up over-reserved past `expectedLitterCount`. That's exactly the
"puppy double-sold" failure this service exists to prevent, and it happens even though every line
of `InventoryService` itself is correct — a reviewer checking only `veripup-policy-review`'s
formula-correctness bullets would sign off on it. The bug lives in how a future caller (the Android
app / backend this policy library is presumably meant to sit behind) wires the check to the write,
which is exactly the kind of caller-sequencing assumption existing skills don't examine.

Checklist for any change that adds a persistence layer, repository, or write path around
`InventoryService`:

- **Look for a re-check inside the transaction, not just a check before the transaction.** The safe
  pattern is: begin transaction → re-read the current `LitterRecord` → call
  `canAcceptDeposit`/`canCompleteAdoption` again against that fresh read → write the increment →
  commit, all as one atomic unit (a DB transaction, a row lock, or equivalent). A caller that calls
  `canAcceptDeposit` once "up front" (e.g. in a UI layer or an earlier API call) and writes the
  increment later/elsewhere without re-checking has reintroduced the race — flag it even if each
  individual call site looks correct in isolation.
- **Watch for a "reserve" step split across two calls/requests** (e.g. a client calls a
  can-I-deposit endpoint, gets `true`, then separately calls a confirm-deposit endpoint some time
  later). Any gap between the check and the write is a window where a second request can slip
  through — the two calls must be collapsed into one atomic operation, or the second call must redo
  the full check against fresh data immediately before writing.
- **If a version/etag or DB-level row lock is introduced on `LitterRecord`, confirm it actually
  rejects a stale write** (e.g. `UPDATE ... WHERE reservedDeposits = :expectedOldValue` affecting
  zero rows means "someone else already took this slot, re-check") rather than just overwriting
  whatever the caller last computed.
- **`completedAdoptions < reservedDeposits` (the deposit-before-adoption ordering rule) is subject
  to the identical race** — two concurrent "complete adoption" calls against the same last reserved
  slot must not both succeed. Treat any new adoption-completion write path with the same scrutiny as
  deposit writes.
- **Don't accept "the formula is unit-tested" as evidence this is handled.** `InventoryServiceTests`
  and `CoreFlowTests` (as of this writing) only exercise `InventoryService` with single, sequential,
  single-threaded calls against hand-built records — that suite cannot and does not catch a
  concurrent double-write, so its passing is not evidence this gap is closed.
