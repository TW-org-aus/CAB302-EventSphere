package com.eventsphere.app.service;

import com.eventsphere.app.model.User;

import java.util.Optional;
// Holds the currently logged-in user for the app.
// Router owns a single SessionManager instance and shares it (currently via AuthService).
public class SessionManager {

    private User currentUser;

    public void start(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Can't start a new session with no active user");
        }
        currentUser = user;
    }

    public void clear() {
        currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
