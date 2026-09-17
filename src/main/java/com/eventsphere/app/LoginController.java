package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.LoginResult;

public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private Label signUpMessageLabel;

    @FXML
    private Label loginErrorLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    // Called by SignUpController after a successful registration.
    public void showSignUpSuccess(String email) {
        signUpMessageLabel.setText("Account created. Log in to get started.");
        setShown(signUpMessageLabel, true);

        if (email != null && !email.isBlank()) {
            emailField.setText(email.strip());
            passwordField.requestFocus();
        }
    }
    // Runs when trying to log in
    @FXML
    protected void onLoginClick() {
        LoginResult result;
        try {
            result = authService.login(emailField.getText(), passwordField.getText());
        } catch (RuntimeException e) {
            showError("Could not login, try again.");
            e.printStackTrace();
            return;
        }

        if (!result.isSuccess()) {
            showError(result.getError());
            passwordField.clear();
            return;
        }

        Router.navigateTo("landing-page.fxml");
    }

    @FXML
    protected void onSignUpClick() {
        Router.navigateTo("SignUp.fxml");
    }

    private void showError(String message) {
        setShown(signUpMessageLabel, false);
        loginErrorLabel.setText(message);
        setShown(loginErrorLabel, true);
    }

    private static void setShown(Label label, boolean show) {
        label.setVisible(show);
        label.setManaged(show);
    }
}
