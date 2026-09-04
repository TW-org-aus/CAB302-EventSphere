package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onSignUpClick() {
        System.out.println("Sign up clicked with email: " + emailField.getText() + ", username: " + usernameField.getText());
    }

    @FXML
    protected void onLoginClick() {
        Router.navigateTo("login-view.fxml");
    }

    @FXML
    protected void onFacebookClick() {
        System.out.println("Sign up with Facebook clicked");
    }

    @FXML
    protected void onInstagramClick() {
        System.out.println("Sign up with Instagram clicked");
    }

    @FXML
    protected void onGoogleClick() {
        System.out.println("Sign up with Google clicked");
    }

    @FXML
    protected void onTikTokClick() {
        System.out.println("Sign up with TikTok clicked");
    }
}
