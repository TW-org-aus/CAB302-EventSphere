# CAB302 EventSphere — Claude Code project rules

## Test-driven development (MANDATORY)

This project follows strict TDD. Every feature must follow RED → GREEN → REFACTOR.

1. **Write tests first.** No production code until the tests exist and fail.
2. **Stub new service methods** with `throw new UnsupportedOperationException("not yet implemented")` until you write the tests. Do not implement anything in the same turn as writing tests.
3. **Tests must be RED before implementation begins.** Run the suite and confirm failures before writing any implementation.
4. **Do not write production code that is not covered by a test you wrote first.**

This applies to every new method, class, and feature — no exceptions.

## Test style

- No Mockito. Hand-roll mocks that implement the DAO interface directly.
- Use real in-memory SQLite (`jdbc:sqlite::memory:`) for DAO and service tests.
- Use `DBController` in `@BeforeEach` to set up schema.
- New DAO tests go in `src/test/java/com/eventsphere/app/dao/`.
- New service tests go in `src/test/java/com/eventsphere/app/service/`.

## Comments

Minimise comments. Javadoc only where the WHY is non-obvious. No inline narration.
