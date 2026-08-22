# Event Hub — First Draft User Stories

This is a simple starting list for our first checkpoint. It focuses on the main things we need first: logging in, saving stuff, and finding events.

We can tweak this as a team later and add more detail when we start planning properly.

Template we are using:
```
As a ...
I want ...
so that ...

Acceptance Criteria
Given ...
When ...
Then ...
```

---

## Authentication

### US-01 — Create an account
**As a** new user
**I want** to sign up with my email and password
**so that** I can save my favourite events and keep my details

**Acceptance Criteria**
- Given I am on the sign-up page
- When I enter a valid email and password
- Then my account is created and I am logged in

**MoSCoW:** Must-have

---

### US-02 — Log in
**As a** returning user
**I want** to log in with my email and password
**so that** I can access my saved events and settings

**Acceptance Criteria**
- Given I already have an account
- When I enter the right login details
- Then I am signed in and taken to my home feed

**MoSCoW:** Must-have

---

### US-03 — Log out
**As a** logged-in user
**I want** to log out of my account
**so that** my session is closed on a shared or public device

**Acceptance Criteria**
- Given I am logged in
- When I click "Log out"
- Then I am signed out and sent back to the home page

**MoSCoW:** Must-have

---

### US-04 — Reset my password
**As a** user who forgot my password
**I want** to reset it by email
**so that** I can get back into my account

**Acceptance Criteria**
- Given I ask to reset my password using my registered email
- When I follow the reset link and choose a new password
- Then I can log in again with the new password

**MoSCoW:** Should-have

---

## Persistency

### US-05 — Save favourite events
**As a** logged-in user
**I want** to save or favourite events I like
**so that** I can find them again later without searching for them again

**Acceptance Criteria**
- Given I am looking at an event
- When I click "Save" or "Favourite"
- Then the event is saved to my account and stays there next time I log in

**MoSCoW:** Must-have

---

### US-06 — Remember my preferences
**As a** logged-in user
**I want** my preferred location and event categories to be saved
**so that** I do not have to choose them every time I open the app

**Acceptance Criteria**
- Given I set my city and category preferences
- When I log out and come back later
- Then my saved preferences are still there

**MoSCoW:** Should-have

---

## Discovering and browsing events

### US-07 — Browse all events
**As a** visitor
**I want** to see a list of upcoming events
**so that** I can see what is happening without searching first

**Acceptance Criteria**
- Given I open the app
- When the home page loads
- Then I can see a list of upcoming events with the name, date, and location

**MoSCoW:** Must-have

---

### US-08 — Search for an event
**As a** user
**I want** to search for events by keyword
**so that** I can quickly find something I already know about

**Acceptance Criteria**
- Given I type a search term
- When I submit the search
- Then I see matching events or a "no results" message

**MoSCoW:** Must-have

---

### US-09 — Filter events
**As a** user
**I want** to filter events by category, date, and location
**so that** I can narrow things down to what matters to me

**Acceptance Criteria**
- Given I am on the event list
- When I choose one or more filters
- Then only events matching those filters are shown

**MoSCoW:** Should-have

---

### US-10 — View event details
**As a** user
**I want** to open an event and see the full details
**so that** I can decide if I want to go

**Acceptance Criteria**
- Given I click on an event
- When the event page opens
- Then I can see the details such as description, date, time, venue, price, and image

**MoSCoW:** Must-have

---

### US-11 — Go to the official booking page
**As a** user
**I want** to be sent to the event's official ticketing page
**so that** I can buy tickets

**Acceptance Criteria**
- Given I am on an event page
- When I click "Get Tickets" or "Book Now"
- Then I am taken to the external booking website in a new tab

**MoSCoW:** Must-have

---

### US-12 — Sort events
**As a** user
**I want** to sort the event list by date, popularity, or distance
**so that** I can browse in the order that is most useful to me

**Acceptance Criteria**
- Given I am on the event list
- When I choose a sort option
- Then the list updates in that order

**MoSCoW:** Could-have

---

## Extras / future ideas

### US-13 — Reminder for saved events
**As a** logged-in user
**I want** to get a reminder before a saved event
**so that** I do not forget about it

**Acceptance Criteria**
- Given I have saved an event
- When the event is getting close
- Then I receive an in-app or email reminder

**MoSCoW:** Could-have

---

### US-14 — Submit an event
**As an** event organiser
**I want** to add my event to the site
**so that** it can appear in Event Hub's listings

**Acceptance Criteria**
- Given I have organiser access
- When I fill in and send the event form
- Then the event is added to the list, or waits for approval if moderation is needed

**MoSCoW:** Won't-have this sprint — come back later

---

## Quick notes for the team
- The main things we need first are Authentication (US-01 to US-04) and Persistency (US-05 and US-06).
- We can do a rough estimate on effort later as a team during checkpoint prep.
- MoSCoW labels are only a starting idea, not final.
- If a story feels too big, we can split it up into smaller pieces before building it.

