package com.eventsphere.app.dao;

import com.eventsphere.app.model.User;

import java.util.List;
import java.util.Optional;

/** DAO for user accounts; deactivation is a soft delete that preserves comments and messages. */
public interface IUserDAO {

    /** Legacy insert used by the seeder; prefer the overload that takes a username. */
    int insert(String firstName, String lastName, String email, String passwordHash,
               Double homeLat, Double homeLong);

    /** Preferred insert that also sets the username; returns the generated UserID. */
    int insert(String firstName, String lastName, String email, String passwordHash,
               Double homeLat, Double homeLong, String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(int userId);

    /** Returns active users only. */
    List<User> findAllActive();

    /** Overwrites the stored row with the user object's current field values. */
    void update(User user);

    void setHomeLocation(int userId, Double homeLat, Double homeLong);

    void setNotifyEnabled(int userId, boolean enabled);

    /** Soft-deletes the account: clears likes, going, notifications, preferences and home location, but keeps the row so comments and messages retain their author. */
    void deactivate(int userId);

    /** Returns true if any other user (excluding excludeUserId) already holds that username. */
    boolean isUsernameTaken(String username, int excludeUserId);

    void setUsername(int userId, String username);
}
