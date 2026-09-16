package com.eventsphere.app;

import com.eventsphere.app.model.Event;
import com.gluonhq.maps.MapLayer;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventMapLayer extends MapLayer {

    private final List<Event> events = new ArrayList<>();
    private final List<Node> markers = new ArrayList<>();
    private final Consumer<Event> onPinClick;

    public EventMapLayer(Consumer<Event> onPinClick) {
        this.onPinClick = onPinClick;
    }

    public void setEvents(List<Event> source) {
        events.clear();
        markers.clear();
        getChildren().clear();

        for (Event event : source) {
            if (event.getLat() == null || event.getLng() == null) {
                continue;
            }

            Circle pin = new Circle(9, Color.web("#F4502F"));
            pin.setStroke(Color.WHITE);
            pin.setStrokeWidth(3);
            pin.setCursor(javafx.scene.Cursor.HAND);
            pin.setOnMouseClicked(e -> onPinClick.accept(event));

            events.add(event);
            markers.add(pin);
            getChildren().add(pin);
        }
        markDirty();
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