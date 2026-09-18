package com.eventsphere.app;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.service.RegisterResult;
import com.eventsphere.app.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.util.LinkedHashSet;
import java.util.Set;

public class SignUpController {

    private final UserService userService;

    public SignUpController(UserService userService) {
        this.userService = userService;
    }

    private static final int MAX_INTERESTS = 5;

    private static final String BUBBLE_DEFAULT_STYLE =
            "-fx-background-color: #F7F7F7; -fx-text-fill: #1A1A1A; -fx-border-color: #E5E7EB; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6 14 6 14; -fx-font-size: 12px;";

    private static final String BUBBLE_SELECTED_STYLE =
            "-fx-background-color: #E3F0FF; -fx-text-fill: #2F80ED; -fx-border-color: #2F80ED; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6 14 6 14; -fx-font-size: 12px; -fx-font-weight: bold;";


    private static final String ERROR_STYLE = "-fx-font-size: 12px; -fx-text-fill: #C0392B;";

    @FXML private Label formMessageLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField addressField;

    @FXML
    private FlowPane interestsFlowPane;

    @FXML
    private Label interestsHintLabel;

    private final Set<Category> selectedInterests = new LinkedHashSet<>();

    @FXML
    public void initialize() {
        for (Category category : Category.values()) {
            ToggleButton bubble = new ToggleButton(category.getDbValue());
            bubble.setStyle(BUBBLE_DEFAULT_STYLE);
            bubble.setOnAction(e -> onInterestToggled(bubble, category));
            interestsFlowPane.getChildren().add(bubble);
        }
        updateInterestsHint();
    }

    private void onInterestToggled(ToggleButton bubble, Category category) {
        if (bubble.isSelected()) {
            if (selectedInterests.size() >= MAX_INTERESTS) {
                bubble.setSelected(false);
                return;
            }
            selectedInterests.add(category);
            bubble.setStyle(BUBBLE_SELECTED_STYLE);
        } else {
            selectedInterests.remove(category);
            bubble.setStyle(BUBBLE_DEFAULT_STYLE);
        }
        updateInterestsHint();
    }

    private void updateInterestsHint() {
        interestsHintLabel.setText(selectedInterests.size() + "/" + MAX_INTERESTS + " selected");
    }

    @FXML
    protected void onSignUpClick() {

        RegisterResult result;
        try{
            // TODO: pass the picked suggestion's coordinates once the address dropdown is built.
            result = userService.register(firstNameField.getText(), lastNameField.getText(), emailField.getText(),
                    passwordField.getText(), null, null, selectedInterests);

        } catch (RuntimeException e){
            showMessage("Could not create your account. Please try again.");
            e.printStackTrace();
            return;
        }
        if (!result.isSuccess()) {
            showMessage(result.getError());

            return;
        }


        LoginController login = Router.navigateToWithController("login-view.fxml");
        login.showSignUpSuccess(emailField.getText());

    }

    private void showMessage(String message) {

        formMessageLabel.setText(message);
        formMessageLabel.setStyle(ERROR_STYLE);
        formMessageLabel.setVisible(true);
        formMessageLabel.setManaged(true);
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
}
