package com.eventsphere.app;

import com.eventsphere.app.places.IPlacesClient;
import com.eventsphere.app.places.PlaceLocation;
import com.eventsphere.app.places.Suggestion;
import com.eventsphere.app.service.RegisterResult;
import com.eventsphere.app.service.UserService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/* multi-step sign-up, changed from single step to multi-step */
public class SignUpController {

    private final UserService userService;
    private final IPlacesClient places;

    private String sessionToken = UUID.randomUUID().toString();
    private Double selectedLat;
    private Double selectedLng;

    public SignUpController(UserService userService, IPlacesClient places) {
        this.userService = userService;
        this.places = places;
    }

    private static final String ERROR_STYLE = "-fx-font-size: 12px; -fx-text-fill: #C0392B;";
    private static final String SUCCESS_STYLE = "-fx-font-size: 11px; -fx-text-fill: #27AE60;";
    private static final String WARN_STYLE = "-fx-font-size: 11px; -fx-text-fill: #C0392B;";

    // Step containers
    @FXML private VBox step1Content;
    @FXML private VBox step2Content;
    @FXML private VBox step1Footer;
    @FXML private VBox step2Footer;

    // Step 1 fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Step 2 fields
    @FXML private TextField usernameField;
    @FXML private Label usernameCheckLabel;
    @FXML private TextArea bioField;
    @FXML private TextField addressField;
    @FXML private ListView<Suggestion> suggestionsList;
    @FXML private FlowPane interestsFlowPane;
    @FXML private Label interestsHintLabel;

    @FXML private Label formMessageLabel;

    private InterestPicker interests;

    @FXML
    public void initialize() {
        interests = new InterestPicker(interestsFlowPane, interestsHintLabel, Set.of());

        suggestionsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Suggestion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getText());
            }
        });
        addressField.textProperty().addListener((obs, oldText, newText) -> onAddressTyped(newText));
        suggestionsList.setOnMouseClicked(e -> onSuggestionPicked());

        usernameField.focusedProperty().addListener((obs, wasFocused, isNow) -> {
            if (!isNow) checkUsername();
        });
    }

    // ----- Step navigation -----

    @FXML
    protected void onNextClick() {
        Optional<String> error = userService.validateCredentials(
                firstNameField.getText(), lastNameField.getText(),
                emailField.getText(), passwordField.getText());
        if (error.isPresent()) {
            showMessage(error.get());
            return;
        }
        hideMessage();
        step1Content.setVisible(false);
        step1Content.setManaged(false);
        step2Content.setVisible(true);
        step2Content.setManaged(true);
        step1Footer.setVisible(false);
        step1Footer.setManaged(false);
        step2Footer.setVisible(true);
        step2Footer.setManaged(true);
    }

    @FXML
    protected void onBackClick() {
        hideMessage();
        step2Content.setVisible(false);
        step2Content.setManaged(false);
        step1Content.setVisible(true);
        step1Content.setManaged(true);
        step2Footer.setVisible(false);
        step2Footer.setManaged(false);
        step1Footer.setVisible(true);
        step1Footer.setManaged(true);
    }

    // ----- Username check -----

    private void checkUsername() {
        String val = usernameField.getText().strip();
        if (val.isEmpty()) {
            usernameCheckLabel.setVisible(false);
            usernameCheckLabel.setManaged(false);
            return;
        }
        if (!userService.validUsername(val)) {
            usernameCheckLabel.setText("3–30 characters, letters/numbers/underscores only");
            usernameCheckLabel.setStyle(WARN_STYLE);
            usernameCheckLabel.setVisible(true);
            usernameCheckLabel.setManaged(true);
            return;
        }
        boolean taken = userService.isUsernameTaken(val, 0);
        usernameCheckLabel.setText(taken ? "Username is taken" : "Username is available");
        usernameCheckLabel.setStyle(taken ? WARN_STYLE : SUCCESS_STYLE);
        usernameCheckLabel.setVisible(true);
        usernameCheckLabel.setManaged(true);
    }

    // ----- google places Address autocomplete -----

    private void onAddressTyped(String text) {
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
        if (picked == null) return;
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

    // ----- Final submission -----

    @FXML
    protected void onSignUpClick() {
        String bio = bioField.getText().strip();
        RegisterResult result;
        try {
            result = userService.register(
                    firstNameField.getText(), lastNameField.getText(),
                    emailField.getText(), passwordField.getText(),
                    selectedLat, selectedLng, interests.getSelected(),
                    usernameField.getText().strip(),
                    bio.isEmpty() ? null : bio);
        } catch (RuntimeException e) {
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

    // ----- Misc navigation -----

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

    // ----- Helpers -----

    private void showMessage(String message) {
        formMessageLabel.setText(message);
        formMessageLabel.setStyle(ERROR_STYLE);
        formMessageLabel.setVisible(true);
        formMessageLabel.setManaged(true);
    }

    private void hideMessage() {
        formMessageLabel.setVisible(false);
        formMessageLabel.setManaged(false);
    }
}
