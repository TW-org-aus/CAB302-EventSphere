# Sprint 6 Retrospective

**Period:** Week 6 (mid-October 2026, ~Oct 3–19)
**Focus:** AI recommendations, notifications, UI polish, test coverage, Javadoc, UX results

---

## What went well

- AI recommendation pipeline fully implemented and wired to the landing page (Kai): interest profile → candidate selection → Gemini ranking → personalised event row.
- Notifications feature implemented end-to-end with full test coverage.
- Controller-level tests added using TestFX, covering login, sign-up, navbar, profile, and settings.
- More seeding data added by Alexander, app now has enough content for a convincing demo.
- Dark mode toggle completed (Alexander, SCRUM-75).

## What went badly

- Javadoc coverage was still very low (only ~11% of production classes had any comments)
- UX test results had been collected but not documented or acted on systematically yet —-> need to collate and create `docs/ux-findings.md`.
- Some existing tests were too broad (multiple assertions per test) and included unnecessary getter/setter tests.
- Raw HTML in event descriptions from Ticketmaster was being shown to users without cleaning.

## Decisions made

| Decision                                                                              | Reason |
|---------------------------------------------------------------------------------------|---|
| Add Javadoc to all production classes                                                 | Submission requires generated Javadoc pages with meaningful coverage |
| Document UX findings in `docs/ux-findings.md`                                         | Marker needs to see evidence of UX testing and what changed as a result |
| Add `xvfb-run` to CI workflow                                                         | TestFX controller tests were failing on the Linux CI runner (headless environment); `xvfb-run` provides a virtual display |
| Use Gemini API via `IAiClient` interface for description cleaning and recommendations | Keeps AI provider swappable; `IAiClient` mock enables tests without live API calls |

## Key commits this sprint

- `9cbbd852` — AI event description cleaner integrated (Kai)
- `b99bfa90` — More verbose AI prompt for fixed-length descriptions; screen sizing fix (SCRUM-AI)
- `cd86213b` — Interest profile built from preferences, going, and likes (Kai, SCRUM-103)
- `ba946c7a` — AI recommendations row on landing page; landing page restructure (Kai, SCRUM-104/105)
- `d951514f` — Notifications working end-to-end (SCRUM-83)
- `e4fb4c1d` — Notification tests written (SCRUM-86)
- `87207dbc` — More seeding data (Alexander)
- `717e6aa4` — Dark mode toggle (Alexander, SCRUM-75)
