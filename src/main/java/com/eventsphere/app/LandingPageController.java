package com.eventsphere.app;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.eventsphere.app.Database.Database;
import com.eventsphere.app.ai.CandidateSelector;
import com.eventsphere.app.ai.EventRecommender;
import com.eventsphere.app.ai.InterestProfile;
import com.eventsphere.app.ai.InterestProfileService;
import com.eventsphere.app.ai.Recommendation;
import com.eventsphere.app.ai.RecommendationCache;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.DateRange;
import com.eventsphere.app.service.EventService;
import com.eventsphere.app.service.SessionManager;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.StringConverter;

/** JavaFX controller for the landing page: event feed, filters, hero carousel, map drawer and recommendations row. */
public class LandingPageController {

    @FXML private StackPane rootPane;
    @FXML private StackPane heroPane;
    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;

    @FXML private VBox sectionsBox;

    @FXML private ComboBox<DateRange> dateFilter;
    @FXML private ComboBox<Double> distanceFilter;

    @FXML private HBox drawer;
    @FXML private Button drawerTab;
    @FXML private FontIcon drawerTabIcon;
    @FXML private VBox detailsPanel;
    @FXML private StackPane mapPanel;

    private static final double TAB_W = 56.0;
    private static final int BLURB_MAX = 150;
    private static final double DETAILS_W = 420.0;
    private static final double CARD_W = 280.0;
    private static final double THUMB_H = 150.0;
    private static final double HERO_H = 400.0;
    private static final int HERO_COUNT = 3;
    private static final int MIN_PER_ROW = 4;
    private static final int CARDS_PER_ROW = 12;
    private static final Double ANY_DISTANCE = 0.0;
    private static final double DEFAULT_LAT = -27.4698;
    private static final double DEFAULT_LNG = 153.0251;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM, h:mm a").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ROW_WEEKDAY =
            DateTimeFormatter.ofPattern("EEE").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ROW_DAY =
            DateTimeFormatter.ofPattern("d").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ROW_MONTH =
            DateTimeFormatter.ofPattern("MMM").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter ROW_TIME =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    private int state = 0;   // 0 closed, 1 map, 2 map + details

    private List<Event> heroEvents = List.of();
    private int heroIndex = 0;
    private Event selectedEvent;
    private MapView mapView;

    private final EventService eventService;
    private final InterestProfileService interestProfiles;
    private final CandidateSelector candidates;
    private final EventRecommender recommender;
    private final RecommendationCache recommendationCache;
    private final SessionManager session;

    private Category selectedCategory;       // null = explore mode
    private boolean filtersReady = false;    // stops setValue() in initFilters firing queries
    private int recommendationRequest = 0;   // discards a ranking superseded by a newer one

    public LandingPageController(EventService eventService, SessionManager session,
                                 InterestProfileService interestProfiles,
                                 CandidateSelector candidates, EventRecommender recommender,
                                 RecommendationCache recommendationCache) {
        this.eventService = eventService;
        this.session = session;
        this.interestProfiles = interestProfiles;
        this.candidates = candidates;
        this.recommender = recommender;
        this.recommendationCache = recommendationCache;
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
        applyFilters();
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
        heroBlurb.setText(shortBlurb(event.getDescription()));

        String url = EventImages.resolveImageUrlWithPlaceholder(event);
        heroImage.setImage(url == null
                ? null
                : new Image(url, 1600, HERO_H, false, true, true));
    }

    // Descriptions run to paragraphs; the hero has room for about two lines.
    private static String shortBlurb(String text) {
        if (text == null) {
            return "";
        }
        String oneLine = text.replaceAll("\\s+", " ").trim();
        if (oneLine.length() <= BLURB_MAX) {
            return oneLine;
        }
        int cut = oneLine.lastIndexOf(' ', BLURB_MAX);
        return oneLine.substring(0, cut < 40 ? BLURB_MAX : cut) + "…";
    }

