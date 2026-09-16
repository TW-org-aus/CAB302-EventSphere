package com.eventsphere.app;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.Database.Database;
import com.eventsphere.app.ingestion.TicketmasterIngestor;
import com.eventsphere.app.seeding.DatabaseSeeder;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        Connection connection = Database.DBConnect();
        new DBController(connection); // new DB controller object as soon as application launches runs CreateTables method to create updated DB tables



        // Demo data. Runs before the first screen loads so the landing page is never empty.
        try {
            DatabaseSeeder.seed(connection);
        } catch (RuntimeException seedFailure) {
            System.err.println("Database seeding failed: " + seedFailure.getMessage());
            seedFailure.printStackTrace();
        }



        TicketmasterIngestor.refreshInBackgroundIfStale(); // after DBController, so the tables and columns exist
        Router.setStage(stage);
        stage.setTitle("EventSphere");
        Router.navigateTo("landing-page.fxml");
        stage.show();
    }
}
