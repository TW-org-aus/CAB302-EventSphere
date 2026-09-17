package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import com.eventsphere.app.Database.AuthResult;
import com.eventsphere.app.Database.AuthService;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onLoginClick() {
        clearError();

        AuthResult result = authService.logIn(emailField.getText(), passwordField.getText());

        if (!result.isSuccess()) {
            showError(result.errorMessage());
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }

        Session.setCurrentUser(result.user());
        Router.navigateTo("landing-page.fxml");
    }

    @FXML
    protected void onSignUpClick() {
        Router.navigateTo("SignUp.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
