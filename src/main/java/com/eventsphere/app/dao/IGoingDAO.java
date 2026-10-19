package com.eventsphere.app.dao;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;

import java.util.List;

/** DAO for tracking which users are attending which events, and mute preferences per attendance. */
public interface IGoingDAO {

    /** Returns true if a Going row was added; false if the user was already marked going. */
    boolean markGoing(int userId, int eventId);

    boolean unmarkGoing(int userId, int eventId);

    /** Returns false for deactivated users, so they fail the messaging Going check. */
    boolean isGoing(int userId, int eventId);

    /** Returns true when both users are going to at least one common event; required before they can message each other. */
    boolean shareGoingEvent(int userA, int userB);

    /** Returns events the user is going to, ordered by StartTime ascending. */
    List<Event> findEventsForUser(int userId);

    /** Returns active attendees of the event, ordered by surname then first name. */
    List<User> findUsersForEvent(int eventId);

    void setMuted(int userId, int eventId, boolean muted);

    boolean isMuted(int userId, int eventId);
}
