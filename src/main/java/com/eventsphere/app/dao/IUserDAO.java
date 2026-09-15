package com.eventsphere.app.dao;

import com.eventsphere.app.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserDAO {

    // Returns the generated UserID. DateCreated, IsActive and NotifyEnabled.
    int insert(String firstName, String lastName, String email, String passwordHash);

    // As above, with an optional home location.
    int insert(String firstName, String lastName, String email, String passwordHash,
               Double homeLat, Double homeLong);

    Optional<User> findByEmail(String email);

    Optional<User> findById(int userId);

    // Active users only ordered.
    List<User> findAllActive();

    // Overwrites the stored row with the user's current field values.
    void update(User user);

    void setHomeLocation(int userId, Double homeLat, Double homeLong);

    void setNotifyEnabled(int userId, boolean enabled);

    // Soft delete. The row stays so comments and messages keep their author.
    void deactivate(int userId);
}
