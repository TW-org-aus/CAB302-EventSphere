package com.eventsphere.app;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.service.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EventPageController {

    private static final double IMAGE_W = 464.0;
    private static final double IMAGE_H = 180.0;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    @FXML private VBox eventFieldsBox;
    @FXML private ImageView eventImage;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private HBox venueRow;
    @FXML private Label venueLabel;
    @FXML private Label notFoundLabel;
    @FXML private Label commentsCountLabel;

    @FXML
    private TextField commentInput;

    private final EventService eventService;

    // Router's controller factory supplies this. There is deliberately no no-arg
    // constructor, so the controller cannot reach for a database on its own.
    public EventPageController(EventService eventService) {
        this.eventService = eventService;
    }

    // Called by Router right after it loads this FXML, once the caller knows which event
    // id it navigated here with. Loads synchronously: there is no background thread to wait on.
    public void showEvent(int eventId) {
        try {
            Event found = eventService.findById(eventId); if (found == null) {
                showNotFound();
                return;
            }
            fillEvent(found);

        } catch (Exception e) {
            System.err.println("Could not load event " + eventId + ": " + e.getMessage());
            showNotFound();
        }
    }

    private void fillEvent(Event event) {
        eventFieldsBox.setVisible(true);
        eventFieldsBox.setManaged(true);
        notFoundLabel.setVisible(false);
        notFoundLabel.setManaged(false);

        String url = EventImages.resolveImageUrlWithPlaceholder(event);
        eventImage.setImage(url == null ? null : new Image(url, IMAGE_W, IMAGE_H, false, true, true));

        titleLabel.setText(event.getTitle());

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            descriptionLabel.setVisible(false);
            descriptionLabel.setManaged(false);
        } else {
            descriptionLabel.setText(event.getDescription());
            descriptionLabel.setVisible(true);
            descriptionLabel.setManaged(true);
        }

        dateLabel.setText(DATE_FORMAT.format(event.getStartTime()));
        timeLabel.setText(event.getEndTime() == null
                ? TIME_FORMAT.format(event.getStartTime())
                : TIME_FORMAT.format(event.getStartTime()) + " – " + TIME_FORMAT.format(event.getEndTime()));

        String venue = joinVenue(event);
        if (venue == null) {
            venueRow.setVisible(false);
            venueRow.setManaged(false);
        } else {
            venueLabel.setText(venue);
            venueRow.setVisible(true);
            venueRow.setManaged(true);
        }

        commentsCountLabel.setText(String.valueOf(event.getCommentsCount()));
    }

    // Joins the venue name and address with ", ", leaving out whichever side is missing.
    private String joinVenue(Event event) {
        if (event.getVenueName() != null && event.getAddress() != null) {
            return event.getVenueName() + ", " + event.getAddress();
        }
        if (event.getVenueName() != null) {
            return event.getVenueName();
        }
        return event.getAddress();
    }

    private void showNotFound() {
        eventFieldsBox.setVisible(false);
        eventFieldsBox.setManaged(false);
        notFoundLabel.setText("Event not found.");
        notFoundLabel.setVisible(true);
        notFoundLabel.setManaged(true);
    }

    @FXML
    protected void onBackClick() {
        Router.navigateTo("landing-page.fxml");
    }

    @FXML
    protected void onFilterTopClick() {
        System.out.println("Filter comments: Top");
    }

    @FXML
    protected void onFilterLatestClick() {
        System.out.println("Filter comments: Latest");
    }

    @FXML
    protected void onFilterHostRepliesClick() {
        System.out.println("Filter comments: Host Replies");
    }

    @FXML
    protected void onSortTopClick() {
        System.out.println("Sort comments: Top");
    }

    @FXML
    protected void onSortLatestClick() {
        System.out.println("Sort comments: Latest");
    }

    @FXML
    protected void onSortHostRepliesClick() {
        System.out.println("Sort comments: Host Replies");
    }

    @FXML
    protected void onPostCommentClick() {
        System.out.println("Post comment clicked: " + commentInput.getText());
    }
}
