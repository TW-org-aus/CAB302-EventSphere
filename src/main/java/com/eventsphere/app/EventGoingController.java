package com.eventsphere.app;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.EventService;
import com.eventsphere.app.service.GoingService;
import com.eventsphere.app.service.MessagingService;
import com.eventsphere.app.service.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class EventGoingController {

    @FXML private Label eventTitleLabel;
    @FXML private Label goingCountLabel;
    @FXML private VBox attendeeList;

    private final EventService eventService;
    private final GoingService goingService;
    private final MessagingService messaging;
    private final SessionManager session;

    private int eventId;

    // Router's controller factory supplies these, same as EventPageController.
    public EventGoingController(EventService eventService, GoingService goingService, MessagingService messaging, SessionManager session) {
        this.eventService = eventService;
        this.goingService = goingService;
        this.messaging = messaging;
        this.session = session;
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
        // Ensures names only clickable if the user is going too
        int viewerId = session.getCurrentUser().map(User::getUserId).orElse(-1);
        boolean viewerGoing = attendees.stream().anyMatch(user -> user.getUserId() == viewerId);

        for (User user : attendees) {
            attendeeList.getChildren().add(buildRow(user, viewerGoing && user.getUserId() != viewerId));
        }
    }

    // Name only, no email, so the list doesn't expose contact details.
    private HBox buildRow(User user, boolean canMessage) {
        Label avatar = new Label(GoingService.initialsFor(user));
        avatar.getStyleClass().add("going-avatar");

        Label name = new Label(user.getFullName());
        if (canMessage) {
            name.getStyleClass().add("reply-link");
            name.setTooltip(new Tooltip("Message " + user.getFullName()));
            name.setOnMouseClicked(event -> openChatWith(user));
        } else {
            name.getStyleClass().add("body-text");
        }

        Button messageBtn = new Button("Message");
        messageBtn.setOnAction(event -> openChatWith(user));

        HBox row = new HBox(14, avatar, name, messageBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("going-row");
        return row;
    }

    private void openChatWith(User other) {
        session.getCurrentUser().ifPresentOrElse(me -> {
            var conv = messaging.openConversation(me.getUserId(), other.getUserId());
            System.out.println("openConversation result: " + conv);
            conv.ifPresentOrElse(conversation -> {
                System.out.println("Routing to messages-view");
                MessagesController chat = Router.navigateToWithController("messages-view.fxml");
                chat.setConversation(conversation, other);
            }, () -> System.out.println("openConversation returned empty — users may not share a going event"));
        }, () -> System.out.println("No logged-in user"));
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