package com.eventsphere.app;

import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
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
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

public class LandingPageController {

    @FXML private StackPane rootPane;
    @FXML private StackPane heroPane;
    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;

    @FXML private Label sectionTitle;
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
    private static final double HERO_H = 400.0;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM, h:mm a").withZone(ZoneId.systemDefault());

    private int state = 0;   // 0 closed, 1 map, 2 map + details

    private List<Event> heroEvents = List.of();
    private int heroIndex = 0;

    @FXML
    public void initialize() {
        drawer.prefWidthProperty().bind(rootPane.widthProperty());
        drawer.maxWidthProperty().bind(rootPane.widthProperty());

        detailsPanel.setTranslateX(-DETAILS_W);
        drawer.setTranslateX(2000);

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (state == 0) drawer.setTranslateX(newVal.doubleValue() - TAB_W);
        });

        heroImage.fitWidthProperty().bind(heroPane.widthProperty());

        loadHeroEvents();
        loadUpcomingEvents();
    }

    // ----- hero carousel -----
    private void loadHeroEvents() {
        try {
            List<Event> upcoming = new EventDAO(Database.DBConnect()).findUpcoming();
            heroEvents = upcoming.stream()
                    .sorted(Comparator.comparingInt(Event::getLikesCount).reversed())
                    .limit(3)
                    .toList();
            showHeroEvent(0);
        } catch (Exception e) {
            System.err.println("Could not load hero events: " + e.getMessage());
        }
    }

    private void showHeroEvent(int index) {
        if (heroEvents.isEmpty()) {
            return;
        }
        heroIndex = Math.floorMod(index, heroEvents.size());
        Event event = heroEvents.get(heroIndex);

        heroTitle.setText(event.getTitle().toUpperCase());
        heroBlurb.setText(event.getDescription() == null ? "" : event.getDescription());

        String url = resolveImageUrl(event.getImageUrl());
        heroImage.setImage(url == null
                ? null
                : new Image(url, 1600, HERO_H, false, true, true));
    }

    @FXML
    protected void onPrevHero() {
        showHeroEvent(heroIndex - 1);
    }

    @FXML
    protected void onNextHero() {
        showHeroEvent(heroIndex + 1);
    }

    // ----- event row and filters -----

    private void loadUpcomingEvents() {
        try {
            renderRow(new EventDAO(Database.DBConnect()).findUpcoming());
        } catch (Exception e) {
            System.err.println("Could not load events: " + e.getMessage());
            weekendRow.getChildren().setAll(emptyMessage("Events unavailable right now."));
        }
    }

    @FXML
    protected void onCategoryFilter(ActionEvent actionEvent) {
        Button clicked = (Button) actionEvent.getSource();
        String value = String.valueOf(clicked.getUserData());

        try {
            EventDAO dao = new EventDAO(Database.DBConnect());
            sectionTitle.setText(clicked.getText());

            if ("ALL".equals(value)) {
                renderRow(dao.findUpcoming());
            } else if ("WEEKEND".equals(value)) {
                renderRow(dao.search(null, null, weekendStart(), weekendEnd()));
            } else {
                renderRow(dao.findByCategory(Category.fromDbValue(value))
                        .stream()
                        .filter(event -> !event.hasOccurred())
                        .toList());
            }
        } catch (Exception e) {
            System.err.println("Filter failed: " + e.getMessage());
        }
    }

    private Instant weekendStart() {
        return ZonedDateTime.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
    }

    private Instant weekendEnd() {
        return ZonedDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
    }

    private void renderRow(List<Event> events) {
        weekendRow.getChildren().clear();
        if (events.isEmpty()) {
            weekendRow.getChildren().add(emptyMessage("Nothing on in this category yet."));
            return;
        }
        for (Event event : events) {
            weekendRow.getChildren().add(buildEventCard(event));
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
        String url = resolveImageUrl(event.getImageUrl());
        if (url == null) {
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

        Rectangle clip = new Rectangle(CARD_W, THUMB_H);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        view.setClip(clip);

        StackPane wrapper = new StackPane(view);
        wrapper.getStyleClass().add("event-card-thumb");
        wrapper.setPrefSize(CARD_W, THUMB_H);
        wrapper.setMinHeight(THUMB_H);
        return wrapper;
    }

    // Remote URLs pass through; anything else is treated as a file under images/.
    private String resolveImageUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (stored.startsWith("http://") || stored.startsWith("https://")) {
            return stored;
        }
        var resource = getClass().getResource("images/" + stored);
        return resource == null ? null : resource.toExternalForm();
    }

    private void onEventCardClick(Event event) {
        System.out.println("Clicked event " + event.getEventId() + ": " + event.getTitle());
    }

    // ----- map drawer -----

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
}