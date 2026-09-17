package com.eventsphere.app.service;

import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.Category;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class UserService {

    public static final int MAX_INTERESTS = 5;
    public static final int MIN_PASSWORD_LENGTH = 8;

    static final String EMAIL_TAKEN = "That email is already registered";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final String UNIQUE_EMAIL_VIOLATION = "UNIQUE constraint failed: Users.Email";
    private static final int MAX_CAUSE_DEPTH = 10;

    private final IUserDAO users;
    private final IPreferenceDAO preferences;

    public UserService(IUserDAO users, IPreferenceDAO preferences) {
        this.users = users;
        this.preferences = preferences;
    }

    public RegisterResult register(String firstName, String lastName, String email,
                                   String rawPassword, String city, Set<Category> interests) {

        String normalisedEmail = email == null ? null : email.strip().toLowerCase(Locale.ROOT);

        if (!validName(firstName)) {
            return RegisterResult.failure("Enter your first name");
        }
        if (!validName(lastName)) {
            return RegisterResult.failure("Enter your last name");
        }
        if (!validEmail(normalisedEmail)) {
            return RegisterResult.failure("Enter a valid email address");
        }
        if (!validPassword(rawPassword)) {
            return RegisterResult.failure("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (!withinInterestLimit(interests)) {
            return RegisterResult.failure("Pick at most " + MAX_INTERESTS + " interests");
        }

        if (users.findByEmail(normalisedEmail).isPresent()) {
            return RegisterResult.failure(EMAIL_TAKEN);
        }

        int userId;
        try {
            userId = users.insert(
                    firstName.strip(),
                    lastName.strip(),
                    normalisedEmail,
                    PasswordHasher.hash(rawPassword),
                    mapLat(city),
                    mapLong(city));
        } catch (RuntimeException e) {

            if (isDuplicateEmail(e)) {
                return RegisterResult.failure(EMAIL_TAKEN);
            }
            throw e;
        }

        savePreferences(userId, city, interests);

        return RegisterResult.ok(userId);
    }


    // ----- helpers ------

    boolean validName(String name) {
        return name != null && !name.isBlank();
    }

    boolean validEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    boolean validPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    boolean withinInterestLimit(Set<Category> interests) {
        return interests == null || interests.size() <= MAX_INTERESTS;
    }

    private void savePreferences(int userId, String city, Set<Category> interests) {
        if (city != null && !city.isBlank()) {
            preferences.upsertCity(userId, city.strip());
        }
        if (interests != null && !interests.isEmpty()) {
            preferences.replaceCategories(userId, interests);
        }
    }

    private static boolean isDuplicateEmail(Throwable error) {
        Throwable cause = error;
        for (int depth = 0; cause != null && depth < MAX_CAUSE_DEPTH; depth++) {
            String message = cause.getMessage();
            if (message != null && message.contains(UNIQUE_EMAIL_VIOLATION)) {
                return true;
            }
            if (cause.getCause() == cause) {
                break;
            }
            cause = cause.getCause();
        }
        return false;
    }

    // will impliment when google api is hooked up for geocaching
    private Double mapLat(String city) {
        return null;
    }

    private Double mapLong(String city) {
        return null;
    }
}
