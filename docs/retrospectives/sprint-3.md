# Sprint 3 Retrospective

**Period:** Week 3 (mid-September 2026, ~Sep 11–18)
**Focus:** Landing page, authentication, DAO layer, external API integration

---

## What went well

- Each member responsible for their own tests (introduced in Sprint 2) worked better, no bottleneck at the end.
- The delete-and-rebuild DB approach was easy for everyone to follow.
- Significant visible progress: the main landing page was completed, map integration was nearly done (Kai).
- Sprint pacing and workload balance were good.

## What went badly

- Scope was too large: the plan included pulling data from ~6 websites via scraping plus the Ticketmaster API. The scraping work was too time-consuming and had to be cut mid-sprint.
- bcrypt was difficult to integrate in Java. Research time was wasted before a suitable alternative was found.

## Decisions made

| Decision | Reason |
|---|---|
| Dropped web scraping, use Ticketmaster API only | Too much implementation effort; one well-structured API gives sufficient data. Static seed data fills the rest. |
| Switched bcrypt → Argon2 (BouncyCastle) | Argon2 has first-class Java library support via BouncyCastle; simpler integration than bcrypt |

## Key commits this sprint

- `ba054219` — EventService added for distance filtering/sorting (SCRUM-53, Ryan)
- `0fbc77ee` — SignUp working end-to-end with RegisterResult for UI error handling (SCRUM-46)
- `5eed0ee4` — UserService with Argon2 password hashing; PasswordHasher decoupled from DAO (SCRUM-47)
- `272278aa` — Ticketmaster Discovery API client, Event mapping, TicketmasterID added to DB schema (SCRUM-50)
- `d2cd0ee4` — Google Places API integrated for address autocomplete and coordinates (SCRUM-57)
- `9e605de1` — Landing page: event data, hero bar, category filters, map view, event summary panel (Kai)
