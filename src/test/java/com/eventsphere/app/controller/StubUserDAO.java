package com.eventsphere.app.controller;

import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.User;

import java.util.List;
import java.util.Optional;

class StubUserDAO implements IUserDAO {
    @Override public int insert(String f, String l, String e, String p, Double la, Double lo) { return 0; }
    @Override public int insert(String f, String l, String e, String p, Double la, Double lo, String u) { return 0; }
    @Override public Optional<User> findByEmail(String e) { return Optional.empty(); }
    @Override public Optional<User> findById(int i) { return Optional.empty(); }
    @Override public List<User> findAllActive() { return List.of(); }
    @Override public void update(User u) {}
    @Override public void setHomeLocation(int i, Double a, Double b) {}
    @Override public void setNotifyEnabled(int i, boolean b) {}
    @Override public void deactivate(int i) {}
    @Override public boolean isUsernameTaken(String u, int e) { return false; }
    @Override public void setUsername(int i, String u) {}
}
