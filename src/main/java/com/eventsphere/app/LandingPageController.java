package com.eventsphere.app;

import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.Connection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LandingPageController {

    @FXML private StackPane rootPane;

    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;
    @FXML private HBox weekendRow;

    @FXML private HBox drawer;
    @FXML private Button drawerTab;
    @FXML private FontIcon drawerTabIcon;
    @FXML private VBox detailsPanel;
    @FXML private StackPane mapPanel;

    private static final double TAB_W = 28.0;
    private static final double DETAILS_W = 420.0;
    private static final double CARD_W = 280.0;
    private static final double THUMB_H = 150.0;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM, h:mm a").withZone(ZoneId.systemDefault());

    private int state = 0;   // 0 closed, 1 map, 2 map + details

    @FXML
    public void initialize() {
        drawer.prefWidthProperty().bind(rootPane.widthProperty());
        drawer.maxWidthProperty().bind(rootPane.widthProperty());

        detailsPanel.setTranslateX(-DETAILS_W);
        drawer.setTranslateX(2000);

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (state == 0) drawer.setTranslateX(newVal.doubleValue() - TAB_W);
        });

        seedTestEvents();
        loadUpcomingEvents();
    }

    // TEMPORARY: local test data so the page has something to show.
    // Remove once the team has shared seed data.
    private void seedTestEvents() {
        try {
            Connection conn = Database.DBConnect();
            EventDAO eventDao = new EventDAO(conn);

            if (!eventDao.findAll().isEmpty()) {
                return;
            }

            int sourceId = new SourceDAO(conn).insert("Manual", "https://eventsphere.local");

            eventDao.insert(new Event(
                    "Boiler Room Brisbane", "Warehouse party in the Valley", Category.NIGHTLIFE,
                    Instant.now().plus(3, ChronoUnit.DAYS), null,
                    "The Zoo", "711 Ann St, Fortitude Valley",
                    -27.4573, 153.0345, null, null, sourceId));

            eventDao.insert(new Event(
                    "Sunrise 5k", "River run, coffee after for everyone who turns up", Category.COMMUNITY,
                    Instant.now().plus(5, ChronoUnit.DAYS), null,
                    "Riverwalk", "New Farm",
                    -27.4679, 153.0459, null, null, sourceId));

            eventDao.insert(new Event(
                    "South Bank Night Market", "Food stalls and live music along the river", Category.FOOD_DRINK,
                    Instant.now().plus(8, ChronoUnit.DAYS), null,
                    "South Bank Parklands", "Stanley St Plaza, South Brisbane",
                    -27.4809, 153.0176, null, null, sourceId));

            eventDao.insert(new Event(
                    "Trivia Night", "Teams of four, first round is free", Category.COMMUNITY,
                    Instant.now().plus(11, ChronoUnit.DAYS), null,
                    "The Grey Fox", "West End",
                    -27.4820, 153.0090, null, null, sourceId));

            eventDao.insert(new Event(
                    "Jazz at the Powerhouse", "Local quartet, doors at seven", Category.MUSIC,
                    Instant.now().plus(14, ChronoUnit.DAYS), null,
                    "Brisbane Powerhouse", "119 Lamington St, New Farm",
                    -27.4665, 153.0490, null, null, sourceId));

            System.out.println("Seeded 5 test events.");
        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
        }
    }

    private void loadUpcomingEvents() {
        try {
            List<Event> events = new EventDAO(Database.DBConnect()).findUpcoming();
            if (events.isEmpty()) {
                weekendRow.getChildren().add(emptyMessage("No upcoming events yet."));
                return;
            }
            for (Event event : events) {
                weekendRow.getChildren().add(buildEventCard(event));
            }
        } catch (Exception e) {
            System.err.println("Could not load events: " + e.getMessage());
            weekendRow.getChildren().add(emptyMessage("Events unavailable right now."));
        }
    }

    private Label emptyMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private VBox buildEventCard(Event event) {
        VBox card = new VBox(8.0);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);

        card.getChildren().add(buildThumb(event));

        Label when = new Label(DATE_FORMAT.format(event.getStartTime()));
        when.getStyleClass().add("muted-text");

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        title.setMaxWidth(CARD_W);

        card.getChildren().addAll(when, title);

        if (event.getVenueName() != null) {
            Label venue = new Label(event.getVenueName());
            venue.getStyleClass().add("muted-text");
            card.getChildren().add(venue);
        }

        card.setOnMouseClicked(e -> onEventCardClick(event));
        return card;
    }

    private Node buildThumb(Event event) {
        String url = event.getImageUrl();
        if (url == null || url.isBlank()) {
            Region placeholder = new Region();
            placeholder.getStyleClass().add("event-card-thumb");
            placeholder.setPrefSize(CARD_W, THUMB_H);
            placeholder.setMinHeight(THUMB_H);
            return placeholder;
        }

        ImageView view = new ImageView(new Image(url, CARD_W, THUMB_H, false, true, true));
        view.setFitWidth(CARD_W);
        view.setFitHeight(THUMB_H);
        view.setPreserveRatio(false);

        StackPane wrapper = new StackPane(view);
        wrapper.getStyleClass().add("event-card-thumb");
        wrapper.setPrefSize(CARD_W, THUMB_H);
        wrapper.setMinHeight(THUMB_H);
        return wrapper;
    }

    private void onEventCardClick(Event event) {
        System.out.println("Clicked event " + event.getEventId() + ": " + event.getTitle());
    }

    private void slide(Node node, double x) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), node);
        tt.setToX(x);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    protected void onMoreInfoClick() {
        Router.navigateTo("EventPage.fxml");
    }

    @FXML
    protected void onToggleDrawer() {
        if (state == 0) {
            state = 1;
            drawerTabIcon.setIconLiteral("bi-chevron-right");
            slide(drawer, 0);
        } else {
            state = 0;
            drawerTabIcon.setIconLiteral("bi-chevron-left");
            slide(drawer, rootPane.getWidth() - TAB_W);
            slide(detailsPanel, -DETAILS_W);
        }
    }

    public void showEventDetails() {
        state = 2;
        drawerTabIcon.setIconLiteral("bi-chevron-right");
        slide(detailsPanel, TAB_W);
    }

    @FXML
    protected void onPrevHero() {
        System.out.println("prev");
    }

    @FXML
    protected void onNextHero() {
        System.out.println("next");
    }
}