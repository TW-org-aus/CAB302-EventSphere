# EventSphere UI Test: Usability and Accessibility Heuristics

Run this once the prototype is feature-complete. It has three parts:

1. **Task-based user test** (Part B): real participants complete tasks while thinking aloud.
2. **Accessibility audit** (Part C): you run it yourself with keyboard, screen reader and contrast checks. No participant needed.
3. **Heuristic walkthrough** (Part D): one or more team members score every screen against Nielsen's 10 heuristics.

Each task and check carries the code of the heuristic it tests. Part F rolls the results up by those codes.

---

## Part A: Setup

### A.1 Screens in scope

| # | Screen | FXML | How you reach it |
|---|--------|------|------------------|
| S1 | Nav bar (shared) | `nav-bar.fxml` | Top of every screen except the event page body |
| S2 | Landing page: hero carousel, category buttons, event row | `landing-page.fxml` | App launch, or click EVENTSPHERE logo |
| S3 | Map drawer and event details panel | `landing-page.fxml` | Grey tab on the right edge of the landing page |
| S4 | Event page and comments | `EventPage.fxml` | MORE INFO (hero or details panel) |
| S5 | Sign up | `SignUp.fxml` | Log In screen > Sign Up link |
| S6 | Log in | `login-view.fxml` | Nav bar "Log In", Sign up > Login link, Log Out |
| S7 | Notifications | `notifications-view.fxml` | Nav bar bell icon |
| S8 | Messages list | `messages-list-view.fxml` | Nav bar chat icon |
| S9 | Conversation | `messages-view.fxml` | Click a row in the messages list |
| S10 | Profile | `profile-view.fxml` | Nav bar blue person icon |
| S11 | Settings | `settings-view.fxml` | Profile > Settings |

No screen links to `Event-Details-View.fxml` or `event-comments-view.fxml` yet. If the team connects either one before testing, add it to this table and run Parts C and D on it.

### A.2 Heuristic codes

