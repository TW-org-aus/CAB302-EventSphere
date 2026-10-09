# Notifications Feature Handoff

Stage 1 (tests) is complete. This document covers what to implement for
Stage 2 (notification loading) and Stage 3 (silence toggles).

The tests are already written and RED. Implement against them.

---

## Stage 2 — Notification loading

### `NotificationService` (all methods are stubbed)

File: `src/main/java/com/eventsphere/app/service/NotificationService.java`

Every method currently throws `UnsupportedOperationException`. Implement them:

**`getForUser(int userId)`**
Return `notifications.findByUser(userId)`.

**`markAllRead(int userId)`**
Call `notifications.markAllRead(userId)`.

**`create(int userId, NotificationType type, Integer eventId, Integer commentId, Integer conversationId, String message)`**
Call `notifications.insert(userId, type, eventId, commentId, conversationId, message)`.

**`checkAndInsertReminders(int userId)`**
Call `going.findEventsForUser(userId)`. For each event, compute seconds until
`event.getStartTime()`. Skip if in the past or more than 7 days away. Then:

| Window | Type | Message |
|---|---|---|
| `1d < until ≤ 7d` | `EVENT_REMINDER_WEEK` | `"<title> is happening in 1 week"` |
| `12h < until ≤ 1d` | `EVENT_REMINDER_DAY` | `"<title> is happening tomorrow"` |
| `0 < until ≤ 12h` | `EVENT_REMINDER_12H` | `"<title> is happening in 12 hours"` |

Use `notifications.existsForEvent(userId, type, eventId)` to guard each insert
— this makes the method idempotent (safe to call every time the user opens the
notifications page).

---

### `CommentService.postComment`

File: `src/main/java/com/eventsphere/app/service/CommentService.java`

After `comments.insert(...)`, when `replyToCommentId != null`:

1. Find the parent comment from `comments.findByEvent(eventId)`.
2. If `parent.getUserId() == replierId`, do nothing (no self-notification).
3. Otherwise look up the replier's first name via `users.findById(replierId)`.
4. Look up the event title via `events.findById(eventId).getTitle()`.
5. Insert: `notifications.insert(parent.getUserId(), NotificationType.COMMENT_REPLY, eventId, parentCommentId, null, replierFirstName + " replied to your comment on " + eventTitle)`.

`CommentService` already has `events` and `notifications` as injected fields —
they just aren't used yet.

---

### `MessagingService.send`

File: `src/main/java/com/eventsphere/app/service/MessagingService.java`

After `messages.insert(...)`, and only when the message was not blank:

1. Find the other participant. You need `conversations.findById(conversationId)` — add this method to `IConversationDAO` and `ConversationDAO` first (one SQL query: `SELECT ... FROM Conversations WHERE ConversationID = ?`).
2. Call `convo.otherUserId(senderId)` to get the recipient.
3. Look up the sender's first name via `users.findById(senderId)`.
4. Insert: `notifications.insert(recipientId, NotificationType.NEW_MESSAGE, null, null, conversationId, senderFirstName + " sent you a message")`.

`MessagingService` already has `notifications` as an injected field.

---

### `NotificationsController` + FXML

File: `src/main/java/com/eventsphere/app/NotificationsController.java`  
FXML: `src/main/resources/com/eventsphere/app/notifications-view.fxml`

The controller is currently an empty class. The FXML has static placeholder rows.

1. Give the controller a constructor: `NotificationsController(NotificationService notificationService, SessionManager session)`.
2. Add `@FXML private VBox notificationList` and give the inner VBox `fx:id="notificationList"` in the FXML (remove the hardcoded rows).
3. In `initialize()`:
   - Get current user from `session.getCurrentUser()`.
   - Call `notificationService.checkAndInsertReminders(userId)`.
   - Call `notificationService.getForUser(userId)` and render each notification as a VBox row with a type label, message label, and relative-time label.
   - Call `notificationService.markAllRead(userId)` after rendering.
4. Wire up in `Router.createController`:
   ```java
   if (type == NotificationsController.class)
       return new NotificationsController(NOTIFICATION_SERVICE, SESSION);
   ```

For the relative time label: convert `notification.getCreatedAt()` to a human
string (e.g. "just now", "X minutes ago", "X hours ago", "X days ago") using
`Duration.between(createdAt, Instant.now())`.

For the type label, map `NotificationType` to a display string:
- `COMMENT_REPLY` → "New reply to your comment"
- `NEW_MESSAGE` → "New message"
- `EVENT_REMINDER_WEEK / _DAY / _12H` → "Event reminder"
- `EVENT_UPDATED` → "Event updated"

---

## Stage 3 — Silence toggles

### Per-event silence

The DB layer is already complete. `IGoingDAO` has:

```java
void setMuted(int userId, int eventId, boolean muted);
boolean isMuted(int userId, int eventId);
```

**What to add:**

In `NotificationService.checkAndInsertReminders`, before inserting each reminder:
```java
if (going.isMuted(userId, eventId)) continue;
```

In `CommentService.notifyReply` (the private method you added in stage 2),
before inserting the `COMMENT_REPLY`:
```java
if (going.isMuted(parentAuthorId, eventId)) return;
```

`CommentService` does not currently have `going` injected — add `IGoingDAO` to
its constructor and pass it through from `Router`.

**UI:** Add a mute toggle button on `EventPageController`. On click:
```java
going.setMuted(userId, eventId, !going.isMuted(userId, eventId));
```

**Tests to write first:**
- `muteEventPreventsReminderNotifications`
- `muteEventPreventsCommentReplyNotification`
- `unmuteEventRestoresNotifications`
- `mutingOneEventDoesNotAffectAnother`

---

### Global silence

No DB layer exists yet.

**Schema migration** — in `DBController.migrate()`, add:
```java
addColumnIfAbsent("Users", "NotificationsMuted", "INTEGER NOT NULL DEFAULT 0");
```

Write a helper `addColumnIfAbsent(table, column, definition)` that runs
`PRAGMA table_info(<table>)`, checks if the column exists, and runs
`ALTER TABLE <table> ADD COLUMN <column> <definition>` if absent.

**`User` model** — add `private final boolean notificationsMuted`, getter
`isNotificationsMuted()`, include in both constructors, include in `UserDAO.mapRow`.

**`IUserDAO`** — add:
```java
void setNotificationsMuted(int userId, boolean muted);
```

**`UserDAO`** — implement with:
```sql
UPDATE Users SET NotificationsMuted = ? WHERE UserID = ?
```

**`NotificationService.create`** — before inserting, check:
```java
users.findById(userId).map(User::isNotificationsMuted).orElse(false)
```
If true, return without inserting. Inject `IUserDAO` into `NotificationService`
(add to constructor, update `Router`).

Also apply the same guard in `CommentService.notifyReply` and
`MessagingService.send` for the target user.

**UI:** Add a global mute toggle to `NotificationsController` (e.g. a checkbox
or button at the top of the page). On toggle:
```java
userDAO.setNotificationsMuted(userId, !user.isNotificationsMuted());
```

**Tests to write first:**
- `globalMutePreventsAllNotifications`
- `globalMuteDoesNotAffectOtherUser`
- `globalUnmuteRestoresNotifications`
- `globalMutePreventsReminderNotifications`
- `globalMutePreventsCommentReplyNotification`
- `globalMutePreventsNewMessageNotification`
