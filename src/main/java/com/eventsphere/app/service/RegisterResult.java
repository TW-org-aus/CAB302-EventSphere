package com.eventsphere.app.service;

public class RegisterResult {

    private final boolean success;
    private final String error;
    private final int userId;

    private RegisterResult(boolean success, String error, int userId) {
        this.success = success;
        this.error = error;
        this.userId = userId;
    }

    public static RegisterResult ok(int userId) {
        return new RegisterResult(true, null, userId);
    }

    public static RegisterResult failure(String error) {
        return new RegisterResult(false, error, 0);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public int getUserId() {
        return userId;
    }
}
