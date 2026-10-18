package com.eventsphere.app.service;

import com.eventsphere.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager session;
    private User user;

    @BeforeEach
    void setUp() {
        session = new SessionManager();
        user = new User(1, "Aang", "Air", "aang@example.com", "hash",
                null, null, LocalDate.now(), true, true, null);
    }

    @Test
    void startSetsCurrentUser() {
        session.start(user);
        assertEquals(user, session.getCurrentUser().orElseThrow());
    }

    @Test
    void startWithNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> session.start(null));
    }

    @Test
    void clearRemovesUser() {
        session.start(user);
        session.clear();
        assertTrue(session.getCurrentUser().isEmpty());
    }

    @Test
    void isLoggedInReflectsState() {
        assertFalse(session.isLoggedIn());
        session.start(user);
        assertTrue(session.isLoggedIn());
        session.clear();
        assertFalse(session.isLoggedIn());
    }
}
