package com.eventsphere.app.service;

import com.eventsphere.app.model.User;
// Used by LoginController to return either the authenticated user or an error message.
public class LoginResult {

    private final boolean success;
    private final String error;
    private final User user;

    private LoginResult(boolean success, String error, User user) {
        this.success = success;
        this.error = error;
        this.user = user;
    }

    public static LoginResult ok(User user) {
        return new LoginResult(true, null, user);
    }

    public static LoginResult fail(String error) {
        return new LoginResult(false, error, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public User getUser() {
        return user;
    }
}
