package com.eventsphere.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {

    @FXML
    protected void onEditProfileClick() {
        System.out.println("Edit profile clicked");
    }

    @FXML
    protected void onGoingTabClick() {
        System.out.println("Going tab clicked");
    }

    @FXML
    protected void onBeenTabClick() {
        System.out.println("Been tab clicked");
    }

    @FXML
    protected void onWarehouseEventClick() {
        System.out.println("Event clicked: Warehouse night: local DJs and a rooftop bar after");
    }

    @FXML
    protected void onSunriseEventClick() {
        System.out.println("Event clicked: Sunrise 5k along the river, coffee after for everyone who turns up");
    }

    @FXML
    protected void onTriviaEventClick() {
        System.out.println("Event clicked: Trivia night, teams of four, first round is free");
    }

    @FXML
    protected void onSettingsClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1280, 800));
    }
}
