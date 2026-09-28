package com.eventsphere.app;

import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class NavBarController {

    private final AuthService authService;
    private final SessionManager session;

    @FXML private Button authButton;
    @FXML private StackPane notificationsButton;
    @FXML private StackPane messagesButton;
    @FXML private StackPane profileButton;
    @FXML private TextField searchField;

    public NavBarController(AuthService authService, SessionManager session) {
        this.authService = authService;
        this.session = session;
    }

    @FXML
    public void initialize() {
        boolean loggedIn = session.isLoggedIn();
        setShown(notificationsButton, loggedIn);
        setShown(messagesButton, loggedIn);
        setShown(profileButton, loggedIn);
        authButton.setText(loggedIn ? "Log Out" : "Log In");
    }

    private static void setShown(Node node, boolean show) {
        node.setVisible(show);
        node.setManaged(show);
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
        if (!session.isLoggedIn()) {
            Router.navigateTo("login-view.fxml");
            return;
        }
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