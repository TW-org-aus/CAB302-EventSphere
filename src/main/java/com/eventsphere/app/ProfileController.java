package com.eventsphere.app;

import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.SessionManager;
import com.eventsphere.app.service.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.eventsphere.app.EditDialog.field;

public class ProfileController {

    private static final DateTimeFormatter ROW_DATE =
            DateTimeFormatter.ofPattern("EEE HH:mm").withZone(ZoneId.systemDefault());

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label bioLabel;
    @FXML private HBox interestBox;
    @FXML private Label friendsCount;
    @FXML private Label goingCount;
    @FXML private Button editButton;
    @FXML private Button goingTab;
    @FXML private Button beenTab;
    @FXML private VBox eventList;

    private final UserService userService;
    private final SessionManager session;
    private final IGoingDAO going;

    private List<Event> goingEvents = List.of();
    private List<Event> beenEvents = List.of();

    public ProfileController(UserService userService, SessionManager session, IGoingDAO going) {
        this.userService = userService;
        this.session = session;
        this.going = going;
    }

    @FXML
    protected void initialize() {
        // avoid gap
        bioLabel.managedProperty().bind(bioLabel.visibleProperty());
        interestBox.managedProperty().bind(interestBox.visibleProperty());

        User user = session.getCurrentUser().orElse(null);
        if (user == null) {
            nameLabel.setText("Not signed in");
            subtitleLabel.setText("Log in to see your events");
            avatarLabel.setText("?");
            bioLabel.setVisible(false);
            interestBox.setVisible(false);
            editButton.setDisable(true);
            eventList.getChildren().add(emptyMessage("Log in to see your events."));
            return;
        }

        nameLabel.setText(user.getFullName());
        subtitleLabel.setText(user.getEmail());
        avatarLabel.setText(initials(user));

        showProfile();
        loadEvents(user);
    }

    // ----- bio and interests -----

    private void showProfile() {
        Optional<Preference> profile = session.getCurrentUser()
                .map(user -> userService.getPreferences(user.getUserId()));

        String bio = profile.map(Preference::getBio).orElse(null);
        bioLabel.setText(bio);
        bioLabel.setVisible(bio != null);

        List<Label> pills = profile.map(Preference::getCategories).orElse(Set.of())
                .stream().map(ProfileController::pill).toList();
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

            EditDialog.show(editButton.getScene().getWindow(), "Edit profile",
                    () -> userService.updateProfile(userId, bio.getText(), interests.getSelected()),
                    field("Bio", bio), field("Interests", new VBox(6, hint, bubbles)));
            showProfile();
        });
    }

    // ----- going and been -----

    private void loadEvents(User user) {
        List<Event> all = going.findEventsForUser(user.getUserId());
        Instant now = Instant.now();
        goingEvents = all.stream().filter(e -> e.getStartTime().isAfter(now)).toList();
        beenEvents = all.stream().filter(e -> !e.getStartTime().isAfter(now)).toList();

        goingCount.setText(String.valueOf(goingEvents.size()));
        friendsCount.setText("0");

        showEvents(goingEvents);
    }

    @FXML
    protected void onGoingTabClick() {
        goingTab.getStyleClass().setAll("tab-pill", "tab-pill-active");
        beenTab.getStyleClass().setAll("tab-pill");
        showEvents(goingEvents);
    }

    @FXML
    protected void onBeenTabClick() {
        beenTab.getStyleClass().setAll("tab-pill", "tab-pill-active");
        goingTab.getStyleClass().setAll("tab-pill");
        showEvents(beenEvents);
    }

    private void showEvents(List<Event> events) {
        eventList.getChildren().clear();
        if (events.isEmpty()) {
            eventList.getChildren().add(emptyMessage("Nothing here yet."));
            return;
        }
        for (Event event : events) {
            eventList.getChildren().add(buildRow(event));
        }
    }

    private HBox buildRow(Event event) {
        Region thumb = new Region();
        thumb.getStyleClass().add("event-thumb");

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("body-text");
        title.setWrapText(true);

        StringBuilder meta = new StringBuilder(ROW_DATE.format(event.getStartTime()));
        if (event.getVenueName() != null) {
            meta.append(" · ").append(event.getVenueName());
        }
        Label when = new Label(meta.toString());
        when.getStyleClass().add("muted-text");

        VBox text = new VBox(title, when);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(16, thumb, text);
        row.getStyleClass().add("event-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label emptyMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private String initials(User user) {
        String first = user.getFirstName();
        String last = user.getLastName();
        String a = first == null || first.isBlank() ? "" : first.substring(0, 1);
        String b = last == null || last.isBlank() ? "" : last.substring(0, 1);
        return (a + b).toUpperCase();
    }

    @FXML
    protected void onSettingsClick() {
        Router.navigateTo("settings-view.fxml");
    }
}