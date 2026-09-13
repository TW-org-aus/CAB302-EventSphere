package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onLoginClick() {
        System.out.println("Login clicked with email: " + emailField.getText());
    }

    @FXML
    protected void onSignUpClick() {
        Router.navigateTo("SignUp.fxml");
    }
}
