package com.eventsphere.app.Database;

public record AuthResult(User user, String errorMessage) {
    public static AuthResult success(User user) {
        return new AuthResult(user, null);
    }

    public static AuthResult fail(String errorMessage) {
        return new AuthResult(null, errorMessage);
    }

    public boolean isSuccess() {
        return user != null;
    }
}
