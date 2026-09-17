package com.eventsphere.app.Database;

import java.time.LocalDate;

public record User(
    int userId,
    String firstName,
    String lastName,
    String email,
    String passwordHash,
    Double homeLat,
    Double homeLong,
    LocalDate dateCreated,
    boolean active,
    boolean notifyEnabled) {

    public String fullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "User[userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", homeLat=" + homeLat + ", homeLong=" + homeLong
                + ", dateCreated=" + dateCreated + ", active=" + active + ", notifyEnabled=" + notifyEnabled + "]";
    }
    }
