package com.eventsphere.app;

import com.eventsphere.app.model.User;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.service.UserService;
import com.eventsphere.app.service.SessionManager;
import static com.eventsphere.app.EditDialog.field;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;


public class ProfileController {

    private final UserService userService;
    private final SessionManager session;

    public ProfileController(UserService userService, SessionManager session) {
        this.userService = userService;
        this.session = session;
    }

    @FXML private Button editButton;
    @FXML private Label bioLabel;
    @FXML private HBox interestBox;

    @FXML
    protected void initialize() {
        // avoid gap
        bioLabel.managedProperty().bind(bioLabel.visibleProperty());
        interestBox.managedProperty().bind(interestBox.visibleProperty());
        showProfile();
    }

    private void showProfile() {
        Optional<Preference> profile = session.getCurrentUser().map(user -> userService.getPreferences(user.getUserId()));

        String bio = profile.map(Preference::getBio).orElse(null);
        bioLabel.setText(bio);
        bioLabel.setVisible(bio != null);

        List<Label> pills = profile.map(Preference::getCategories).orElse(Set.of()).stream().map(ProfileController::pill).toList();
        interestBox.getChildren().setAll(pills);
        interestBox.setVisible(!pills.isEmpty());

        editButton.setDisable(profile.isEmpty());
    }

    private static Label pill(Category category) {
        Label pill = new Label(category.getDbValue());
        pill.getStyleClass().add("tag-pill");
        return pill;
    }

    @FXML
    protected void onEditProfileClick() {
        session.getCurrentUser().map(User::getUserId).ifPresent(userId -> {
            Preference current = userService.getPreferences(userId);

            TextArea bio = new TextArea(Objects.requireNonNullElse(current.getBio(), ""));
            bio.setWrapText(true);
            bio.setPrefRowCount(3);
            bio.setPromptText("Up to " + userService.MAX_BIO_LENGTH + " characters");

            FlowPane bubbles = new FlowPane(8, 8);
            Label hint = new Label();
            hint.getStyleClass().add("muted-text");
            InterestPicker interests = new InterestPicker(bubbles, hint, current.getCategories());

            EditDialog.show(editButton.getScene().getWindow(), "Edit profile", () -> userService.updateProfile(userId, bio.getText(), interests.getSelected()), field("Bio", bio), field("Interests", new VBox(6, hint, bubbles)));
            showProfile();
        });
    }

    @FXML
    protected void onGoingTabClick() {
        System.out.println("Going tab clicked");
    }

    @FXML
    protected void onBeenTabClick() {
        System.out.println("Been tab clicked");
    }

    @FXML
    protected void onWarehouseEventClick() {
        System.out.println("Event clicked: Warehouse night: local DJs and a rooftop bar after");
    }

    @FXML
    protected void onSunriseEventClick() {
        System.out.println("Event clicked: Sunrise 5k along the river, coffee after for everyone who turns up");
    }

    @FXML
    protected void onTriviaEventClick() {
        System.out.println("Event clicked: Trivia night, teams of four, first round is free");
    }

    @FXML
    protected void onSettingsClick() {
        Router.navigateTo("settings-view.fxml");
    }
}
