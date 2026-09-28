package com.eventsphere.app.service;

import java.util.List;

import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.model.User;

/**
 * Read side of the Going feature: who is going to an event, plus the small display
 * helpers the "Who's going" screen needs. Filtering (active users only) and ordering
 * (surname, then first name) are done by GoingDAO.
 */
public class GoingService {

    static final String NOBODY_GOING = "Nobody's going yet";

    private final IGoingDAO going;

    public GoingService(IGoingDAO going) {
        this.going = going;
    }

    /** Active users going to the event, ordered by surname then first name. */
    public List<User> attendeesFor(int eventId) {
        return going.findUsersForEvent(eventId);
    }

    /** Heading text for the attendee count, e.g. "1 person going" / "3 people going". */
    public static String headingFor(int count) {
        if (count <= 0) {
            return NOBODY_GOING;
        }
        return count + (count == 1 ? " person going" : " people going");
    }

    /** Two-letter avatar initials, e.g. "AL" for Ada Lovelace. "?" if no name is stored. */
    public static String initialsFor(User user) {
        String initials = firstLetter(user.getFirstName()) + firstLetter(user.getLastName());
        return initials.isEmpty() ? "?" : initials.toUpperCase();
    }

    private static String firstLetter(String name) {
        return name == null || name.isBlank() ? "" : name.trim().substring(0, 1);
    }
}