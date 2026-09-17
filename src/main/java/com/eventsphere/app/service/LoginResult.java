package com.eventsphere.app.service;

import com.eventsphere.app.model.User;
// this is used the same as RegisterResults, the controller shows an error when isSuccess
// is false.
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
