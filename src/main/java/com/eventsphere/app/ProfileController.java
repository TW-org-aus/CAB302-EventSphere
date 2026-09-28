package com.eventsphere.app;

import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import javafx.geometry.Pos;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ProfileController {

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label friendsCount;
    @FXML private Label goingCount;
    @FXML private Button goingTab;
    @FXML private Button beenTab;
    @FXML private VBox eventList;

    private final SessionManager session;
    private final IGoingDAO going;

    public ProfileController(SessionManager session, IGoingDAO going) {
        this.session = session;
        this.going = going;
    }

    @FXML
    protected void onEditProfileClick() {
        System.out.println("Edit profile clicked");
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

    @FXML
    protected void onSettingsClick() {
        Router.navigateTo("settings-view.fxml");
    }

    private static final DateTimeFormatter ROW_DATE =
            DateTimeFormatter.ofPattern("EEE HH:mm").withZone(ZoneId.systemDefault());

    private List<Event> goingEvents = List.of();
    private List<Event> beenEvents = List.of();

    @FXML
    public void initialize() {
        User user = session.getCurrentUser().orElse(null);
        if (user == null) {
            nameLabel.setText("Not signed in");
            subtitleLabel.setText("Log in to see your events");
            avatarLabel.setText("?");
            eventList.getChildren().add(emptyMessage("Log in to see your events."));
            return;
        }

        nameLabel.setText(user.getFullName());
        subtitleLabel.setText(user.getEmail());
        avatarLabel.setText(initials(user));

        List<Event> all = going.findEventsForUser(user.getUserId());
        Instant now = Instant.now();
        goingEvents = all.stream().filter(e -> e.getStartTime().isAfter(now)).toList();
        beenEvents = all.stream().filter(e -> !e.getStartTime().isAfter(now)).toList();

        goingCount.setText(String.valueOf(goingEvents.size()));
        friendsCount.setText("0");

        showEvents(goingEvents);
    }

    private String initials(User user) {
        String first = user.getFirstName();
        String last = user.getLastName();
        String a = first == null || first.isBlank() ? "" : first.substring(0, 1);
        String b = last == null || last.isBlank() ? "" : last.substring(0, 1);
        return (a + b).toUpperCase();
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
}
