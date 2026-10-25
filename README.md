# EventSphere

A JavaFX desktop app for finding events around Brisbane. CAB302 Agile Software Engineering assignment, Semester 2 2026, QUT.

## What it does

- Browses a featured carousel, category filters, and an event row on the landing page.
- Opens a map drawer with pins pulled from Ticketmaster, and shows event details beside each pin.
- Handles sign-up, log-in, profile, notifications, direct messages, and event comments.
- Recommends events from an interest profile the user picks during sign-up.

## Stack

Java 21, JavaFX, Maven, SQLite. Run it with `./mvnw javafx:run` on the first launch; `DatabaseSeeder` fills the DB and the Ticketmaster ingestor fetches live events on start.

## Team

| Name | Role |
|------|------|
| Tom Wehl | Backend + data |
| Ryan Norton | Backend + DAO |
| Kai Ren Scott Khoo | Frontend |
| Alexander Pravidur | Messaging, notifications + seeding |
| Chan Pak Ming Huji | AI recommender, testing + seeding |

## Docs

- `docs/UI_Usability_Accessibility_Test.md` — the usability + accessibility test script.
- `docs/User_Tests/` — filled participant reports.
- `docs/User_Stories.md` — user stories backing the sprint plan.
- `docs/adr/` — architecture decisions.
- `docs/retrospectives/` — sprint retros.

Jira project: `SCRUM` at tomwehl0.atlassian.net.
