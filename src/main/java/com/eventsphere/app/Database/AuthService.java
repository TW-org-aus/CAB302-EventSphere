package com.eventsphere.app.Database;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_VERIFY = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final String BAD_CREDS = "Email or password is incorrect";
    private final UserDAO userDAO;
    private final PasswordHasher passwordHasher;

    public AuthService() {
        this(new UserDAO(Database.DBConnect()));
    }

    public AuthService(UserDAO userDAO) {
        this(userDAO, new PasswordHasher());
    }

    public AuthService(UserDAO userDAO, PasswordHasher passwordHasher) {
        this.userDAO = userDAO;
        this.passwordHasher = passwordHasher;
    }

    public AuthResult signUp(String firstName, String lastName, String email, String password) {
        if (isBlank(firstName)) {
            return AuthResult.fail("Please enter first name");
        }
        if (isBlank(lastName)) {
            return AuthResult.fail("Please enter last name");
        }
        if (isBlank(email)) {
            return AuthResult.fail("Please enter email");
        }
        if (!EMAIL_VERIFY.matcher(email.trim()).matches()) {
            return AuthResult.fail("That is not a valid email address");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return AuthResult.fail("Password too short, must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        try {
            User user = userDAO.insert(firstName, lastName, email, password, null, null);
            return AuthResult.success(user);
        } catch (DuplicateEmailException ex) {
            return AuthResult.fail("This email has already created an account");
        } catch (SQLException ex) {
            ex.printStackTrace();
            return AuthResult.fail("Unable to create account, try again");
        }
    }

    public AuthResult logIn(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            return AuthResult.fail("Please enter email and password");
        }

        try {
            Optional <User> match = userDAO.findByEmail(email);

            if (match.isEmpty()) {
                return AuthResult.fail(BAD_CREDS);
            }

            User user = match.get();
            if (!passwordHasher.verification(password, user.passwordHash())) {
                return AuthResult.fail(BAD_CREDS);
            }

            if (!user.active()) {
                return AuthResult.fail("This account is deactivated");
            }

            return AuthResult.success(user);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return AuthResult.fail("Unable to log in, try again");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
