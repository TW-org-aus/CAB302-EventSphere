package com.eventsphere.app.service;

import com.eventsphere.app.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserServiceDeleteAccountTest {
    private static final String EMAIL = "angg@email.com";
    private static final String PASSWORD = "SecretPassword2";

    private MockUserDAO users;
    private UserService userService;
    private AuthService auth;
    private User user;

    @BeforeEach
    void setUp() {
        users = new MockUserDAO();
        userService = new UserService(users, new MockPreferenceDAO());
        auth = new AuthService(users, new SessionManager());
        int id = users.insert("Angg", "Air", EMAIL, PasswordHasher.hash(PASSWORD));
        user = users.findById(id).orElseThrow();
    }

    @Test
    void wrongPasswordKeepsAccount() {
        assertTrue(userService.deleteAccount(user, "not the password").isPresent());
        assertTrue(user.isActive());
        assertTrue(auth.login(EMAIL, PASSWORD).isSuccess());
    }

    @Test
    void correctPasswordDeactivatesAccount() {
        assertTrue(userService.deleteAccount(user, PASSWORD).isEmpty());
        assertFalse(user.isActive());
        assertFalse(auth.login(EMAIL, PASSWORD).isSuccess());
    }
}
