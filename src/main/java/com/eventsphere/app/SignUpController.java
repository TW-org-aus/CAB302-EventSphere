package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SignUpController {

    // Stub interest list: the 9 base Categories from DBController.CATEGORY_LIST
    // plus invented sub-categories. UI-only for now — not wired to the DB schema.
    private static final List<String> INTEREST_OPTIONS = List.of(
            "Music", "Live Music", "Sports", "Arts & Theatre", "Film", "Comedy",
            "Family", "Community", "Food & Drink", "Craft Beer", "Nightlife",
            "Festivals", "Outdoors", "Fitness & Wellness", "Markets", "Charity",
            "Workshops", "Tech", "Gaming", "Other"
    );

    private static final int MAX_INTERESTS = 5;

    private static final String BUBBLE_DEFAULT_STYLE =
            "-fx-background-color: #F7F7F7; -fx-text-fill: #1A1A1A; -fx-border-color: #E5E7EB; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6 14 6 14; -fx-font-size: 12px;";

    private static final String BUBBLE_SELECTED_STYLE =
            "-fx-background-color: #E3F0FF; -fx-text-fill: #2F80ED; -fx-border-color: #2F80ED; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6 14 6 14; -fx-font-size: 12px; -fx-font-weight: bold;";

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField cityField;

    @FXML
    private FlowPane interestsFlowPane;

    @FXML
    private Label interestsHintLabel;

    private final Set<String> selectedInterests = new LinkedHashSet<>();

    @FXML
    public void initialize() {
        for (String interest : INTEREST_OPTIONS) {
            ToggleButton bubble = new ToggleButton(interest);
            bubble.setStyle(BUBBLE_DEFAULT_STYLE);
            bubble.setOnAction(e -> onInterestToggled(bubble));
            interestsFlowPane.getChildren().add(bubble);
        }
        updateInterestsHint();
    }

    private void onInterestToggled(ToggleButton bubble) {
        String interest = bubble.getText();
        if (bubble.isSelected()) {
            if (selectedInterests.size() >= MAX_INTERESTS) {
                bubble.setSelected(false);
                return;
            }
            selectedInterests.add(interest);
            bubble.setStyle(BUBBLE_SELECTED_STYLE);
        } else {
            selectedInterests.remove(interest);
            bubble.setStyle(BUBBLE_DEFAULT_STYLE);
        }
        updateInterestsHint();
    }

    private void updateInterestsHint() {
        interestsHintLabel.setText(selectedInterests.size() + "/" + MAX_INTERESTS + " selected");
    }

    @FXML
    protected void onSignUpClick() {
        System.out.println("Sign up clicked with name: " + firstNameField.getText() + " " + lastNameField.getText()
                + ", email: " + emailField.getText()
                + ", username: " + usernameField.getText()
                + ", city: " + cityField.getText()
                + ", interests: " + selectedInterests);
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
