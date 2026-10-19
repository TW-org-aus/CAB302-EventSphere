package com.eventsphere.app.controller;

import com.eventsphere.app.NavBarController;
import com.eventsphere.app.SignUpController;
import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.places.IPlacesClient;
import com.eventsphere.app.places.PlaceLocation;
import com.eventsphere.app.places.Suggestion;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;
import com.eventsphere.app.service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class SignUpControllerTest {

    @Start
    void start(Stage stage) throws Exception {
        SessionManager session = new SessionManager();
        UserService users = new UserService(new StubUserDAO(), new StubPreferenceDAO());
        AuthService auth = new AuthService(new StubUserDAO(), session);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/eventsphere/app/SignUp.fxml"));
        loader.setControllerFactory(cls -> {
            if (cls == SignUpController.class) return new SignUpController(users, new StubPlacesClient());
            if (cls == NavBarController.class) return new NavBarController(auth, session);
            throw new RuntimeException("Unexpected controller: " + cls);
        });

        stage.setScene(new Scene(loader.load(), 900, 800));
        stage.show();
    }

    @Test
    void blankFieldsShowsError(FxRobot robot) {
        robot.clickOn("Next →");
        assertTrue(robot.lookup("#formMessageLabel").<Node>query().isVisible());
    }

    @Test
    void validStep1ProceedsToStep2(FxRobot robot) {
        robot.clickOn("#firstNameField").write("Alice");
        robot.clickOn("#lastNameField").write("Smith");
        robot.clickOn("#emailField").write("alice@test.com");
        robot.clickOn("#passwordField").write("password123");
        robot.clickOn("Next →");

        assertTrue(robot.lookup("#step2Content").<Node>query().isVisible());
        assertFalse(robot.lookup("#step1Content").<Node>query().isVisible());
    }

    static class StubPreferenceDAO implements IPreferenceDAO {
        @Override public Preference findByUser(int i) {
            return new Preference(i, null, null, List.of());
        }
        @Override public void upsertAddress(int i, String a) {}
        @Override public void upsertBio(int i, String b) {}
        @Override public void replaceCategories(int i, Collection<Category> c) {}
    }

    static class StubPlacesClient implements IPlacesClient {
        @Override public List<Suggestion> autocomplete(String input, String token) { return List.of(); }
        @Override public PlaceLocation fetchDetails(String placeId, String token) {
            throw new UnsupportedOperationException();
        }
    }
}
