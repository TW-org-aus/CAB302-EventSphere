package com.eventsphere.app;

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
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root, 1280, 800));
            return fxmlLoader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + fxmlFile, e);
        }
    }
}
