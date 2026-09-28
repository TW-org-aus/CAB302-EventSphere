package com.eventsphere.app;

import java.util.List;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.EventService;
import com.eventsphere.app.service.GoingService;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EventGoingController {

    @FXML private Label eventTitleLabel;
    @FXML private Label goingCountLabel;
    @FXML private VBox attendeeList;

    private final EventService eventService;
    private final GoingService goingService;

    private int eventId;

    // Router's controller factory supplies these, same as EventPageController.
    public EventGoingController(EventService eventService, GoingService goingService) {
        this.eventService = eventService;
        this.goingService = goingService;
    }

    // Called by EventPageController right after navigating here.
    public void showEvent(int eventId) {
        this.eventId = eventId;

        try {
            Event event = eventService.findById(eventId);
            eventTitleLabel.setText(event == null ? "Event not found" : event.getTitle());
        } catch (Exception e) {
            System.err.println("Could not load event " + eventId + ": " + e.getMessage());
            eventTitleLabel.setText("Event unavailable");
        }

        try {
            renderAttendees(goingService.attendeesFor(eventId));
        } catch (Exception e) {
            System.err.println("Could not load attendees for event " + eventId + ": " + e.getMessage());
            goingCountLabel.setText("");
            attendeeList.getChildren().setAll(muted("Attendee list unavailable right now."));
        }
    }

    private void renderAttendees(List<User> attendees) {
        goingCountLabel.setText(GoingService.headingFor(attendees.size()));
        attendeeList.getChildren().clear();

        if (attendees.isEmpty()) {
            attendeeList.getChildren().add(muted("Be the first to mark yourself as going."));
            return;
        }
        for (User user : attendees) {
            attendeeList.getChildren().add(buildRow(user));
        }
    }

    // Name only, no email, so the list doesn't expose contact details.
    private HBox buildRow(User user) {
        Label avatar = new Label(GoingService.initialsFor(user));
        avatar.getStyleClass().add("going-avatar");

        Label name = new Label(user.getFullName());
        name.getStyleClass().add("body-text");

        HBox row = new HBox(14, avatar, name);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("going-row");
        return row;
    }

    private Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    @FXML
    protected void onBackClick() {
        EventPageController page = Router.navigateToWithController("EventPage.fxml");
        page.showEvent(eventId);
    }
}