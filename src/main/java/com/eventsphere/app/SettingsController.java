package com.eventsphere.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private ToggleButton notificationsToggle;

    @FXML
    protected void onUsernameClick() {
        System.out.println("Username setting clicked");
    }

    @FXML
    protected void onPasswordClick() {
        System.out.println("Password setting clicked");
    }

    @FXML
    protected void onEmailClick() {
        System.out.println("Email setting clicked");
    }

    @FXML
    protected void onNameClick() {
        System.out.println("Name setting clicked");
    }

    @FXML
    protected void onNightModeToggle() {
        System.out.println("Night mode toggled");
    }

    @FXML
    protected void onNotificationsToggle() {
        boolean enabled = notificationsToggle.isSelected();
        notificationsToggle.setText(enabled ? "On" : "Off");
        System.out.println("Notifications toggled " + (enabled ? "on" : "off"));
    }

    @FXML
    protected void onLogOutClick() {
        System.out.println("Log out clicked");
    }

    @FXML
    protected void onDeleteAccountClick() {
        System.out.println("Delete account clicked");
    }

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("profile-view.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1280, 800));
    }
}
