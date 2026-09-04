package com.eventsphere.app;

import javafx.fxml.FXML;

public class NavBarController {

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

    // Temporary dev-only entry point until real auth/session state exists to gate this properly.
    @FXML
    protected void onLoginClick() {
        Router.navigateTo("login-view.fxml");
    }
}
