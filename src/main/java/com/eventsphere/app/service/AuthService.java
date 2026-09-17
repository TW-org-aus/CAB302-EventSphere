package com.eventsphere.app.service;

import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.User;

import java.util.Locale;
import java.util.Optional;
// login and logout
public class AuthService {
    // only one message for wrong password or email so there is no sign which is wrong
    static final String INVALID_CREDS = "Incorrect email or password";

    private final IUserDAO users;
    private final SessionManager session;

    public AuthService(IUserDAO users, SessionManager session) {
        this.users = users;
        this.session = session;
    }

    public LoginResult login(String email, String rawPassword) {
        if (email == null || email.isBlank()) {
            return LoginResult.fail("Please enter email");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            return LoginResult.fail("Please enter password");
        }
        // This is used to match how UserServices holds onto emails
        String normalisedEmail = email.strip().toLowerCase(Locale.ROOT);

        Optional<User> found = users.findByEmail(normalisedEmail);
        if (found.isEmpty()) {
            return LoginResult.fail(INVALID_CREDS);
        }

        User user = found.get();
        if (!user.isActive() || !PasswordHasher.verify(rawPassword, user.getPasswordHash())) {
            return LoginResult.fail(INVALID_CREDS);
        }

        session.start(user);
        return LoginResult.ok(user);
    }

    public void logout() {
        session.clear();
    }
}
