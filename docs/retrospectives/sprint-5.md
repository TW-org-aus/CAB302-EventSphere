# Sprint 5 Retrospective

**Period:** Week 5 (early October 2026, ~Sep 26 – Oct 2)
**Focus:** OOP refactoring, UX improvements, AI groundwork, increasing test coverage

---

## What went well

- OOP and UX refactoring progressed well — service classes were restructured and UI flows cleaned up.
- Co-owned features (one person implements part 1, another finishes part 2 in a later sprint) worked without major conflicts, useful as other team members had overlapping assignment deadlines.
- The project was approaching completion for must-have features.

## What went badly

- TDD had not been adopted: tests were still written after features, not before. This was identified as a critical gap.
- User testing had not started — no UX feedback had been collected from actual users.
- AI recommendations were still not implemented despite being added to the backlog in Sprint 4.
- Several UI rough edges (leftover from informal testing) were not yet fixed.

## Decisions made

| Decision | Reason |
|---|---|
| Adopt TDD immediately for all new work | Week 9 submission feedback risk; better design discipline; tests become the specification |
| Begin user testing this sprint | UX validation was a deliverable requirement that had been deferred too long |
| Implement AI recommendations now | No longer deferrable — must be in the final prototype |

## Key commits this sprint

- `5b026a3a` — Comments: edit, delete, session guard for other users' comments (SCRUM-69)
- `65248172` — Sign-up refactored to 2-step flow; username re-added; bio field added (SCRUM-118)
- `fe3baf48` — Who's Going screen and GoingService unit tests (SCRUM-116, Ryan)
- `0ddf6197` — Event likes and comments wired end-to-end (Huji)
- `6ecc4d4e` — Profile: edit preferences, bio, name, password, email (Alexander, SCRUM-74/72)
- `37302fad` — AI event description cleaner with provider-agnostic IAiClient interface (Kai)
- `32c3c888` — CommentService tests (first TDD-style tests in the project)
