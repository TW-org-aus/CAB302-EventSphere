package com.eventsphere.app.dao;

import com.eventsphere.app.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private Connection connection;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {

        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE Users (
                    UserID INTEGER PRIMARY KEY AUTOINCREMENT,
                    FirstName TEXT NOT NULL,
                    LastName TEXT NOT NULL,
                    Email TEXT NOT NULL UNIQUE,
                    PasswordHash TEXT NOT NULL,
                    HomeLat REAL,
                    HomeLong REAL,
                    DateCreated DATE NOT NULL DEFAULT (date('now')),
                    IsActive INTEGER NOT NULL DEFAULT 1,
                    NotifyEnabled INTEGER NOT NULL DEFAULT 1
                )
                """);
        }

        userDAO = new UserDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void insertAndFindUserByEmail() {

        int userId = userDAO.insert(
                "Test",
                "User",
                "test@example.com",
                "fakePasswordHash"
        );

        Optional<User> result = userDAO.findByEmail("test@example.com");

        assertTrue(result.isPresent());

        User user = result.get();

        assertEquals(userId, user.getUserId());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("fakePasswordHash", user.getPasswordHash());
    }


@Test
void duplicateEmailShouldFail() {

    userDAO.insert(
            "Test",
            "User",
            "test@example.com",
            "fakePasswordHash"
    );

    assertThrows(RuntimeException.class, () -> {
        userDAO.insert(
                "Another",
                "User",
                "test@example.com",
                "anotherFakeHash"
        );
    });
}



@Test
void findUserById() {

    int userId = userDAO.insert(
            "Test",
            "User",
            "test@example.com",
            "fakePasswordHash"
    );

    Optional<User> result = userDAO.findById(userId);

    assertTrue(result.isPresent());

    User user = result.get();

    assertEquals(userId, user.getUserId());
    assertEquals("Test", user.getFirstName());
    assertEquals("User", user.getLastName());
    assertEquals("test@example.com", user.getEmail());
}

@Test
void findByEmailShouldReturnEmptyWhenUserDoesNotExist() {

    Optional<User> result = userDAO.findByEmail("missing@example.com");

    assertTrue(result.isEmpty());
}
}