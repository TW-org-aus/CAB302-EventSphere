# EventSphere — Design Notes

Architectural decisions made during development, with the reasoning behind each.

---

## Database: MySQL → SQLite (Sprint 2)

The initial plan used a hosted MySQL instance. However, 2 issues immediately arose, cost, and the network dependency making local development fragile. We switched to SQLite with a local file. Schema changes are handled by deleting and rebuilding the `.db` file on startup rather than running migrations. Overall, migrations added complexity for no benefit at this scale.

**Evidence:** `DBController.java` schema creation runs at app launch via `DBController.initialiseDatabase()`.

---

## Database migrations dropped (Sprint 2)

We began Sprint 2 with a migration-based schema change strategy. It caused integration issues in the first week. For a small team building toward a fixed submission date, delete-and-rebuild is faster and less error-prone than maintaining a migration chain. We dropped migrations entirely.

---

## Password hashing: bcrypt → Argon2 (Sprint 3)

bcrypt had no clean Java library at the time of implementation. We switched to Argon2id via BouncyCastle, which has first-class Java support. `PasswordHasher.java` owns all hashing logic; `UserService` calls it rather than anything in the DAO layer, keeping persistence and security concerns separate.

**Evidence:** `src/main/java/com/eventsphere/app/service/PasswordHasher.java`, commit `5eed0ee4`.

---

## Event data: web scraping → Ticketmaster API + seeding (Sprint 3)

The Sprint 3 plan included scraping ~6 event websites plus calling the Ticketmaster API. Scraping took too long to implement reliably. We cut everything except Ticketmaster and seeded the remaining data statically. The Ticketmaster integration lives in `TicketmasterClient` behind `ITicketmasterClient`, so the source can be swapped or mocked in tests without touching the ingestor logic.

**Evidence:** `src/main/java/com/eventsphere/app/ingestion/`, `src/test/java/com/eventsphere/app/ingestion/MockTicketmasterClient.java`.

---

## DAO interfaces for dependency injection (Sprint 3 onwards)

Every DAO has a matching interface (`IUserDAO`, `IEventDAO`, etc. — 10 total). Services accept these interfaces via constructor, never instantiating concrete DAO classes themselves. This made the test suite possible: `MockEventDAO`, `MockUserDAO`, and similar hand-rolled test doubles implement the interface and run in-memory without a database.

The week 9 feedback identified `TicketmasterIngestor` as a violation of this pattern — it created `new EventDAO` and `new SourceDAO` directly. That is tracked in SCRUM-151 for correction.

**Evidence:** `src/main/java/com/eventsphere/app/dao/I*.java`, `src/test/java/com/eventsphere/app/service/Mock*.java`.

---

## Router owns all controller construction (Sprint 3)

JavaFX's default `FXMLLoader` instantiates controllers via reflection with no-arg constructors. Our controllers need injected services. We solved this by routing all navigation through `Router.java`, which calls `setControllerFactory(Router::createController)` to construct every controller with its dependencies. No controller creates its own DAO or service.

**Evidence:** `src/main/java/com/eventsphere/app/Router.java` — `createController` method and all `navigateTo*` methods.

---

## Test ownership: centralised → per-developer (Sprint 2)

Sprint 2 assigned all tests to one team member. That person ran out of time because everyone else was still writing features at the deadline, leaving no time for tests. From Sprint 3 onward, each developer writes their own tests for the code they ship. Test coverage increased every sprint as a result.

---

## TDD adopted mid-project (Sprint 5)

Tests were written after features through Sprints 2–4. Sprint 5 retrospective flagged this as a gap — tests written after the fact tend to confirm the implementation rather than specify it. We switched to writing tests first for all new service and DAO work from Sprint 5 onward. The notification feature (Sprint 6) followed the full red-green-refactor cycle.

**Evidence:** commit `e4fb4c1d` (notification tests written before feature), commit `d951514f` (notification feature implemented after).

---

## AI provider abstraction: IAiClient interface (Sprint 5)

`AiClient` calls the Gemini API. Wrapping it behind `IAiClient` means `DescriptionCleaner` and `EventRecommender` never depend on the concrete HTTP client. `MockAiClient` in the test suite returns fixed responses, so AI logic can be tested without live API calls or network access.

**Evidence:** `src/main/java/com/eventsphere/app/ai/IAiClient.java`, `src/test/java/com/eventsphere/app/ai/MockAiClient.java`.

---

## CI: Windows → Ubuntu runner, then xvfb-run added (Sprints 4 and 6)

The initial CI workflow ran on a Windows runner. We switched to Ubuntu in Sprint 4 for consistency with standard CI environments (commit `ef380430`). In Sprint 6, TestFX controller tests began failing on the headless Ubuntu runner because JavaFX needs a display. We added `xvfb-run --auto-servernum` to the Maven verify step to provide a virtual framebuffer.

**Evidence:** `.github/workflows/ci.yml`.
