package com.eventsphere.app;

import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.service.EventService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Global navigation entry point. Holds the app's single {@link Stage} so any
 * controller can swap the displayed screen without needing an ActionEvent to
 * fish the Stage out of a clicked Node's scene graph.
 */
public class Router {

    private static Stage stage;

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

    // just a lil change: because we are re-factoring and removing the DAO object from the landingpage controller we still need to
    //pass the DAO service somewhere when the user loads the landing page
    // atm it is just for the landing page controller but we will need to add some more logic here in future
    // to detect what DAO service needs to be passed for each page, currently the landing page is the only page that
    // accesses live data and hence the only one that needs this but more changes will come as we move away from
    // the stub data :)
    private static Object createController(Class<?> type) {
        if (type == LandingPageController.class) {
            return new LandingPageController(new EventService(new EventDAO(Database.DBConnect())));
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create controller " + type.getName(), e);
        }
    }
}
