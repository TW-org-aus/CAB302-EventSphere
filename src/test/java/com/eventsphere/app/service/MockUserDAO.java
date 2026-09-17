package com.eventsphere.app.service;

import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class MockUserDAO implements IUserDAO{

    private final List<User> users = new ArrayList<>();

    @Override
    public int insert(String firstName, String lastName, String email, String passwordHash) {
        return insert(firstName, lastName, email, passwordHash, null, null);
    }

    @Override
    public int insert(String firstName, String lastName, String email, String passwordHash, Double homeLat, Double homeLong) {
        int id = users.size() + 1;
        users.add(new User(id, firstName, lastName, email, passwordHash, homeLat, homeLong, LocalDate.now(), true, true));
        return id;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAllActive() {
        List<User> active = new ArrayList<>();
        for  (User user : users) {
            if(user.isActive()) {
                active.add(user);
            }
        }
        return active;
    }

    @Override
    public void update(User user) {
        if (user == null) {
            return;
        }
        findById(user.getUserId()).ifPresent(existing -> {
            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setEmail(user.getEmail());
            existing.setPasswordHash(user.getPasswordHash());
            existing.setHome(user.getHomeLat(), user.getHomeLong());
            existing.setActive(user.isActive());
            existing.setNotifyEnabled(user.isNotifyEnabled());
        });
    }

    @Override
    public void setHomeLocation(int userId, Double homeLat, Double homeLong) {
        findById(userId).ifPresent(user -> user.setHome(homeLat, homeLong));
    }

    @Override
    public void setNotifyEnabled(int userId, boolean enabled) {
        findById(userId).ifPresent(user -> user.setNotifyEnabled(enabled));
    }

    @Override
    public void deactivate(int userId) {
        findById(userId).ifPresent(user -> user.setActive(false));
    }
}
