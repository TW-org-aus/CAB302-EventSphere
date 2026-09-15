package com.eventsphere.app.dao;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;

import java.util.List;

public interface IGoingDAO {

    //true if a Going row was added, false if the user was already going.
    boolean markGoing(int userId, int eventId);

    boolean unmarkGoing(int userId, int eventId);

    // false for deactivated users, so they fail the messaging Going check.
    boolean isGoing(int userId, int eventId);

    // Events the user is going to, ordered by start time ascending.
    List<Event> findEventsForUser(int userId);

    // Active users only, ordered by surname then first name.
    List<User> findUsersForEvent(int eventId);

    void setMuted(int userId, int eventId, boolean muted);


    boolean isMuted(int userId, int eventId);
}
