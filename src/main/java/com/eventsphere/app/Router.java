package com.eventsphere.app;

import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.dao.PreferenceDAO;
import com.eventsphere.app.dao.UserDAO;
import com.eventsphere.app.places.IPlacesClient;
import com.eventsphere.app.places.PlacesClient;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.EventService;
import com.eventsphere.app.service.SessionManager;
import com.eventsphere.app.service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;

/**
 * Global navigation entry point. Holds the app's single {@link Stage} so any
 * controller can swap the displayed screen without needing an ActionEvent to
 * fish the Stage out of a clicked Node's scene graph.
 */
public class Router {

    private static Stage stage;

    private static final Connection CONNECTION = Database.DBConnect();
    private static final EventService EVENTS = new EventService(new EventDAO(CONNECTION));

    // small refactor by me alex, I just changed it because UserService and
    // AuthService both read the Users table, so they share one DAO.
    // its better to declare users and userDAO before so it can be passed down
    private static final IUserDAO USER_DAO = new UserDAO(CONNECTION);
    private static final UserService USERS =
            new UserService(USER_DAO, new PreferenceDAO(CONNECTION));
    private static final SessionManager SESSION = new SessionManager();
    private static final AuthService AUTH = new AuthService(USER_DAO, SESSION);
    private static final IPlacesClient PLACES = new PlacesClient();

    private Router() {
    }

    public static void setStage(Stage stage) {
        Router.stage = stage;
    }

    /** Navigates to the given FXML file (resolved next to this class, i.e. com.eventsphere.app). */
    public static void navigateTo(String fxmlFile) {
        navigateToWithController(fxmlFile);
    }

    /** Same as {@link #navigateTo(String)}, but returns the new screen's controller so callers can pass it data. */
    public static <T> T navigateToWithController(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Router.class.getResource(fxmlFile));
            fxmlLoader.setControllerFactory(Router::createController);
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root, 1280, 800));
            return fxmlLoader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + fxmlFile, e);
        }
    }

    private static Object createController(Class<?> type) {
        if (type == LandingPageController.class) return new LandingPageController(EVENTS);
        if (type == SignUpController.class) return new SignUpController(USERS, PLACES);
        if (type == LoginController.class) return new LoginController(AUTH);
        if (type == SettingsController.class) return new SettingsController(AUTH);
        if (type == NavBarController.class) return new NavBarController(AUTH, SESSION);
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create controller " + type.getName(), e);
        }
    }
}
