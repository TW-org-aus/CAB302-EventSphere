package com.eventsphere.app.service;

import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.model.User;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class UserService {

    public static final int MAX_INTERESTS = 5;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_BIO_WORDS = 50;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 30;

    static final String EMAIL_TAKEN = "That email is already registered";
    static final String USERNAME_TAKEN = "That username is already taken";
    static final String WRONG_PASSWORD = "Current password is incorrect";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
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
                                   Set<Category> interests, String username, String bio) {

        String normalisedEmail = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        String trimmedUsername = username == null ? null : username.strip();

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
        if (!validUsername(trimmedUsername)) {
            return RegisterResult.failure("Username must be " + MIN_USERNAME_LENGTH + "–" + MAX_USERNAME_LENGTH
                    + " characters, letters/numbers/underscores only");
        }
        if (!withinInterestLimit(interests)) {
            return RegisterResult.failure("Pick at most " + MAX_INTERESTS + " interests");
        }
        String trimmedBio = bio == null ? "" : bio.strip();
        if (!trimmedBio.isEmpty() && wordCount(trimmedBio) > MAX_BIO_WORDS) {
            return RegisterResult.failure("Bio must be " + MAX_BIO_WORDS + " words or fewer");
        }

        if (users.findByEmail(normalisedEmail).isPresent()) {
            return RegisterResult.failure(EMAIL_TAKEN);
        }
        if (users.isUsernameTaken(trimmedUsername, 0)) {
            return RegisterResult.failure(USERNAME_TAKEN);
        }

        int userId;
        try {
            userId = users.insert(
                    firstName.strip(),
                    lastName.strip(),
                    normalisedEmail,
                    PasswordHasher.hash(rawPassword),
                    homeLat,
                    homeLng,
                    trimmedUsername);
        } catch (RuntimeException e) {
            if (isDuplicateEmail(e)) {
                return RegisterResult.failure(EMAIL_TAKEN);
            }
            throw e;
        }

        savePreferences(userId, interests, trimmedBio.isEmpty() ? null : trimmedBio);

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

    public Preference getPreferences(int userId) {
        return preferences.findByUser(userId);
    }

    public Optional<String> updateProfile(int userId, String bio, Set<Category> interests) {
        String trimmedBio = bio == null ? "" : bio.strip();

        if (!trimmedBio.isEmpty() && wordCount(trimmedBio) > MAX_BIO_WORDS) {
            return Optional.of("Bio must be " + MAX_BIO_WORDS + " words or fewer");
        }
        if (!withinInterestLimit(interests)) {
            return Optional.of("Pick at most " + MAX_INTERESTS + " interests");
        }
        preferences.upsertBio(userId, trimmedBio.isEmpty() ? null : trimmedBio);
        preferences.replaceCategories(userId, interests == null ? Set.of() : interests);
        return Optional.empty();
    }

    public Optional<String> changeUsername(User user, String username) {
        String trimmed = username == null ? null : username.strip();
        if (!validUsername(trimmed)) {
            return Optional.of("Username must be " + MIN_USERNAME_LENGTH + "–" + MAX_USERNAME_LENGTH
                    + " characters, letters/numbers/underscores only");
        }
        if (users.isUsernameTaken(trimmed, user.getUserId())) {
            return Optional.of(USERNAME_TAKEN);
        }
        String old = user.getUsername();
        user.setUsername(trimmed);
        try {
            users.setUsername(user.getUserId(), trimmed);
        } catch (RuntimeException e) {
            user.setUsername(old);
            throw e;
        }
        return Optional.empty();
    }

    public boolean isUsernameTaken(String username, int excludeUserId) {
        if (username == null || username.isBlank()) return false;
        return users.isUsernameTaken(username.strip(), excludeUserId);
    }

    // using helper methods to val
    public Optional<String> validateCredentials(String firstName, String lastName,
                                                String email, String rawPassword) {
        String normalisedEmail = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        if (!validName(firstName)) return Optional.of("Enter your first name");
        if (!validName(lastName)) return Optional.of("Enter your last name");
        if (!validEmail(normalisedEmail)) return Optional.of("Enter a valid email address");
        if (!validPassword(rawPassword)) {
            return Optional.of("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
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

    public boolean validUsername(String username) {
        return username != null
                && username.length() >= MIN_USERNAME_LENGTH
                && username.length() <= MAX_USERNAME_LENGTH
                && USERNAME_PATTERN.matcher(username).matches();
    }

    boolean withinInterestLimit(Set<Category> interests) {
        return interests == null || interests.size() <= MAX_INTERESTS;
    }

    private static int wordCount(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.strip().split("\\s+").length;
    }

    // Stores interests and optional bio on first registration.
    private void savePreferences(int userId, Set<Category> interests, String bio) {
        if (interests != null && !interests.isEmpty()) {
            preferences.replaceCategories(userId, interests);
        }
        if (bio != null && !bio.isBlank()) {
            preferences.upsertBio(userId, bio);
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
