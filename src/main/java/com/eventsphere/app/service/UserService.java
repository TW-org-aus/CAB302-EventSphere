package com.eventsphere.app.service;

import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.User;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Optional;

public class UserService {

    public static final int MAX_INTERESTS = 5;
    public static final int MIN_PASSWORD_LENGTH = 8;

    static final String EMAIL_TAKEN = "That email is already registered";
    static final String WRONG_PASSWORD = "Current password is incorrect";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final String UNIQUE_EMAIL_VIOLATION = "UNIQUE constraint failed: Users.Email";
    private static final int MAX_CAUSE_DEPTH = 10;

    private final IUserDAO users;
    private final IPreferenceDAO preferences;

    public UserService(IUserDAO users, IPreferenceDAO preferences) {
        this.users = users;
        this.preferences = preferences;
    }

    // Coordinates arrive already resolved by the caller, and are null when no address was picked.
    public RegisterResult register(String firstName, String lastName, String email,
                                   String rawPassword, Double homeLat, Double homeLng,
                                   Set<Category> interests) {

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
                    homeLat,
                    homeLng);
        } catch (RuntimeException e) {

            if (isDuplicateEmail(e)) {
                return RegisterResult.failure(EMAIL_TAKEN);
            }
            throw e;
        }

        savePreferences(userId, interests);

        return RegisterResult.ok(userId);
    }

    public Optional<String> changeName(User user, String firstName, String lastName) {
        if (!validName(firstName)) {
            return Optional.of("Enter your first name");
        }
        if (!validName(lastName)) {
            return Optional.of("Enter your last name");
        }

        String oldFirst = user.getFirstName();
        String oldLast = user.getLastName();
        user.setFirstName(firstName.strip());
        user.setLastName(lastName.strip());
        save(user, () -> {
            user.setFirstName(oldFirst);
            user.setLastName(oldLast);
        });
        return Optional.empty();
    }

    public Optional<String> changeEmail(User user, String email) {
        String normalisedEmail = email == null ? null : email.strip().toLowerCase(Locale.ROOT);

        if (!validEmail(normalisedEmail)) {
            return Optional.of("Enter a valid email address");
        }
        if (normalisedEmail.equals(user.getEmail())) {
            return Optional.empty();
        }
        if (users.findByEmail(normalisedEmail).isPresent()) {
            return Optional.of(EMAIL_TAKEN);
        }
        String oldEmail = user.getEmail();
        user.setEmail(normalisedEmail);
        try {
            save(user, () -> user.setEmail(oldEmail));
        } catch (RuntimeException e) {
            if (isDuplicateEmail(e)) {
                return Optional.of(EMAIL_TAKEN);
            }
            throw e;
        }
        return Optional.empty();
    }

    public Optional<String> changePassword(User user, String currentPassword, String newPassword) {
        if (!PasswordHasher.verify(currentPassword, user.getPasswordHash())) {
            return Optional.of(WRONG_PASSWORD);
        }
        if (!validPassword(newPassword)) {
            return Optional.of("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        String oldHash = user.getPasswordHash();
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        save(user, () -> user.setPasswordHash(oldHash));
        return Optional.empty();
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

    // Only the interests: sign-up stores coordinates on the user row, never the address text.
    private void savePreferences(int userId, Set<Category> interests) {
        if (interests != null && !interests.isEmpty()) {
            preferences.replaceCategories(userId, interests);
        }
    }

    private void save(User user, Runnable undo) {
        try {
            users.update(user);
        } catch (RuntimeException e) {
            undo.run();
            throw e;
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
}
