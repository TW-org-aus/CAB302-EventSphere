# Sprint 2 Retrospective

**Period:** Week 2 (early September 2026)
**Focus:** Database setup, schema, and core project architecture

---

## What went well

- Task split was even and clear — each team member had a defined piece of work.
- Jira setup with sprints and epics worked well for tracking progress.
- The project's layered architecture (models, DAOs, services) was established early and gave everyone a shared structure to build on.

## What went badly

- Tests assigned to Huji were left until the end of the sprint. Because everyone was still implementing right up to the deadline, there was no time to write them. Tests were skipped entirely for that sprint.
- Database migrations caused integration issues — the schema change process was fragile and slowed down the whole team.
- The initial plan to use a hosted MySQL database added unnecessary cost and complexity for a project of this scale.

## Decisions made

| Decision | Reason |
|---|---|
| Dropped database migrations | Too fragile for a small project; delete-and-rebuild the `.db` file on schema changes is simpler and faster |
| Switched MySQL → SQLite | Local file-based DB removes hosting cost and the network dependency; fits the scope of the project |
| Each developer now owns their own tests | Centralising tests on one person created a bottleneck and meant they were always the last thing done |

## Key commits this sprint

- `bb1fd9ee` — DB schema, SQLite, and DBController added (SCRUM-40)
- `9bcb7d18` — Bug fixes: bootstrap icons added to all FXML files (SCRUM-95)
- `5a5f4b9e` — User onboarding section in signup (address, interests, first/last name) (SCRUM-97)
