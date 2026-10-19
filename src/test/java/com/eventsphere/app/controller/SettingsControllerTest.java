package com.eventsphere.app.controller;

import com.eventsphere.app.NavBarController;
import com.eventsphere.app.SettingsController;
import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;
import com.eventsphere.app.service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class SettingsControllerTest {

    private SessionManager session;

    @Start
    void start(Stage stage) throws Exception {
        session = new SessionManager();
        setupStage(stage);
    }

    private void setupStage(Stage stage) throws Exception {
        AuthService auth = new AuthService(new StubUserDAO(), session);
        UserService users = new UserService(new StubUserDAO(), new StubPreferenceDAO());

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/eventsphere/app/settings-view.fxml"));
        loader.setControllerFactory(cls -> {
            if (cls == SettingsController.class) return new SettingsController(auth, users, session);
            if (cls == NavBarController.class) return new NavBarController(auth, session);
            throw new RuntimeException("Unexpected controller: " + cls);
        });

        stage.setScene(new Scene(loader.load(), 900, 700));
        stage.show();
    }

    @Test
    void noUserDisablesRows(FxRobot robot) {
        assertTrue(robot.lookup("#emailRow").<Button>query().isDisable());
        assertTrue(robot.lookup("#nameRow").<Button>query().isDisable());
        assertTrue(robot.lookup("#passwordRow").<Button>query().isDisable());
    }

    @Test
    void noUserShowsDashInEmailRow(FxRobot robot) {
        String text = robot.lookup("#emailRow").queryLabeled().getText();
        assertTrue(text.contains("-"), "expected '-' in email row text, got: " + text);
    }

    @Test
    void userEmailAppearsInEmailRow(FxRobot robot) {
        User alice = new User(1, "Alice", "Smith", "alice@test.com", "hash",
                null, null, LocalDate.now(), true, false, null);
        robot.interact(() -> {
            session.start(alice);
            try {
                Stage s = (Stage) robot.lookup("#emailRow").<Button>query().getScene().getWindow();
                setupStage(s);
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        String text = robot.lookup("#emailRow").queryLabeled().getText();
        assertTrue(text.contains("alice@test.com"), "expected email in row text, got: " + text);
    }

    static class StubPreferenceDAO implements IPreferenceDAO {
        @Override public Preference findByUser(int i) {
            return new Preference(i, null, null, List.of());
        }
        @Override public void upsertAddress(int i, String a) {}
        @Override public void upsertBio(int i, String b) {}
        @Override public void replaceCategories(int i, Collection<Category> c) {}
    }
}
