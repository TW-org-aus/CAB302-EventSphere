package com.eventsphere.app.controller;

import com.eventsphere.app.LoginController;
import com.eventsphere.app.NavBarController;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class LoginControllerTest {

    private LoginController controller;

    @Start
    void start(Stage stage) throws Exception {
        SessionManager session = new SessionManager();
        AuthService auth = new AuthService(new StubUserDAO(), session);
        controller = new LoginController(auth);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/eventsphere/app/login-view.fxml"));
        loader.setControllerFactory(cls -> {
            if (cls == LoginController.class) return controller;
            if (cls == NavBarController.class) return new NavBarController(auth, session);
            throw new RuntimeException("Unexpected controller: " + cls);
        });

        stage.setScene(new Scene(loader.load(), 800, 600));
        stage.show();
    }

    @Test
    void blankEmailShowsError(FxRobot robot) {
        robot.clickOn(".primary-button");
        assertTrue(robot.lookup("#loginErrorLabel").<javafx.scene.Node>query().isVisible());
    }

    @Test
    void unknownEmailShowsInvalidCredsError(FxRobot robot) {
        robot.clickOn("#emailField").write("nobody@test.com");
        robot.clickOn("#passwordField").write("password123");
        robot.clickOn(".primary-button");

        javafx.scene.control.Labeled label =
                robot.lookup("#loginErrorLabel").queryLabeled();
        assertTrue(label.isVisible());
        assertEquals("Incorrect email or password", label.getText());
    }

    @Test
    void showSignUpSuccessPopulatesEmailAndShowsMessage(FxRobot robot) {
        robot.interact(() -> controller.showSignUpSuccess("alice@test.com"));

        assertEquals("alice@test.com",
                robot.lookup("#emailField").<javafx.scene.control.TextField>query().getText());
        assertTrue(robot.lookup("#signUpMessageLabel").<javafx.scene.Node>query().isVisible());
    }
}
