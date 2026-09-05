package com.eventsphere.app.Database;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBController {
    private static Connection connect;


    // Catagories list: keep both CHECK lists in sync if this set changes.
    private static final String CATEGORY_LIST =
            "'Music','Sports','Arts & Theatre','Film','Family','Community','Food & Drink','Nightlife','Other'";

    public DBController() {
        connect = Database.DBConnect();
        createTables();
    }

    public void createTables() {
        String schema =
            "CREATE TABLE IF NOT EXISTS Source (" +
            "    SourceID    INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    SiteName    TEXT    NOT NULL," +
            "    SiteURL     TEXT    NOT NULL" +
            ");" +

            "CREATE TABLE IF NOT EXISTS Users (" +
            "    UserID          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    FirstName       TEXT     NOT NULL," +
            "    LastName        TEXT     NOT NULL," +
            "    Email           TEXT     NOT NULL UNIQUE," +
            "    PasswordHash    TEXT     NOT NULL," +
            "    HomeLat         REAL," +
            "    HomeLong        REAL," +
            "    DateCreated     DATE     NOT NULL DEFAULT (date('now'))," +
            "    IsActive        INTEGER  NOT NULL DEFAULT 1 CHECK (IsActive IN (0, 1))," +
            "    NotifyEnabled   INTEGER  NOT NULL DEFAULT 1 CHECK (NotifyEnabled IN (0, 1))" +
            ");" +

            // Preference: per-User city + preferred Categories.
            "CREATE TABLE IF NOT EXISTS Preferences (" +
            "    UserID      INTEGER PRIMARY KEY," +
            "    City        TEXT," +
            "    FOREIGN KEY (UserID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +

            "CREATE TABLE IF NOT EXISTS PreferenceCategories (" +
            "    UserID      INTEGER NOT NULL," +
            "    Category    TEXT    NOT NULL CHECK (Category IN (" + CATEGORY_LIST + "))," +
            "    PRIMARY KEY (UserID, Category)," +
            "    FOREIGN KEY (UserID) REFERENCES Preferences (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +

            "CREATE TABLE IF NOT EXISTS Events (" +
            "    EventID         INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    Title           TEXT     NOT NULL," +
            "    Description     TEXT," +
            "    Category        TEXT CHECK (Category IN (" + CATEGORY_LIST + "))," +
            "    StartTime       DATETIME NOT NULL," +
            "    EndTime         DATETIME," +
            "    VenueName       TEXT," +
            "    Address         TEXT," +
            "    EventLat        REAL," +
            "    EventLng        REAL," +
            "    ImageURL        TEXT," +
            "    TicketURL       TEXT," + // single external booking link
            "    CreatedAt       DATETIME NOT NULL DEFAULT (datetime('now'))," +
            "    HasOccured      INTEGER  NOT NULL DEFAULT 0 CHECK (HasOccured IN (0, 1))," +
            "    SourceID        INTEGER  NOT NULL," +
            "    LikesCount      INTEGER  NOT NULL DEFAULT 0," +
            "    CommentsCount   INTEGER  NOT NULL DEFAULT 0," +
            "    FOREIGN KEY (SourceID) REFERENCES Source (SourceID)" +
            "        ON DELETE RESTRICT ON UPDATE CASCADE" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_events_sourceid ON Events (SourceID);" +

            "CREATE TABLE IF NOT EXISTS Comments (" +
            "    CommentID   INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "    UserID      INTEGER  NOT NULL," +
            "    EventsID    INTEGER  NOT NULL," +
            "    CreatedAt   DATETIME NOT NULL DEFAULT (datetime('now'))," +
            "    Content     TEXT     NOT NULL," +
            "    UpdatedAt   DATETIME," +
            "    FOREIGN KEY (UserID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (EventsID) REFERENCES Events (EventID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_comments_userid   ON Comments (UserID);" +
            "CREATE INDEX IF NOT EXISTS idx_comments_eventsid ON Comments (EventsID);" +

            // Likes apply only to Events, not Comments.
            "CREATE TABLE IF NOT EXISTS Likes (" +
            "    LikeID      INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "    UserID      INTEGER  NOT NULL," +
            "    EventsID    INTEGER  NOT NULL," +
            "    CreatedAt   DATETIME NOT NULL DEFAULT (datetime('now'))," +
            "    FOREIGN KEY (UserID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (EventsID) REFERENCES Events (EventID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    UNIQUE (UserID, EventsID)" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_likes_userid   ON Likes (UserID);" +
            "CREATE INDEX IF NOT EXISTS idx_likes_eventsid ON Likes (EventsID);" +

            // Going: a row = Going is true for that (User, Event) pair. Table created to store an event a user is going to.
            "CREATE TABLE IF NOT EXISTS Going (" +
            "    UserID          INTEGER  NOT NULL," +
            "    EventID         INTEGER  NOT NULL," +
            "    DateGoing       DATE     NOT NULL DEFAULT (date('now'))," +
            "    MuteNotify      INTEGER  NOT NULL DEFAULT 0 CHECK (MuteNotify IN (0, 1))," +
            "    PRIMARY KEY (UserID, EventID)," +
            "    FOREIGN KEY (UserID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (EventID) REFERENCES Events (EventID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_going_eventid ON Going (EventID);" +

            // Conversation: private 1:1 thread, opened only once both Users share a Going Event.
            // User1ID < User2ID so (A,B) and (B,A) can't both exist.
            "CREATE TABLE IF NOT EXISTS Conversations (" +
            "    ConversationID  INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "    User1ID         INTEGER  NOT NULL," +
            "    User2ID         INTEGER  NOT NULL," +
            "    CreatedAt       DATETIME NOT NULL DEFAULT (datetime('now'))," +
            "    CHECK (User1ID < User2ID)," +
            "    UNIQUE (User1ID, User2ID)," +
            "    FOREIGN KEY (User1ID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (User2ID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_conversations_user1 ON Conversations (User1ID);" +
            "CREATE INDEX IF NOT EXISTS idx_conversations_user2 ON Conversations (User2ID);" +

            "CREATE TABLE IF NOT EXISTS Messages (" +
            "    MessageID       INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "    ConversationID  INTEGER  NOT NULL," +
            "    SenderID        INTEGER  NOT NULL," +
            "    Content         TEXT     NOT NULL," +
            "    SentAt          DATETIME NOT NULL DEFAULT (datetime('now'))," +
            "    ReadAt          DATETIME," +
            "    FOREIGN KEY (ConversationID) REFERENCES Conversations (ConversationID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (SenderID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_messages_conversationid ON Messages (ConversationID);" +

            // Notifications
            "CREATE TABLE IF NOT EXISTS Notifications (" +
            "    NotificationID          INTEGER  PRIMARY KEY AUTOINCREMENT," +
            "    UserID                  INTEGER  NOT NULL," +
            "    Type                    TEXT     NOT NULL CHECK (Type IN ('CommentReply', 'NewMessage', 'EventReminder'))," +
            "    RelatedEventID          INTEGER," +
            "    RelatedCommentID        INTEGER," +
            "    RelatedConversationID   INTEGER," +
            "    CreatedAt               DATETIME NOT NULL DEFAULT (datetime('now'))," +
            "    FOREIGN KEY (UserID) REFERENCES Users (UserID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (RelatedEventID) REFERENCES Events (EventID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (RelatedCommentID) REFERENCES Comments (CommentID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE," +
            "    FOREIGN KEY (RelatedConversationID) REFERENCES Conversations (ConversationID)" +
            "        ON DELETE CASCADE ON UPDATE CASCADE" +
            ");" +
            "CREATE INDEX IF NOT EXISTS idx_notifications_userid ON Notifications (UserID);" +

            "CREATE TRIGGER IF NOT EXISTS trg_likes_count_after_insert" +
            "    AFTER INSERT ON Likes" +
            " BEGIN" +
            "    UPDATE Events SET LikesCount = LikesCount + 1 WHERE EventID = NEW.EventsID;" +
            "END;" +
            "CREATE TRIGGER IF NOT EXISTS trg_likes_count_after_delete" +
            "    AFTER DELETE ON Likes" +
            " BEGIN" +
            "    UPDATE Events SET LikesCount = LikesCount - 1 WHERE EventID = OLD.EventsID;" +
            "END;" +
            "CREATE TRIGGER IF NOT EXISTS trg_comments_count_after_insert" +
            "    AFTER INSERT ON Comments" +
            " BEGIN" +
            "    UPDATE Events SET CommentsCount = CommentsCount + 1 WHERE EventID = NEW.EventsID;" +
            "END;" +
            "CREATE TRIGGER IF NOT EXISTS trg_comments_count_after_delete" +
            "    AFTER DELETE ON Comments" +
            " BEGIN" +
            "    UPDATE Events SET CommentsCount = CommentsCount - 1 WHERE EventID = OLD.EventsID;" +
            "END;";

        try (Statement statement = connect.createStatement()) {
            statement.executeUpdate(schema);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public void create_Insert_user() {
        try (Statement statement = connect.createStatement()) {
            statement.execute("INSERT INTO Users (FirstName, LastName, Email, PasswordHash, HomeLat, HomeLong, IsActive)\n" +
                    "VALUES ('baily', 'bob', 'baily.bob@example.com', 'a1bssfe2c3asdas35g6', -29.4698, 13.0251, 1);");
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public void close() {
        try {
            connect.close();
        } catch (SQLException sqlEx) {
            System.err.println(sqlEx);
        }
    }
}
