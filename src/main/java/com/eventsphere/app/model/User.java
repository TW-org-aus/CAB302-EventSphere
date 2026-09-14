package com.eventsphere.app.model;

import java.time.LocalDate;

public class User {

    private final int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Double homeLat;
    private Double homeLong;
    private final LocalDate dateCreated;
    private boolean active;
    private boolean notifyEnabled;

    public User(int userId, String firstName, String lastName, String email, String passwordHash,
                Double homeLat, Double homeLong, LocalDate dateCreated,
                boolean active, boolean notifyEnabled) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.homeLat = homeLat;
        this.homeLong = homeLong;
        this.dateCreated = dateCreated;
        this.active = active;
        this.notifyEnabled = notifyEnabled;
    }

    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Double getHomeLat() { return homeLat; }
    public Double getHomeLong() { return homeLong; }
    public LocalDate getDateCreated() { return dateCreated; }
    public boolean isActive() { return active; }
    public boolean isNotifyEnabled() { return notifyEnabled; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActive(boolean active) { this.active = active; }
    public void setNotifyEnabled(boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }

    //Sets both home coordinates, or clears them when passed nulls.
    public void setHome(Double homeLat, Double homeLong) {
        this.homeLat = homeLat;
        this.homeLong = homeLong;
    }

    // Display name for the UI.
    public String getFullName() {
        return firstName + " " + lastName;
    }


    // hashing skeleton
    @Override
    public boolean equals(Object other) {
        return other instanceof User user && userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }


}
