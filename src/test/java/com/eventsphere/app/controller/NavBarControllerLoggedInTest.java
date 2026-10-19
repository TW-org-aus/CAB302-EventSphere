package com.eventsphere.app.controller;

import com.eventsphere.app.NavBarController;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class NavBarControllerLoggedInTest {

    @Start
    void start(Stage stage) throws Exception {
        SessionManager session = new SessionManager();
        User alice = new User(1, "Alice", "Smith", "alice@test.com", "hash",
                null, null, LocalDate.now(), true, false, null);
        session.start(alice);

        AuthService auth = new AuthService(new StubUserDAO(), session);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/eventsphere/app/nav-bar.fxml"));
        loader.setControllerFactory(cls -> new NavBarController(auth, session));

        stage.setScene(new Scene(loader.load(), 900, 72));
        stage.show();
    }

    @Test
    void loggedInShowsNavButtons(FxRobot robot) {
        assertTrue(robot.lookup("#notificationsButton").<Node>query().isVisible());
        assertTrue(robot.lookup("#messagesButton").<Node>query().isVisible());
        assertTrue(robot.lookup("#profileButton").<Node>query().isVisible());
    }

    @Test
    void loggedInShowsLogOutText(FxRobot robot) {
        assertEquals("Log Out", robot.lookup("#authButton").queryLabeled().getText());
    }
}
