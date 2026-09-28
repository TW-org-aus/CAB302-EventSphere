package com.eventsphere.app;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.DateRange;
import com.eventsphere.app.service.EventService;
import com.eventsphere.app.service.SessionManager;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class LandingPageController {

    @FXML private StackPane rootPane;
    @FXML private StackPane heroPane;
    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;

    @FXML private Label sectionTitle;
    @FXML private HBox weekendRow;

    @FXML private ComboBox<DateRange> dateFilter;
    @FXML private ComboBox<Double> distanceFilter;

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
    private static final int HERO_COUNT = 3;
    private static final Double ANY_DISTANCE = 0.0;
    private static final String TAB_RIGHT =
            "-fx-background-radius: 8 0 0 8; -fx-background-color: #dddddd;";
    private static final String TAB_LEFT =
            "-fx-background-radius: 0 8 8 0; -fx-background-color: #dddddd;";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM, h:mm a").withZone(ZoneId.systemDefault());

    private int state = 0;   // 0 closed, 1 map, 2 map + details

    private List<Event> heroEvents = List.of();
    private int heroIndex = 0;
    private Event selectedEvent;

    private final EventService eventService;
    private final SessionManager session;

    private Category selectedCategory;       // null = all categories
    private boolean filtersReady = false;    // stops setValue() in initFilters firing queries

    // Router's controller factory supplies these. There is deliberately no no-arg
    // constructor, so the controller cannot reach for a database on its own.
    public LandingPageController(EventService eventService, SessionManager session) {
        this.eventService = eventService;
        this.session = session;
    }

    @FXML
    public void initialize() {
        initFilters();

        drawer.prefWidthProperty().bind(rootPane.widthProperty());
        drawer.maxWidthProperty().bind(rootPane.widthProperty());

        detailsPanel.setTranslateX(-DETAILS_W);
        drawer.setTranslateX(2000);

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (state == 0) drawer.setTranslateX(newVal.doubleValue());
        });

        heroImage.fitWidthProperty().bind(heroPane.widthProperty());

        detailsPanel.setCursor(Cursor.HAND);
        detailsPanel.setOnMouseClicked(e -> {
            if (selectedEvent != null) {
                openEvent(selectedEvent);
            }
        });

        initMap();
        loadHeroEvents();
        loadUpcomingEvents();
    }

    // ----- hero carousel -----

    private void loadHeroEvents() {
        try {
            heroEvents = eventService.topByLikes(HERO_COUNT);
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

        String url = EventImages.resolveImageUrlWithPlaceholder(event);
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

    // ----- search -----

    // Called by NavBarController after it navigates here with a search term.
    public void showSearchResults(String keyword) {
        try {
            sectionTitle.setText("Results for \"" + keyword + "\"");
            List<Event> results = eventService.search(keyword);
            renderRow(results);
            if (results.isEmpty()) {
                weekendRow.getChildren().setAll(emptyMessage("No events match \"" + keyword + "\"."));
            }
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            weekendRow.getChildren().setAll(emptyMessage("Search unavailable right now."));
        }
    }

    // ----- event row and filters -----

    private void loadUpcomingEvents() {
        try {
            renderRow(eventService.findUpcoming());
        } catch (Exception e) {
            System.err.println("Could not load events: " + e.getMessage());
            weekendRow.getChildren().setAll(emptyMessage("Events unavailable right now."));
        }
    }

    private void initFilters() {
        dateFilter.getItems().setAll(DateRange.values());
        dateFilter.setValue(DateRange.ANY);

        distanceFilter.getItems().setAll(ANY_DISTANCE, 5.0, 10.0, 25.0, 50.0);
        distanceFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double km) {
                return km == null || km <= 0 ? "Any distance" : "Within " + km.intValue() + " km";
            }

            @Override
            public Double fromString(String text) {
                return null;   // not editable
            }
        });
        distanceFilter.setValue(ANY_DISTANCE);

        // Distance needs a home location, which only logged-in users with an address have.
        distanceFilter.setDisable(homeUser() == null);

        filtersReady = true;
    }

    @FXML
    protected void onCategoryFilter(ActionEvent actionEvent) {
        Button clicked = (Button) actionEvent.getSource();
        String value = String.valueOf(clicked.getUserData());

        filtersReady = false;
        if (EventService.FILTER_ALL.equals(value)) {
            selectedCategory = null;
            dateFilter.setValue(DateRange.ANY);
            distanceFilter.setValue(ANY_DISTANCE);
        } else if (EventService.FILTER_WEEKEND.equals(value)) {
            selectedCategory = null;
            dateFilter.setValue(DateRange.THIS_WEEKEND);
        } else {
            selectedCategory = Category.fromDbValue(value);
        }
        filtersReady = true;

        sectionTitle.setText(clicked.getText());
        applyFilters();
    }

    @FXML
    protected void onFiltersChanged() {
        applyFilters();
    }

    private void applyFilters() {
        if (!filtersReady) {
            return;
        }
        Double km = distanceFilter.getValue();
        Double radius = (km == null || km <= 0) ? null : km;
        User home = homeUser();

        try {
            List<Event> results = eventService.filter(selectedCategory, dateFilter.getValue(), radius,
                    home == null ? null : home.getHomeLat(),
                    home == null ? null : home.getHomeLong());
            renderRow(results);
            if (results.isEmpty()) {
                weekendRow.getChildren().setAll(emptyMessage("No events match these filters."));
            }
        } catch (Exception e) {
            System.err.println("Filter failed: " + e.getMessage());
            weekendRow.getChildren().setAll(emptyMessage("Events unavailable right now."));
        }
    }

    // The logged-in user, only if they have a home location saved.
    private User homeUser() {
        return session.getCurrentUser()
                .filter(user -> user.getHomeLat() != null && user.getHomeLong() != null)
                .orElse(null);
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

        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> onEventCardClick(event));
        return card;
    }

    private Node buildThumb(Event event) {
        return buildThumb(event, CARD_W, THUMB_H);
    }

    private Node buildThumb(Event event, double width, double height) {
        String url = EventImages.resolveImageUrlWithPlaceholder(event);

        // old logic for the legacy image url resolver class no longer needed feel free to delete if you want
        /*
        if (url == null) {
            Region placeholder = new Region();
            placeholder.getStyleClass().add("event-card-thumb");
            placeholder.setPrefSize(width, height);
            placeholder.setMinHeight(height);
            return placeholder;
        }*/

        ImageView view = new ImageView(new Image(url, width, height, false, true, true));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        view.setClip(clip);

        StackPane wrapper = new StackPane(view);
        wrapper.getStyleClass().add("event-card-thumb");
        wrapper.setPrefSize(width, height);
        wrapper.setMinHeight(height);
        return wrapper;
    }

    private void onEventCardClick(Event event) {
        openEvent(event);
    }

    // Opens the full event page for the given event, passing its id across the navigation.
    private void openEvent(Event event) {
        EventPageController page = Router.navigateToWithController("EventPage.fxml");
        page.showEvent(event.getEventId());
    }

    // ----- map drawer -----

    private void initMap() {
        MapView mapView = new MapView();
        mapView.setZoom(11);
        mapView.flyTo(0, new MapPoint(-27.4698, 153.0251), 0.1);

        EventMapLayer layer = new EventMapLayer(this::onPinClick);
        mapView.addLayer(layer);
        mapPanel.getChildren().setAll(mapView);

        try {
            layer.setEvents(eventService.findUpcoming());
        } catch (Exception e) {
            System.err.println("Could not load map pins: " + e.getMessage());
        }
    }

    private void onPinClick(Event event) {
        selectedEvent = event;
        showEventSummary(event);
        showEventDetails();
    }

    private void showEventSummary(Event event) {
        detailsPanel.getChildren().clear();

        FontIcon closeIcon = new FontIcon("bi-x");
        closeIcon.setIconSize(22);

        Button close = new Button();
        close.setGraphic(closeIcon);
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> hideEventDetails());
        // I dont know if this a design flaw on my part or not but I wanted to open event details
        // by the user clicking on the event because it was mor intuative, However after getting
        //it working on the landing page I realised that the
        // detailsPanel opens the event on click and therefore the close button must not trigger
        // that, so its click is consumed before it bubbles up to the panel.
        //will need to talk to kai about how to resolve this cause I dont want to delete his work, kai if
        // your reading this just let me know what you want me to do.
        close.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        HBox topRow = new HBox(close);
        topRow.setAlignment(Pos.CENTER_RIGHT);
        detailsPanel.getChildren().add(topRow);

        Node thumb = buildThumb(event, 372, 200);

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Label when = new Label(DATE_FORMAT.format(event.getStartTime()));
        when.getStyleClass().add("muted-text");

        detailsPanel.getChildren().addAll(thumb, title, when);

        if (event.getVenueName() != null) {
            Label venue = new Label(event.getVenueName());
            venue.getStyleClass().add("body-text");
            detailsPanel.getChildren().add(venue);
        }

        if (event.getAddress() != null) {
            Label address = new Label(event.getAddress());
            address.getStyleClass().add("muted-text");
            address.setWrapText(true);
            detailsPanel.getChildren().add(address);
        }

        if (event.getDescription() != null) {
            Label blurb = new Label(event.getDescription());
            blurb.getStyleClass().add("body-text");
            blurb.setWrapText(true);
            detailsPanel.getChildren().add(blurb);
        }

        Button moreInfo = new Button("MORE INFO");
        moreInfo.getStyleClass().add("primary-button");
        moreInfo.setMaxWidth(Double.MAX_VALUE);
        moreInfo.setOnAction(e -> openEvent(event));
        // Avoid navigating twice: the panel's own click handler would otherwise also fire.
        moreInfo.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        detailsPanel.getChildren().add(moreInfo);
    }

    private void slide(Node node, double x) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), node);
        tt.setToX(x);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    protected void onMoreInfoClick() {
        if (heroEvents.isEmpty()) {
            return;
        }
        openEvent(heroEvents.get(heroIndex));
    }

    @FXML
    protected void onToggleDrawer() {
        if (state == 0) {
            state = 1;
            drawerTabIcon.setIconLiteral("bi-chevron-right");
            drawerTab.setStyle(TAB_LEFT);
            slide(drawer, 0);
            slide(drawerTab, -(rootPane.getWidth() - TAB_W));
        } else {
            state = 0;
            drawerTabIcon.setIconLiteral("bi-chevron-left");
            drawerTab.setStyle(TAB_RIGHT);
            slide(drawer, rootPane.getWidth());
            slide(detailsPanel, -DETAILS_W);
            slide(drawerTab, 0);
        }
    }

    public void showEventDetails() {
        state = 2;
        drawerTabIcon.setIconLiteral("bi-chevron-right");
        slide(detailsPanel, TAB_W);
    }

    public void hideEventDetails() {
        state = 1;
        slide(detailsPanel, -DETAILS_W);
    }
}