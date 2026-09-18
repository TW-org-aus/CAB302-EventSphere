package com.eventsphere.app;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.places.IPlacesClient;
import com.eventsphere.app.places.PlaceLocation;
import com.eventsphere.app.places.Suggestion;
import com.eventsphere.app.service.RegisterResult;
import com.eventsphere.app.service.UserService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SignUpController {

    private final UserService userService;
    private final IPlacesClient places;

    // Regenerated after every fetchDetails() call: Google bills autocomplete + fetchDetails
    // together as one session, keyed by this token, so a new lookup needs a new token.
    private String sessionToken = UUID.randomUUID().toString();
    private Double selectedLat;
    private Double selectedLng;

    public SignUpController(UserService userService, IPlacesClient places) {
        this.userService = userService;
        this.places = places;
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
    private ListView<Suggestion> suggestionsList;

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

        // Suggestion rows show their address text, not Suggestion's default toString().
        suggestionsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Suggestion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getText());
            }
        });
        addressField.textProperty().addListener((obs, oldText, newText) -> onAddressTyped(newText));
        suggestionsList.setOnMouseClicked(e -> onSuggestionPicked());
    }

    private void onAddressTyped(String text) {
        // Typing again invalidates whatever was picked before.
        selectedLat = null;
        selectedLng = null;
        if (text == null || text.isBlank()) {
            hideSuggestions();
            return;
        }
        Task<List<Suggestion>> lookup = new Task<>() {
            @Override
            protected List<Suggestion> call() throws Exception {
                return places.autocomplete(text, sessionToken);
            }
        };
        lookup.setOnSucceeded(e -> showSuggestions(lookup.getValue()));
        lookup.setOnFailed(e -> hideSuggestions());
        new Thread(lookup).start();

    }

    private void onSuggestionPicked() {
        Suggestion picked = suggestionsList.getSelectionModel().getSelectedItem();
        if (picked == null) {
            return;
        }
        Task<PlaceLocation> details = new Task<>() {
            @Override
            protected PlaceLocation call() throws Exception {
                return places.fetchDetails(picked.getPlaceId(), sessionToken);
            }
        };
        details.setOnSucceeded(e -> {
            PlaceLocation location = details.getValue();
            selectedLat = location.getLat();
            selectedLng = location.getLng();
            addressField.setText(picked.getText());
            hideSuggestions();
            // The session that token was billing for is closed now that fetchDetails ran.
            sessionToken = UUID.randomUUID().toString();
        });
        new Thread(details).start();
    }

    private void showSuggestions(List<Suggestion> suggestions) {
        suggestionsList.getItems().setAll(suggestions);
        boolean hasResults = !suggestions.isEmpty();
        suggestionsList.setVisible(hasResults);
        suggestionsList.setManaged(hasResults);
    }

    private void hideSuggestions() {
        suggestionsList.getItems().clear();
        suggestionsList.setVisible(false);
        suggestionsList.setManaged(false);
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
            result = userService.register(firstNameField.getText(), lastNameField.getText(), emailField.getText(),
                    passwordField.getText(), selectedLat, selectedLng, selectedInterests);



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
