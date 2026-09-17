package com.eventsphere.app.Database;

// used when there is an insert or update that would break the unique constraint email has
public class DuplicateEmailException extends Exception {
    // used to show on screen which email is the problem
    private final String email;

    public DuplicateEmailException(String email, Throwable cause) {
        // cause is the SQL exception
        super("An account already exists for" + email, cause);
        this.email = email;
    }
    // the normalised email
    public String getEmail() {
        return email;
    }
}
