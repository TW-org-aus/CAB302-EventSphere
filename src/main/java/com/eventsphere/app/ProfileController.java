package com.eventsphere.app;

import javafx.fxml.FXML;

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
    protected void onSettingsClick() {
        Router.navigateTo("settings-view.fxml");
    }
}