    @FXML
    protected void onPrevHero() {
        showHeroEvent(heroIndex - 1);
    }

    @FXML
    protected void onNextHero() {
        showHeroEvent(heroIndex + 1);
    }

    // ----- rendering -----

    // Personalisation reorders the rows; it never hides a category.
    private void renderExplore(List<Event> events) {
        sectionsBox.getChildren().clear();

        Map<Category, List<Event>> byCategory = groupByCategory(events, MIN_PER_ROW);
        if (byCategory.isEmpty()) {
            sectionsBox.getChildren().add(emptyMessage("No events match these filters."));
            return;
        }

        for (Category category : orderCategories(byCategory)) {
            sectionsBox.getChildren().add(buildSection(
                    category.getDbValue(),
                    byCategory.get(category),
                    () -> selectCategory(category)));
        }

        loadRecommendationsAsync();
    }

    // A grid rather than a strip, so the whole category is reachable by scrolling down.
    private void renderSingle(String title, List<Event> events) {
        sectionsBox.getChildren().clear();

        Label heading = new Label(title);
        heading.getStyleClass().add("section-heading-large");
        sectionsBox.getChildren().add(heading);

        if (events.isEmpty()) {
            sectionsBox.getChildren().add(emptyMessage("Nothing on here yet."));
            return;
        }

        FlowPane grid = new FlowPane(16, 16);
        grid.setMinWidth(0);
        grid.setPrefWrapLength(0);
        grid.setAlignment(Pos.CENTER);
        events.forEach(event -> grid.getChildren().add(buildEventCard(event)));
        sectionsBox.getChildren().add(grid);
    }

    // Every category still gets a row, so a user whose tastes miss the data loses nothing.
    private List<Category> orderCategories(Map<Category, List<Event>> byCategory) {
        List<Category> ordered = new ArrayList<>();

        session.getCurrentUser().ifPresent(user -> {
            for (Category category : interestProfiles.buildFor(user).rankedCategories(5)) {
                if (byCategory.containsKey(category)) {
                    ordered.add(category);
                }
            }
        });

        byCategory.entrySet().stream()
                .filter(entry -> !ordered.contains(entry.getKey()))
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .map(Map.Entry::getKey)
                .forEach(ordered::add);

        return ordered;
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        applyFilters();
    }

    // Uncategorised events are skipped and not bucketed: "Other" is a real category, not a missing value.
    private Map<Category, List<Event>> groupByCategory(List<Event> events, int minPerRow) {
        Map<Category, List<Event>> byCategory = new EnumMap<>(Category.class);
        for (Event event : events) {
            if (event.getCategory() != null) {
                byCategory.computeIfAbsent(event.getCategory(), k -> new ArrayList<>()).add(event);
            }
        }
        byCategory.values().removeIf(list -> list.size() < minPerRow);
        return byCategory;
    }

    // Ticketmaster stores one row per performance, so a run fills a whole strip with the
    // same show. Keyed on venue too, so a tour keeps an entry per city.
    private static List<Event> dedupeByTitleAndVenue(List<Event> events) {
        Map<String, Event> soonest = new LinkedHashMap<>();
        for (Event event : events) {
            String key = event.getTitle() + "|" + event.getVenueName();
            soonest.merge(key, event, (existing, candidate) ->
                    candidate.getStartTime().isBefore(existing.getStartTime()) ? candidate : existing);
        }
        return List.copyOf(soonest.values());
    }

