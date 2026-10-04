package com.eventsphere.app.dao;

import com.eventsphere.app.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserDAO {

    // old --> used for the seeder and I CBF to remove it, cause that means updating the seeder which is not used anymore anyway
    int insert(String firstName, String lastName, String email, String passwordHash,
               Double homeLat, Double homeLong);

    // new--> USE THIS ONE PLEASE. this is the most updated insert method and is what should be used from now on
    int insert(String firstName, String lastName, String email, String passwordHash,
               Double homeLat, Double homeLong, String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(int userId);

    // Active users only ordered.
    List<User> findAllActive();

    // Overwrites the stored row with the user's current field values.
    void update(User user);

    void setHomeLocation(int userId, Double homeLat, Double homeLong);

    void setNotifyEnabled(int userId, boolean enabled);

    // Soft delete. The row stays so comments and messages keep their author. While likes, going, notifications, preferences and home location is removed.
    void deactivate(int userId);

    // Returns true if any other user already holds that username
    boolean isUsernameTaken(String username, int excludeUserId);

    void setUsername(int userId, String username);
}
