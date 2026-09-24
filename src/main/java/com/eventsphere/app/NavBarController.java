package com.eventsphere.app;

import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class NavBarController {

    private final AuthService authService;
    private final SessionManager session;

    @FXML private Button authButton;
    @FXML private TextField searchField;

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

    // Enter in the search bar: load the landing page, then show matching events there.
    // A blank search just shows the normal landing page.
    @FXML
    protected void onSearch() {
        String keyword = searchField.getText();
        LandingPageController landing = Router.navigateToWithController("landing-page.fxml");
        if (keyword != null && !keyword.isBlank()) {
            landing.showSearchResults(keyword.trim());
        }
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