    // The heading and the "View all" link both open the section, so either is clickable.
    private VBox buildSection(String title, List<Event> events, Runnable onViewAll) {
        Label heading = new Label(title);
        heading.getStyleClass().add("section-heading-large");

        VBox section = new VBox(16);
        section.setMinWidth(0);

        if (onViewAll == null) {
            section.getChildren().add(heading);
        } else {
            heading.setCursor(Cursor.HAND);
            heading.setOnMouseClicked(e -> onViewAll.run());

            Button viewAll = new Button("View all →");
            viewAll.getStyleClass().add("view-all-link");
            viewAll.setOnAction(e -> onViewAll.run());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox header = new HBox(heading, spacer, viewAll);
            header.setAlignment(Pos.CENTER_LEFT);
            section.getChildren().add(header);
        }

        HBox cards = new HBox(16);
        events.stream().limit(CARDS_PER_ROW)
                .forEach(event -> cards.getChildren().add(buildEventCard(event)));

        ScrollPane scroller = new ScrollPane(cards);
        scroller.setFitToHeight(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setMinWidth(0);
        scroller.setStyle("-fx-background-color: transparent;");

        section.getChildren().add(scroller);
        return section;
    }

    // ----- AI recommendations -----

    // Ranking is an API call, so it runs off the FX thread behind a placeholder. A cached
    // ranking skips that, which is what makes filter changes and revisits free.
    private void loadRecommendationsAsync() {
        User user = session.getCurrentUser().orElse(null);
        if (user == null || recommender == null) {
            return;   // logged out, or no API key: no row at all
        }

        final int request = ++recommendationRequest;

        // Local database reads, and what the cache is keyed on.
        InterestProfile profile = profileFor(user);
        if (profile == null) {
            return;
        }

        List<Recommendation> cached = recommendationCache.get(user.getUserId(), profile);
        if (cached != null) {
            showRecommendations(cached, request);
            return;
        }

        Node loading = buildRecommendationsLoading();
        sectionsBox.getChildren().add(0, loading);

        Task<List<Recommendation>> task = new Task<>() {
            @Override
            protected List<Recommendation> call() {
                List<Recommendation> ranked =
                        recommender.recommend(profile, candidates.candidatesFor(profile));
                // Cached even if the user has moved on, so the call is not wasted.
                recommendationCache.put(user.getUserId(), profile, ranked);
                return ranked;
            }
        };
        task.setOnSucceeded(e -> {
            sectionsBox.getChildren().remove(loading);
            showRecommendations(task.getValue(), request);
        });
        task.setOnFailed(e -> {
            System.err.println("Could not rank recommendations: " + task.getException());
            sectionsBox.getChildren().remove(loading);
        });

        Thread worker = new Thread(task, "recommendations");
        worker.setDaemon(true);
        worker.start();
    }

    // A user who has since picked a category should not have it pushed down by a late row.
    private void showRecommendations(List<Recommendation> results, int request) {
        if (request != recommendationRequest || selectedCategory != null || results.isEmpty()) {
            return;
        }
        List<Event> events = results.stream().map(Recommendation::event).toList();
        sectionsBox.getChildren().add(0, buildSection("Recommended for you", events, null));
    }

    // Holds the row's place, so it does not appear from nowhere seconds later.
    private Node buildRecommendationsLoading() {
        Label heading = new Label("Recommended for you");
        heading.getStyleClass().add("section-heading-large");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(18, 18);

        Label message = new Label("Finding events for you…");
        message.getStyleClass().add("muted-text");

        HBox status = new HBox(10, spinner, message);
        status.setAlignment(Pos.CENTER_LEFT);

        VBox section = new VBox(16, heading, status);
        section.setMinWidth(0);
        return section;
    }

    // A profile failure must not take the category rows down with it.
    private InterestProfile profileFor(User user) {
        try {
            return interestProfiles.buildFor(user);
        } catch (Exception e) {
            System.err.println("Could not build interest profile: " + e.getMessage());
            return null;
        }
    }

    // ----- filters and search -----

    private void applyFilters() {
        if (!filtersReady) {
            return;
        }
        Double km = distanceFilter.getValue();
        Double radius = (km == null || km <= 0) ? null : km;
        User home = homeUser();

        try {
            List<Event> results = dedupeByTitleAndVenue(
                    eventService.filter(selectedCategory, dateFilter.getValue(), radius,
                            home == null ? null : home.getHomeLat(),
                            home == null ? null : home.getHomeLong()));
            if (selectedCategory == null) {
                renderExplore(results);
            } else {
                renderSingle(selectedCategory.getDbValue(), results);
            }
        } catch (Exception e) {
            System.err.println("Filter failed: " + e.getMessage());
            sectionsBox.getChildren().setAll(emptyMessage("Events unavailable right now."));
        }
    }

    // Called by NavBarController after it navigates here with a search term.
    public void showSearchResults(String keyword) {
        try {
            renderSingle("Results for \"" + keyword + "\"",
                    dedupeByTitleAndVenue(eventService.search(keyword)));
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            sectionsBox.getChildren().setAll(emptyMessage("Search unavailable right now."));
        }
    }

    private void initFilters() {
        dateFilter.getItems().setAll(DateRange.values());
        dateFilter.setValue(DateRange.ANY);
        dateFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(DateRange range) {
                if (range == null || range == DateRange.ANY) {
                    return "ANY TIME";
                }
                return range.name().replace('_', ' ');
            }

            @Override
            public DateRange fromString(String text) {
                return null;   // not editable
            }
        });

