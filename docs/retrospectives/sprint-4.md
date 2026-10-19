# Sprint 4 Retrospective

**Period:** Week 4 (late September 2026, ~Sep 19–25)
**Focus:** Feature completion (messages, comments, event detail, profile), CI hardening

---

## What went well

- Major feature throughput: messages, comments, event details page, and profile/settings all progressed significantly.
- Javadoc generation added to the CI pipeline — documentation is now built automatically on every push to main.
- CI now runs all tests before a branch can merge — quality gate in place.

## What went badly

- UX feedback from informal testing was not being acted on systematically — refactoring was needed but not yet scheduled.
- Test coverage was insufficient; tests were being written after features, not before.
- Huji's workload was still lighter than other team members — needed better task assignment.
- The sign-in flow was incomplete: the username section was missing.
- AI recommendations were entirely absent from the sprint plan — they had been overlooked at planning time.

## Decisions made

| Decision | Reason |
|---|---|
| Restructured sign-in to a 2-step flow | Needed to capture more user details (address, interests, bio) without overwhelming a single screen |
| Added CI enforcement (tests must pass before merge to main) | Prevent broken code landing on main; maintain a stable integration branch |
| Added AI to sprint backlog immediately | Identified as a missing must-have; prioritised for Sprint 5 |

## Key commits this sprint

- `3ca95061` — CI pipeline updated to publish Javadoc to GitHub Pages (SCRUM-100)
- `ef380430` — Bug fix: CI switched to Ubuntu runner for consistent builds
- `32006473` — Keyword search across title, description and location, with unit tests (SCRUM-59, Ryan)
- `cde4e883` — Filter events by category, date and distance (Ryan)
- `54929346` — Event detail page restructure; route fixes; old unused controller removed (SCRUM-115)
