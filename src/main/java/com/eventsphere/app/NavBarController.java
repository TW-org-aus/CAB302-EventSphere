package com.eventsphere.app;

import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavBarController {

    private final AuthService authService;
    private final SessionManager session;

    @FXML private Button authButton;

    public NavBarController(AuthService authService, SessionManager session) {
        this.authService = authService;
        this.session = session;
    }

    @FXML
    protected void initialize() {
        authButton.setText(session.isLoggedIn() ? "Log Out" : "Log In");
    }

    @FXML
    protected void onLogoClick() {
        Router.navigateTo("landing-page.fxml");
    }

    @FXML
    protected void onSearch() {
        System.out.println("Search submitted");
    }

    @FXML
    protected void onNotificationsClick() {
        Router.navigateTo("notifications-view.fxml");
    }

    @FXML
    protected void onMessagesClick() {
        Router.navigateTo("messages-list-view.fxml");
    }

    @FXML
    protected void onProfileClick() {
        Router.navigateTo("profile-view.fxml");
    }

    @FXML
    protected void onAuthClick() {
        if (session.isLoggedIn()) {
            authService.logout();
            Router.navigateTo("landing-page.fxml");
        } else {
            Router.navigateTo("login-view.fxml");
        }
    }
}