        distanceFilter.getItems().setAll(ANY_DISTANCE, 5.0, 10.0, 25.0, 50.0);
        distanceFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double km) {
                return km == null || km <= 0 ? "ANY DISTANCE" : "WITHIN " + km.intValue() + " KM";
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

        applyFilters();
    }

    @FXML
    protected void onFiltersChanged() {
        applyFilters();
    }

    private User homeUser() {
        return session.getCurrentUser()
                .filter(user -> user.getHomeLat() != null && user.getHomeLong() != null)
                .orElse(null);
    }

    // ----- cards -----

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

    private void openEvent(Event event) {
        EventPageController page = Router.navigateToWithController("EventPage.fxml");
        page.showEvent(event.getEventId());
    }

    // ----- map drawer -----

    private void initMap() {
        mapView = new MapView();
        mapView.setZoom(15);

        EventMapLayer layer = new EventMapLayer(this::onPinClick);
        mapView.addLayer(layer);
        mapPanel.getChildren().setAll(mapView);

        try {
            layer.setEvents(dedupeByTitleAndVenue(
                    new EventDAO(Database.DBConnect()).findUpcoming()));
        } catch (Exception e) {
            System.err.println("Could not load map pins: " + e.getMessage());
        }

        centreWhenSized();
    }

    private void centreWhenSized() {
        if (mapPanel.getWidth() > 0 && mapPanel.getHeight() > 0) {
            centreMap();
            return;
        }
        mapPanel.heightProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number old, Number now) {
                if (mapPanel.getWidth() > 0 && now.doubleValue() > 0) {
                    mapPanel.heightProperty().removeListener(this);
                    centreMap();
                }
            }
        });
    }

    private void centreMap() {
        User user = session.getCurrentUser().orElse(null);
        double lat = user != null && user.getHomeLat() != null ? user.getHomeLat() : DEFAULT_LAT;
        double lng = user != null && user.getHomeLong() != null ? user.getHomeLong() : DEFAULT_LNG;
        mapView.flyTo(0, new MapPoint(lat, lng), 0.1);
    }

    private void onPinClick(List<Event> atPoint) {
        if (atPoint.size() == 1) {
            selectedEvent = atPoint.get(0);
            showEventSummary(selectedEvent);
        } else {
            showVenueSummary(atPoint);
        }
        showEventDetails();
    }

    private void showVenueSummary(List<Event> atPoint) {
        selectedEvent = null;
        detailsPanel.getChildren().clear();

        FontIcon closeIcon = new FontIcon("bi-x");
        closeIcon.setIconSize(22);

        Button close = new Button();
        close.setGraphic(closeIcon);
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> hideEventDetails());

        HBox topRow = new HBox(close);
        topRow.setAlignment(Pos.CENTER_RIGHT);

        Event first = atPoint.get(0);
        Label venue = new Label(first.getVenueName() == null ? "This venue" : first.getVenueName());
        venue.getStyleClass().add("card-title");
        venue.setWrapText(true);

        Label count = new Label(atPoint.size() + " events");
        count.getStyleClass().add("muted-text");

        VBox rows = new VBox();
        atPoint.forEach(event -> rows.getChildren().add(buildVenueRow(event, atPoint)));

        ScrollPane scroller = new ScrollPane(rows);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.getStyleClass().add("panel-scroll");
        VBox.setVgrow(scroller, Priority.ALWAYS);

        detailsPanel.getChildren().addAll(topRow, venue, count, scroller);
    }

    private Node buildVenueRow(Event event, List<Event> atPoint) {
        Label weekday = new Label(ROW_WEEKDAY.format(event.getStartTime()).toUpperCase());
        weekday.getStyleClass().add("row-date-small");

        Label day = new Label(ROW_DAY.format(event.getStartTime()));
        day.getStyleClass().add("row-date-day");

        Label month = new Label(ROW_MONTH.format(event.getStartTime()).toUpperCase());
        month.getStyleClass().add("row-date-small");

        VBox date = new VBox(weekday, day, month);
        date.getStyleClass().add("row-date");
        date.setAlignment(Pos.CENTER);

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("row-title");
        title.setWrapText(true);

        Label time = new Label(ROW_TIME.format(event.getStartTime()));
        time.getStyleClass().add("muted-text");

        VBox text = new VBox(3, title, time);
        text.setAlignment(Pos.CENTER_LEFT);
        text.setMinWidth(0);   // lets the title wrap instead of forcing the row wider
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(14, date, text);
        row.getStyleClass().add("venue-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setOnMouseClicked(e -> {
            e.consume();
            selectedEvent = event;
            showEventSummary(event, () -> showVenueSummary(atPoint));
        });

        return row;
    }

    private void showEventSummary(Event event) {
        showEventSummary(event, null);
    }

    // Row in venue list passes a way back to that list; single pin/event passes null.
    private void showEventSummary(Event event, Runnable onBack) {
        detailsPanel.getChildren().clear();

        FontIcon closeIcon = new FontIcon("bi-x");
        closeIcon.setIconSize(22);

        Button close = new Button();
        close.setGraphic(closeIcon);
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> hideEventDetails());
        close.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        if (onBack != null) {
            Button back = new Button("← " + (event.getVenueName() == null
                    ? "Back" : event.getVenueName()));
            back.getStyleClass().add("view-all-link");
            back.setMaxWidth(260);
            back.setOnAction(e -> onBack.run());
            back.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
            topRow.getChildren().add(back);
        }
        topRow.getChildren().addAll(spacer, close);
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
            setTabOpen(true);
            slide(drawer, 0);
            slide(drawerTab, -(rootPane.getWidth() - TAB_W));
        } else {
            state = 0;
            setTabOpen(false);
            slide(drawer, rootPane.getWidth());
            slide(detailsPanel, -DETAILS_W);
            slide(drawerTab, 0);
        }
    }

    private void setTabOpen(boolean open) {
        drawerTab.getTooltip().setText(open ? "Back to events" : "Map");
        drawerTabIcon.setIconLiteral(open ? "bi-house" : "bi-map");
        drawerTabIcon.setIconSize(open ? 28 : 22);
        if (open && !drawerTab.getStyleClass().contains("map-tab-open")) {
            drawerTab.getStyleClass().add("map-tab-open");
        } else if (!open) {
            drawerTab.getStyleClass().remove("map-tab-open");
        }
    }

    public void showEventDetails() {
        state = 2;
        slide(detailsPanel, TAB_W);
    }

    public void hideEventDetails() {
        state = 1;
        slide(detailsPanel, -DETAILS_W);
    }
}