package com.eventsphere.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.eventsphere.app.model.Event;
import com.gluonhq.maps.MapLayer;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

/** Gluon MapLayer that plots clickable event markers on the map; fires a callback when a marker is selected. */
public class EventMapLayer extends MapLayer {

    private final List<Event> events = new ArrayList<>();
    private final List<Node> markers = new ArrayList<>();
    private final Consumer<List<Event>> onPinClick;


    public EventMapLayer(Consumer<List<Event>> onPinClick) {
        this.onPinClick = onPinClick;
    }

    public void setEvents(List<Event> source) {
        events.clear();
        markers.clear();
        getChildren().clear();

        for (List<Event> atPoint : groupByLocation(source).values()) {
            Node marker = buildMarker(atPoint);

            events.add(atPoint.get(0));
            markers.add(marker);
            getChildren().add(marker);
        }
        markDirty();
    }

    // One pin per coordinate: a venue with a season of shows otherwise stacks
    // dozens of pins on the same point, which reads as one smudged marker.
    private static Map<String, List<Event>> groupByLocation(List<Event> source) {
        Map<String, List<Event>> byPoint = new LinkedHashMap<>();
        for (Event event : source) {
            if (!hasUsableLocation(event)) {
                continue;
            }
            byPoint.computeIfAbsent(event.getLat() + "," + event.getLng(),
                    key -> new ArrayList<>()).add(event);
        }
        byPoint.values().forEach(list -> list.sort(Comparator.comparing(
                Event::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))));
        return byPoint;
    }

    // 0,0 is the Gulf of Guinea: a missing coordinate stored as zero.
    private static boolean hasUsableLocation(Event event) {
        return event.getLat() != null && event.getLng() != null
                && !(event.getLat() == 0.0 && event.getLng() == 0.0);
    }

    private Node buildMarker(List<Event> atPoint) {
        Event soonest = atPoint.get(0);
        int count = atPoint.size();

        FontIcon pin = new FontIcon("bi-geo-alt-fill");
        pin.setIconSize(28);
        pin.getStyleClass().add("map-pin");
        pin.setTranslateX(-14);
        pin.setTranslateY(-28);

        Label venue = new Label(soonest.getVenueName() == null
                ? "Venue unknown" : soonest.getVenueName());
        venue.getStyleClass().add("map-pin-venue");

        Label detail = new Label(count == 1 ? soonest.getTitle() : count + " events");
        detail.getStyleClass().add("map-pin-title");

        VBox bubble = new VBox(2, venue, detail);
        bubble.getStyleClass().add("map-pin-label");
        bubble.setMaxWidth(220);
        bubble.setVisible(false);
        bubble.setMouseTransparent(true);
        bubble.setTranslateY(-120);
        bubble.widthProperty().addListener((obs, old, width) ->
                bubble.setTranslateX(-width.doubleValue() / 2));

        Group marker = new Group(bubble, pin);
        pin.setCursor(Cursor.HAND);
        pin.setOnMouseEntered(e -> {
            bubble.setVisible(true);
            marker.toFront();
        });
        pin.setOnMouseExited(e -> bubble.setVisible(false));
        pin.setOnMouseClicked(e -> onPinClick.accept(atPoint));
        return marker;
    }

    @Override
    protected void layoutLayer() {
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Node marker = markers.get(i);
            Point2D point = getMapPoint(event.getLat(), event.getLng());
            marker.setTranslateX(point.getX());
            marker.setTranslateY(point.getY());
        }
    }
}