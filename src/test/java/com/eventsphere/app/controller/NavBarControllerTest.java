package com.eventsphere.app.controller;

import com.eventsphere.app.NavBarController;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class NavBarControllerTest {

    @Start
    void start(Stage stage) throws Exception {
        SessionManager session = new SessionManager();
        AuthService auth = new AuthService(new StubUserDAO(), session);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/eventsphere/app/nav-bar.fxml"));
        loader.setControllerFactory(cls -> new NavBarController(auth, session));

        stage.setScene(new Scene(loader.load(), 900, 72));
        stage.show();
    }

    @Test
    void notLoggedInHidesNavButtons(FxRobot robot) {
        assertFalse(robot.lookup("#notificationsButton").<Node>query().isVisible());
        assertFalse(robot.lookup("#messagesButton").<Node>query().isVisible());
        assertFalse(robot.lookup("#profileButton").<Node>query().isVisible());
    }

    @Test
    void notLoggedInShowsLogInText(FxRobot robot) {
        assertEquals("Log In", robot.lookup("#authButton").queryLabeled().getText());
    }
}
