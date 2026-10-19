package com.eventsphere.app.controller;

import com.eventsphere.app.NavBarController;
import com.eventsphere.app.ProfileController;
import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.model.User;
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
class ProfileControllerTest {

    @Start
    void start(Stage stage) throws Exception {
        SessionManager session = new SessionManager();
        AuthService auth = new AuthService(new StubUserDAO(), session);
        UserService users = new UserService(new StubUserDAO(), new StubPreferenceDAO());

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/eventsphere/app/profile-view.fxml"));
        loader.setControllerFactory(cls -> {
            if (cls == ProfileController.class) return new ProfileController(users, session, new StubGoingDAO());
            if (cls == NavBarController.class) return new NavBarController(auth, session);
            throw new RuntimeException("Unexpected controller: " + cls);
        });

        stage.setScene(new Scene(loader.load(), 900, 700));
        stage.show();
    }

    @Test
    void noUserSetsNotSignedInLabel(FxRobot robot) {
        assertEquals("Not signed in", robot.lookup("#nameLabel").queryLabeled().getText());
    }

    @Test
    void noUserSetsAvatarPlaceholder(FxRobot robot) {
        assertEquals("?", robot.lookup("#avatarLabel").queryLabeled().getText());
    }

    @Test
    void noUserDisablesEditButton(FxRobot robot) {
        assertTrue(robot.lookup("#editButton").<Node>query().isDisable());
    }

    static class StubPreferenceDAO implements IPreferenceDAO {
        @Override public Preference findByUser(int i) {
            return new Preference(i, null, null, List.of());
        }
        @Override public void upsertAddress(int i, String a) {}
        @Override public void upsertBio(int i, String b) {}
        @Override public void replaceCategories(int i, Collection<Category> c) {}
    }

    static class StubGoingDAO implements IGoingDAO {
        @Override public boolean markGoing(int u, int e) { return false; }
        @Override public boolean unmarkGoing(int u, int e) { return false; }
        @Override public boolean isGoing(int u, int e) { return false; }
        @Override public boolean shareGoingEvent(int a, int b) { return false; }
        @Override public List<Event> findEventsForUser(int u) { return List.of(); }
        @Override public List<User> findUsersForEvent(int e) { return List.of(); }
        @Override public void setMuted(int u, int e, boolean m) {}
        @Override public boolean isMuted(int u, int e) { return false; }
    }
}
