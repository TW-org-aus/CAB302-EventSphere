package com.eventsphere.app;

import javafx.application.Application;

/** Entry point that delegates to JavaFX Application.launch so module-path restrictions don't block the real application class. */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
