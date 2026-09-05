package com.eventsphere.app;

import com.eventsphere.app.Database.DBController;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {

        new DBController(); // new DB controller object as soon as application launches runs CreateTables method to create updated DB tables
        Router.setStage(stage);
        stage.setTitle("EventSphere");
        Router.navigateTo("landing-page.fxml");
        stage.show();
    }
}
