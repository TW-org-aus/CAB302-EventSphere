package com.eventsphere.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private static final String EMAIL = "example@gmail.com";
    private static final String PASSWORD = "example of a password123/";

    private MockUserDAO users;
    private SessionManager session;
    private AuthService auth;
    private int userId;

    @BeforeEach
    void setUp() {
        users = new MockUserDAO();
        session = new SessionManager();
        auth = new AuthService(users, session);
        userId = users.insert("John", "Smith", EMAIL, PasswordHasher.hash(PASSWORD));
    }

    @Test
    void correctLogin() {
        LoginResult result = auth.login(EMAIL, PASSWORD);

        assertTrue(result.isSuccess());
        assertEquals(userId, result.getUser().getUserId());
        assertEquals(userId, session.getCurrentUser().orElseThrow().getUserId());
    }

    @Test
    void emailSameDisregardingCaseAndSpace() {
        assertTrue(auth.login("    EXample@GmAil.CoM  ", PASSWORD).isSuccess());
    }

    @Test
    void wrongPassword() {
        LoginResult result = auth.login(EMAIL, "Incorrect Password");

        assertFalse(result.isSuccess());
        assertEquals(AuthService.INVALID_CREDS, result.getError());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void wrongEmailAndWrongPasswordGetSameMessage() {
        String wrongEmailError = auth.login("weirdemail@gmail.com", PASSWORD).getError();
        String wrongPasswordError = auth.login(EMAIL, "Incorrect Password").getError();
        assertEquals(wrongPasswordError, wrongEmailError);
    }

    @Test
    void deactivatedCanNotLogin() {
        users.deactivate(userId);

        assertEquals(AuthService.INVALID_CREDS, auth.login(EMAIL, PASSWORD).getError());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void blankFieldsRejected() {
        assertEquals("Please enter email", auth.login("", PASSWORD).getError());
        assertEquals("Please enter password", auth.login(EMAIL, "").getError());
    }

    @Test
    void loggingOutClearsSession() {
        auth.login(EMAIL, PASSWORD);
        auth.logout();
        assertFalse(session.isLoggedIn());
        assertTrue(session.getCurrentUser().isEmpty());
    }
}
