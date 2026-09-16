package com.eventsphere.app;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.Database.Database;
import com.eventsphere.app.ingestion.TicketmasterIngestor;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {

        new DBController(Database.DBConnect()); // new DB controller object as soon as application launches runs CreateTables method to create updated DB tables
        TicketmasterIngestor.refreshInBackgroundIfStale(); // after DBController, so the tables and columns exist
        Router.setStage(stage);
        stage.setTitle("EventSphere");
        Router.navigateTo("landing-page.fxml");
        stage.show();
    }
}
