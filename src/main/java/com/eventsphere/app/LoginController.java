package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Label signUpMessageLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    // Called by SignUpController after a successful registration.
    public void showSignUpSuccess(String email) {
        signUpMessageLabel.setText("Account created. Log in to get started.");
        signUpMessageLabel.setVisible(true);
        signUpMessageLabel.setManaged(true);

        if (email != null && !email.isBlank()) {
            emailField.setText(email.strip());
            passwordField.requestFocus();
        }
    }

    @FXML
    protected void onLoginClick() {
        System.out.println("Login clicked with email: " + emailField.getText());
    }

    @FXML
    protected void onSignUpClick() {
        Router.navigateTo("SignUp.fxml");
    }
}
