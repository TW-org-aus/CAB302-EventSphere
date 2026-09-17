package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import com.eventsphere.app.service.AuthService;

public class SettingsController {

    private final AuthService authService;

    public SettingsController(AuthService authService) {
        this.authService = authService;
    }

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
        authService.logout();
        Router.navigateTo("login-view.fxml");
    }

    @FXML
    protected void onDeleteAccountClick() {
        System.out.println("Delete account clicked");
    }
}