**Usability (Nielsen's 10)**

| Code | Heuristic |
|------|-----------|
| H1 | Visibility of system status |
| H2 | Match between system and the real world |
| H3 | User control and freedom |
| H4 | Consistency and standards |
| H5 | Error prevention |
| H6 | Recognition rather than recall |
| H7 | Flexibility and efficiency of use |
| H8 | Aesthetic and minimalist design |
| H9 | Help users recognise, diagnose and recover from errors |
| H10 | Help and documentation |

**Accessibility (WCAG 2.1 AA, applied to a desktop JavaFX app)**

| Code | Criterion | WCAG ref |
|------|-----------|----------|
| A1 | Keyboard operable, no keyboard trap | 2.1.1, 2.1.2 |
| A2 | Logical focus order | 2.4.3 |
| A3 | Visible focus indicator | 2.4.7 |
| A4 | Text contrast ≥ 4.5:1 (≥ 3:1 for text ≥ 18.66px bold or ≥ 24px) | 1.4.3 |
| A5 | Non-text contrast ≥ 3:1 (borders, icons, focus rings) | 1.4.11 |
| A6 | Colour is not the only way to show state | 1.4.1 |
| A7 | Controls have an accessible name and role | 4.1.2 |
| A8 | Form inputs have programmatic labels | 1.3.1, 3.3.2 |
| A9 | Errors identified in text and announced | 3.3.1, 4.1.3 |
| A10 | Images and icons have text alternatives | 1.1.1 |
| A11 | Usable at 200% text / OS scaling, no lost content | 1.4.4, 1.4.10 |
| A12 | Target size ≥ 24×24px | 2.5.8 |

### A.3 Participants

- **5 participants.** Five users find most usability problems in a round. If you have time after fixing, run a second round of 5.
- Mix: at least 1 who seldom uses event apps, at least 1 who prefers keyboard over mouse, and, if you can recruit one, 1 who uses a screen reader or screen magnifier day to day.
- Exclude team members, since they know where everything is.

### A.4 Roles

| Role | Job |
|------|-----|
| Facilitator | Reads the script and tasks, keeps the participant talking, never helps with the task |
| Note-taker | Fills in the observation sheet (Part E) live, records times |

### A.5 Environment and data

- [ ] Windows laptop, app launched from IntelliJ or `mvnw javafx:run`, window at default 1280×800.
- [ ] Fresh database, seeded (`DatabaseSeeder` runs on launch). Confirm the hero carousel and event row show events.
- [ ] Ticketmaster ingest finished, so map pins appear around Brisbane.
- [ ] A pre-made test account for login tasks: `tester@eventsphere.test` / a password you choose. Write it on a card to hand the participant.
- [ ] The email you will use for the sign-up task is **not** already registered.
- [ ] Screen recording on (OBS or Xbox Game Bar), with microphone, if the participant consents.
- [ ] Stopwatch or the recording timestamps.
- [ ] For Part C: Windows Narrator (Win + Ctrl + Enter), Windows Magnifier (Win + Plus), and a contrast checker (WebAIM or Colour Contrast Analyser).

---

## Part B: Task-based user test

### B.1 Facilitator script

Read this aloud:

> Thanks for helping. We're testing the app, so you can't get anything wrong. If something confuses you, tell us. That's the stuff we need to hear.
>
> Please think aloud as you go: say what you're looking at, what you expect to happen, and anything that surprises you.
>
> I'll read each task. I can't help you complete it, but you can tell me when you think you're done or when you'd give up. Is it OK if we record the screen and audio? The recording stays inside the team.

Then ask the warm-up questions:

1. How do you find out about events and things to do now?
2. Which apps or sites do you use for it?

### B.2 Recording each task

For every task, the note-taker records:

- **Outcome:** Success / Success with difficulty / Fail / Gave up
- **Time** from the end of the task prompt to the participant saying "done"
- **Errors:** wrong clicks, dead ends, backtracks
- **Quotes** that show confusion or delight
- **Ease rating:** ask after each task, "On a scale of 1 (very hard) to 7 (very easy), how easy was that?" (Single Ease Question)

### B.3 Tasks

Start every participant on the landing page, logged out.

---

#### T1. Browse featured events

> "You've opened EventSphere for the first time. Look through the featured events at the top and tell me which one sounds most interesting."

| | |
|---|---|
| Screens | S2 |
| Success | Participant uses the carousel arrows to view all 3 featured events |
| Tests | H1, H2, H6, A10 |

Observe:
- Do they notice the left/right chevrons at the bottom of the hero image?
- Can they tell how many featured events there are and which one they're on? (No position indicator exists, so watch for this.)
- Can they read the event name and blurb on top of the image?

---

#### T2. Filter by category

> "Find music events. Then find what's on this weekend."

| | |
|---|---|
| Screens | S2 |
| Success | Clicks MUSIC, then THIS WEEKEND; row of cards updates each time |
| Tests | H1, H4, H6, A6 |

Observe:
- After clicking, do they notice the event row change further down the page? Do they scroll?
- Can they tell which category is active? (The section heading changes; the button does not.)
- If a category is empty, is "Nothing on in this category yet." clear enough?
- Do they expect EXPLORE to mean "show everything"?

---

#### T3. Find an event near you on the map

> "You want to see what's on near the CBD. Use the map to find an event and read its details. Then close the details and go back to the list."

| | |
|---|---|
| Screens | S3 |
| Success | Opens drawer, clicks a pin, reads details panel, closes details with ✕, closes drawer |
| Tests | H1, H3, H6, A5, A12 |

Observe:
- How long until they find the grey tab on the right edge? Do they find it at all without a hint? Record time to discovery.
- Can they distinguish pins from the map at the default zoom?
- Do they understand the panel belongs to the pin they clicked?
- Do they find the ✕ close button and the drawer tab to close the map?

---

#### T4. Search for an event

> "Search for a comedy event."

| | |
|---|---|
| Screens | S1 |
| Success | Types in the search bar, presses Enter, sees results or a "no results" message |
| Tests | H1, H7, H9 |

Observe:
- Do they press Enter or look for a search button? (There is no button, only the Enter key.)
- Is the result or empty state clear?
- Run this task once the team has built search. In the current build `onSearch` prints to the console and does nothing else. If it's still a stub, skip it and mark N/A.

---

#### T5. Create an account (with a deliberate error)

> "Create an account. Use this email: [test email]. When it asks for a password, first try the password `abc`."

Then after the error: "Now fix it and finish creating the account. Pick some interests you'd want to see."

| | |
|---|---|
| Screens | S6 → S5 → S6 |
| Success | Finds sign-up via Log In, reads the password error, corrects it, selects interests, lands on Log In with the "Account created" message and email pre-filled |
| Tests | H1, H5, H9, A1, A8, A9 |

Observe:
- How do they find sign-up? (Path is nav bar "Log In" > "Sign Up" link.)
- Do they expect password rules before they type, or only after an error?
- Where do they look for the error message? It appears at the bottom above the Sign UP button, which may be off-screen from the password field. Do they see it?
- Do they try to pick a 6th interest? What do they think when it won't select? (Only the "5/5 selected" hint tells them.)
- Do they try the back arrow at the top left of the form? It's a plain icon with no click handler.
- Do they notice "Account created. Log in to get started." on the next screen?
- Do they try the Facebook / Instagram / Google buttons? Note what they expect them to do.

---

#### T6. Log in (with a deliberate error)

> "Log in with the account on this card. Type the password wrong the first time."

| | |
|---|---|
| Screens | S6 |
| Success | Sees error, re-enters password, reaches landing page |
| Tests | H1, H9, H7, A9 |

Observe:
- Is the error message specific enough to act on?
- Do they notice the app emptied the password field?
- Do they press Enter to submit? (Enter submits from the password field. The email field ignores it.)
- After logging in, can they tell they're logged in? The nav bar still shows "Log In".

---

#### T7. Read an event and join the conversation

> "Open the Brisbane Car Meet event. Find out where it is, then post a comment asking if there's parking. After that, show only the newest comments."

| | |
|---|---|
| Screens | S2 → S4 |
| Success | Reaches event page, reads location, posts comment, comment appears, switches to Latest |
| Tests | H1, H3, H4, H8, A1, A7 |

Observe:
- How do they get to the event page (hero MORE INFO or map panel)?
- Do they understand the difference between "Filter comments" on the left and the Top/Latest/Host Replies buttons on the right? They show the same three options twice.
- Does "Sort: Relevant ⌄" look clickable? It's a label.
- After posting, does the comment appear and does the input clear?
- Do they try to click 👍, "Reply" or "View 3 replies"? These are plain text.
- How do they leave the page? There is no Back button; watch whether they find the logo.

---

#### T8. Check notifications

> "Check whether anyone has replied to you."

| | |
|---|---|
| Screens | S1 → S7 |
| Success | Clicks bell icon, identifies Jordan Lee's reply |
| Tests | H1, H6, A7 |

Observe:
- Do they recognise the bell icon without a label?
- Do they expect to click a notification to jump to the comment or message?
- Can they tell read from unread?

---

#### T9. Reply to a message

> "Jordan Lee sent you a message. Reply to say you'll be there at 8."

| | |
|---|---|
| Screens | S1 → S8 → S9 |
| Success | Opens messages, opens Jordan's conversation, sends reply, reply appears |
| Tests | H1, H4, H7 |

Observe:
- Do they recognise the chat icon?
- Do they try to press Enter to send? (In the current build, Enter does nothing here. Only the Send button sends.)
- Can they tell their messages from Jordan's? (Alignment and the "You ·" label are the only cues.)
- How do they get back to the conversation list?

---

#### T10. Review your events

> "Go to your profile. Find the events you're going to, then the events you've already been to. Open the trivia night."

| | |
|---|---|
| Screens | S1 → S10 |
| Success | Opens profile, switches Going/Been tabs, opens an event |
| Tests | H1, H4, A6 |

Observe:
- Do they recognise the blue person icon as "profile"?
- Can they tell which tab is active? (Colour only.)
- Is it clear the event rows are clickable?

---

#### T11. Change settings and log out

> "Turn off notifications. Then turn on night mode. Then log out."

| | |
|---|---|
| Screens | S10 → S11 → S6 |
| Success | Toggles both settings, understands their state, logs out |
| Tests | H1, H2, H4, H5, A6 |

Observe:
- Do they find Settings via Profile? Where did they look first?
- Does the toggle text (On/Off) make the state clear? Note: the Night Mode toggle label stays "Off" after clicking in the current build.
- Do they recognise "Log Out" as a button? It is styled like the setting rows above it.
- Do they expect a confirmation before logging out?

---

#### T12. Delete your account (observe only)

> "You've decided to stop using EventSphere. Show me how you'd delete your account, but stop before you actually confirm it."

| | |
|---|---|
| Screens | S11 |
| Success | Finds Delete Account; app asks for confirmation before deleting |
| Tests | H3, H5, H9 |

Observe:
- Is there a confirmation step? A destructive action with no confirmation is a severity 4 finding.
- Does the app explain the consequence (data lost, can't undo)?
- Run on a throwaway account in case the confirmation is missing.

---

### B.4 Post-test questionnaire

**System Usability Scale (SUS).** Participant rates each from 1 (strongly disagree) to 5 (strongly agree).

1. I think that I would like to use this system frequently.
2. I found the system unnecessarily complex.
3. I thought the system was easy to use.
4. I think that I would need the support of a technical person to be able to use this system.
5. I found the various functions in this system were well integrated.
6. I thought there was too much inconsistency in this system.
7. I would imagine that most people would learn to use this system very quickly.
8. I found the system very cumbersome to use.
9. I felt very confident using the system.
10. I needed to learn a lot of things before I could get going with this system.

**Scoring:** for odd items subtract 1 from the score; for even items subtract the score from 5. Sum the ten results and multiply by 2.5. Result is 0 to 100. 68 is average; above 80 is good.

**Open questions**

1. What was the most frustrating moment?
2. Was there anything you expected to click that didn't do anything?
3. Was any text hard to read?
4. Did you ever feel lost or unsure where you were in the app?
5. What one thing would you change first?

---

## Part C: Accessibility audit

You run this yourself. Do every check on every screen in A.1 and record results in the matrix in C.8.

### C.1 Keyboard only (A1, A2, A3)

Unplug or ignore the mouse. Starting on the landing page:

1. Press Tab over and over. Write down the order focus moves in.
2. At each stop, confirm you can see where focus is.
3. Activate each control with Enter or Space.
4. Try to complete T2, T3, T5, T6, T7 and T11 without the mouse.
5. Confirm you can always Tab out of a region (no trap), including the map and the scroll areas.

Pass: every interactive element is reachable, in a sensible order, with a visible focus ring, and works from the keyboard.

Check these controls one by one. In the current build each uses `onMouseClicked` on a node that can't take focus, so a keyboard user probably can't reach them:

| Control | Screen |
|---------|--------|
| Bell, chat and profile icons | S1 |
| Event cards in the event row | S2 |
| Map pins | S3 |
| Conversation rows | S8 |
| Event rows on the Going/Been list | S10 |
| 👍, Reply, "View N replies" | S4 |

### C.2 Screen reader (A7, A8, A9, A10)

Turn on Narrator (Win + Ctrl + Enter). JavaFX exposes controls to Narrator through UI Automation.

For each screen:

1. Tab through and note what Narrator announces for each control: name, role, state.
2. On forms (S5, S6), confirm each field announces its label ("First name, edit"), not only "edit".
3. Trigger each error in T5 and T6. Does Narrator announce the error without you moving focus to it?
4. Toggle interest bubbles and settings toggles. Is selected/unselected state announced?

Controls likely to be announced with no name, because they show only an icon:

| Control | Screen |
|---------|--------|
| Hero previous / next chevrons | S2 |
| Map drawer tab | S3 |
| Details panel ✕ close | S3 |
| Facebook, Instagram, Google buttons | S5 |
| Bell, chat, profile icons | S1 |

Fix pattern if they fail: set `accessibleText` on the node, and set `labelFor` on each form `Label`.

Also listen for the emoji labels (📅 🕒 📍 👤 ✔ 👍 💬 📌 🖼 ☺). Narrator may read them as "calendar", "one o'clock" and so on, or skip them. Note anything that sounds confusing.

### C.3 Colour contrast (A4, A5)

Measured from the colours in the FXML and `app.css`. Re-measure on the finished build with a colour picker, because hex values may change.

| Element | Foreground / background | Ratio | Size | Requirement | Result |
|---------|-------------------------|-------|------|-------------|--------|
| "Community Comments" heading on orange | #FFFFFF / #F0512B | 3.55:1 | 20px bold heading | 3:1 (large) | Pass |
| Any normal-size white text on the orange background | #FFFFFF / #F0512B | 3.55:1 | 13px | 4.5:1 | **Fail** |
| "Sort: Relevant ⌄" on orange | #FFFFFF / #F0512B | 3.55:1 | 13px | 4.5:1 | **Fail** |
| "or continue with" (sign up) | #999999 / #FFFFFF | 2.85:1 | 11px | 4.5:1 | **Fail** |
| Comment timestamps "5h ago" | #9A9A9A / #FFFFFF | 2.81:1 | 11px | 4.5:1 | **Fail** |
| "📌 Pinned" | #8A8A8A / #E9F0FB | 3.01:1 | 11px | 4.5:1 | **Fail** |
| Tag pills, selected interest bubbles | #2F80ED / #E3F0FF | 3.35:1 | 12px | 4.5:1 | **Fail** |
| Primary button, active tab pill (app.css accent) | #FFFFFF / #2F80ED | 3.87:1 | 13px | 4.5:1 | **Fail** |
| Buttons using inline blue | #FFFFFF / #2E6BE8 | 4.79:1 | 13–14px | 4.5:1 | Pass |
| Delete Account | #FFFFFF / #D64545 | 4.38:1 | 13px bold | 4.5:1 | **Fail** (narrow) |
| Comment count badge | #FFFFFF / #D8431F | 4.43:1 | 12px | 4.5:1 | **Fail** (narrow) |
| Success message "Account created" | #1E8E3E / #FFFFFF | 4.21:1 | 13px bold | 4.5:1 | **Fail** (narrow) |
| Muted text, stat labels | #6B7280 / #FFFFFF | 4.83:1 | 11–13px | 4.5:1 | Pass |
| Sign-up hints on field background | #6B7280 / #F7F7F7 | 4.51:1 | 11px | 4.5:1 | Pass (0.01 margin) |
| Login error | #C0392B / #FFFFFF | 5.44:1 | 12px | 4.5:1 | Pass |
| **Non-text:** sign-up text field border vs fill | #E4E4E4 / #F7F7F7 | 1.19:1 | | 3:1 | **Fail** |
| **Non-text:** secondary button border vs white | #E5E7EB / #FFFFFF | 1.24:1 | | 3:1 | **Fail** |
| **Non-text:** map drawer tab vs map | #DDDDDD / #E8E8E8 | 1.11:1 | | 3:1 | **Fail** |
| **Non-text:** map pin vs white | #F4502F / #FFFFFF | 3.48:1 | | 3:1 | Pass |

Check the white text on the hero image too. A light photo can wash it out even with the gradient overlay, so measure all 3 featured images at the lightest point under the text.

### C.4 Colour as the only cue (A6)

For each, cover the colour (squint, or use a greyscale filter: Settings > Accessibility > Colour filters > Greyscale) and ask whether you can still tell the state:

- [ ] Active Going/Been tab pill (S10)
- [ ] Active Top/Latest/Host Replies comment filter (S4)
- [ ] Selected interest bubble in sign-up (S5). Has bold text as a second cue; confirm it is enough.
- [ ] Toggle buttons in Settings (S11). Text "On"/"Off" is the second cue, if it updates.
- [ ] Active category button on the landing page (S2)
- [ ] Host badge and pinned comment (S4)

### C.5 Scaling and resizing (A11)

1. Set Windows display scale to 150%, then 200% (Settings > System > Display > Scale). Relaunch the app.
2. Visit every screen. Record any clipped text, overlapping elements or controls pushed off-screen.
3. Reset to 100%. Resize the window to 1024×768, then maximise. Repeat the screen visit.
4. Resize or maximise the window, then navigate to another screen. Does the window keep its size, or snap back to 1280×800? (`Router` creates each new scene at 1280×800, so check this.)
5. Use Windows Magnifier at 200% on the sign-up form. Can you follow the form and find the error message?

Watch in particular: the category button row (7 fixed-width 150px buttons), the nav bar search field, the sign-up card (fixed 460×640), and the event page's two-column layout.

### C.6 Target size (A12)

Measure with a screenshot and ruler tool. Minimum 24×24px, recommended 44×44px.

| Target | Size in build | Result |
|--------|---------------|--------|
| Nav bar icons | 38×38 | Pass |
| Social sign-up buttons | 40×40 | Pass |
| Map drawer tab | 28 wide × 80 high | Pass minimum, check it's findable |
| Details panel ✕ | icon 22 + 4 padding | Measure |
| Map pins | 18px circle + 3px stroke | Measure; hard to hit at low zoom? |
| Hero chevrons | default button | Measure |

### C.7 Motion and timing

- [ ] Confirm the carousel waits for the user and never advances on a timer.
- [ ] Drawer and details panel slide over 280ms. Confirm nothing flashes more than 3 times a second.

### C.8 Accessibility results matrix

Fill in P (pass), F (fail) or N/A.

| Screen | A1 | A2 | A3 | A4 | A5 | A6 | A7 | A8 | A9 | A10 | A11 | A12 |
|--------|----|----|----|----|----|----|----|----|----|-----|-----|-----|
| S1 Nav bar | | | | | | | | | | | | |
| S2 Landing | | | | | | | | | | | | |
| S3 Map drawer | | | | | | | | | | | | |
| S4 Event page | | | | | | | | | | | | |
| S5 Sign up | | | | | | | | | | | | |
| S6 Log in | | | | | | | | | | | | |
| S7 Notifications | | | | | | | | | | | | |
| S8 Messages list | | | | | | | | | | | | |
| S9 Conversation | | | | | | | | | | | | |
| S10 Profile | | | | | | | | | | | | |
| S11 Settings | | | | | | | | | | | | |

---

## Part D: Heuristic walkthrough

Each evaluator walks every screen alone, then the team merges findings. Use 3 evaluators if you can. Three people catch problems one person misses.

For each screen, answer the prompts, log every problem in the issue log (Part E.2) with a heuristic code and severity.

### D.1 Screen-by-screen prompts

**S1 Nav bar**
- H1: Does the nav bar show which screen you're on? Does it show whether you're logged in?
- H4: Is "Log In" still showing after login? Does the profile icon show before login?
- H6: Are the three round icons recognisable without labels or tooltips?
- H7: Does search work from Enter only, or is there a button too?

**S2 Landing page**
- H1: After a category click, does anything near the buttons confirm the change?
- H2: Is "EXPLORE" understood as "all events"?
- H4: Are all category buttons the same style and case? Does "MORE INFO" open the event shown in the hero, or always the same event?
- H8: Is the hero image text readable and not crowded?
- H9: What does the user see if events fail to load? ("Events unavailable right now." Is that actionable?)
- Do event cards respond to a click?

**S3 Map drawer**
- H1: Is the drawer tab discoverable? Does its chevron show which way it will slide?
- H3: How many clicks does it take to close the details panel and the drawer?
- H6: Does the details panel repeat enough info that the user doesn't have to remember the pin?

**S4 Event page**
- H3: Is there a way back to where the user came from?
- H4: Why are the three filter options shown twice? Does "Sort: Relevant ⌄" behave like the dropdown it looks like?
- H8: Does the duplicated filter panel add clutter?
- H1: After posting, does the comment appear and the input clear? Is the comment count updated?
- H5: Can the user post an empty comment?
- Do 👍, Reply, "View N replies", 🖼 and ☺ do anything? If they don't, the user sees controls that look clickable and do nothing.

**S5 Sign up**
- H3: Does the back arrow at the top work?
- H5: Are password rules shown before submitting? Are required fields marked?
- H9: Do errors say which field is wrong and how to fix it? Is the error near the field?
- H1: Is the 5-interest limit communicated before the user hits it?
- H4: "Sign UP" vs "Log In" vs "Login": is casing consistent across buttons and links?
- H2: Do the social buttons work? If they don't, should the team hide them?

**S6 Log in**
- H9: Is the error specific ("Incorrect email or password") without leaking whether the email exists?
- H7: Does Enter submit from both fields?
- H10: Is there a "Forgot password" path? (User Story 4.)

**S7 Notifications**
- H1: Read vs unread?
- H7: Can the user click a notification to go to its source?
- H3: Can the user dismiss or clear notifications?

**S8 / S9 Messages**
- H3: Is there a way back from a conversation to the list?
- H7: Does Enter send a message?
- H1: Does a sent message appear right away? Is there a sent/failed state?
- H6: Is it clear which messages are yours?
- Is there an empty state when the user has no conversations?

**S10 Profile**
- H1: Is the active Going/Been tab obvious?
- H4: Is "Edit" (secondary) vs "Settings" (primary) the right emphasis? Editing the profile is the main action on this screen.
- H2: Do "Friends" and "Joined" mean what the user expects? "Joined" what?
- Would a user guess the event rows are clickable?

**S11 Settings**
- H1: Do both toggles show their current state in text? (Night Mode label stays "Off" in the current build.)
- H2: Do the setting rows look like buttons? Does clicking "Password: ••••••••" open an edit view?
- H4: Log Out is styled like a setting row. Is that consistent with other actions?
- H5: Does Delete Account ask for confirmation and explain the consequence? Does Log Out need one?
- H3: Is there a way back to Profile?

### D.2 Cross-screen checks

- [ ] **H3 / H4:** Back navigation. List every screen and how the user leaves it. Screens with no Back or breadcrumb and only the logo as an exit are findings.
- [ ] **H4:** Two blues are in use (#2E6BE8 inline, #2F80ED in `app.css`) and two oranges (#F0512B, #F4502F). Are buttons and backgrounds consistent from screen to screen?
- [ ] **H4:** Card corner radius, button padding and font sizes: consistent across screens?
- [ ] **H1:** Loading states: does anything show while Ticketmaster data or images load?
- [ ] **H10:** Is there any help, onboarding or tooltip anywhere in the app?
- [ ] **H2:** Dates and times: consistent format across landing cards ("Sat 21 Feb, 5:00 PM"), event page ("Saturday, February 21st, 2026") and profile ("Sat 06:00")?

---

## Part E: Recording results

### E.1 Severity scale (Nielsen)

| Rating | Meaning | Fix by |
|--------|---------|--------|
| 0 | Not a usability problem | |
| 1 | Cosmetic. Fix if there's time | Backlog |
| 2 | Minor. Low priority | Next sprint |
| 3 | Major. Users struggle or fail a task | Before release |
| 4 | Catastrophe. Blocks a task, loses data, or blocks an assistive tech user | Now |

Any A1 (keyboard) or A7 (no accessible name) failure that blocks a task is severity 4 for keyboard and screen reader users.

### E.2 Issue log

One row per problem. Merge duplicates from different evaluators and participants into one row, and count how many hit it.

| ID | Screen | Task / check | Heuristic(s) | Description | Evidence (quote, time, screenshot) | # participants | Severity | Suggested fix | Jira ticket |
|----|--------|--------------|--------------|-------------|------------------------------------|----------------|----------|---------------|-------------|
| 1 | | | | | | | | | |
| 2 | | | | | | | | | |
| 3 | | | | | | | | | |

### E.3 Per-participant task sheet

Copy once per participant.

**Participant:** P__ **Date:** ____ **Facilitator:** ____ **Note-taker:** ____

| Task | Outcome (S / SD / F / GU) | Time | Errors | SEQ (1–7) | Notes |
|------|---------------------------|------|--------|-----------|-------|
| T1 Featured events | | | | | |
| T2 Filter | | | | | |
| T3 Map | | | | | |
| T4 Search | | | | | |
| T5 Sign up | | | | | |
| T6 Log in | | | | | |
| T7 Event + comment | | | | | |
| T8 Notifications | | | | | |
| T9 Messages | | | | | |
| T10 Profile | | | | | |
| T11 Settings + log out | | | | | |
| T12 Delete account | | | | | |

**SUS score:** ____

S = success, SD = success with difficulty, F = fail, GU = gave up.

---

## Part F: Summary for the report

Fill in after all sessions.

### F.1 Task results

| Task | Success rate | Mean time | Mean SEQ | Top issue |
|------|--------------|-----------|----------|-----------|
| T1 | /5 | | | |
| T2 | /5 | | | |
| T3 | /5 | | | |
| T4 | /5 | | | |
| T5 | /5 | | | |
| T6 | /5 | | | |
| T7 | /5 | | | |
| T8 | /5 | | | |
| T9 | /5 | | | |
| T10 | /5 | | | |
| T11 | /5 | | | |
| T12 | /5 | | | |

**Mean SUS:** ____

### F.2 Issues per heuristic

| Heuristic | Sev 4 | Sev 3 | Sev 2 | Sev 1 | Worst example |
|-----------|-------|-------|-------|-------|---------------|
| H1 Visibility of status | | | | | |
| H2 Match real world | | | | | |
| H3 User control | | | | | |
| H4 Consistency | | | | | |
| H5 Error prevention | | | | | |
| H6 Recognition over recall | | | | | |
| H7 Flexibility | | | | | |
| H8 Minimalist design | | | | | |
| H9 Error recovery | | | | | |
| H10 Help | | | | | |
| A1–A12 Accessibility | | | | | |

### F.3 Top 5 fixes

1.
2.
3.
4.
5.

---

## Appendix: Readiness checklist

In the build from 2026-09-25, the handlers below print to the console and do nothing else. Check each one before testing. If a feature is still a stub, skip the tasks and checks that depend on it and mark them N/A. Don't log a stub as a usability failure.

| Feature | Handler | Needed for |
|---------|---------|------------|
| Search | `NavBarController.onSearch` | T4 |
| Click event card on landing | `LandingPageController.onEventCardClick` | T2, D.1 S2 |
| MORE INFO opens the selected event | `onMoreInfoClick` routes to a fixed `EventPage.fxml` | T1, T7 |
| Post comment, filter and sort comments | `EventPageController` | T7 |
| Send message | `MessagesController.onSendClick` | T9 |
| Going / Been tabs, event rows, Edit | `ProfileController` | T10 |
| Setting rows, Night Mode, Delete Account | `SettingsController` | T11, T12 |
| Social sign-up | `SignUpController.onFacebookClick` etc. | T5 (optional) |
| Remove dev-only "Log In" button from nav bar | `nav-bar.fxml` | T6, D.1 S1 |
| Notifications list loads real data | `NotificationsController` (empty) | T8 |
