package com.eventsphere.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        Router.setStage(stage);
        stage.setTitle("EventSphere");
        Router.navigateTo("landing-page.fxml");
        stage.show();
    }
}
