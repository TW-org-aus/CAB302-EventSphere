package com.eventsphere.app;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.service.UserService;

import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class InterestPicker {
    private static final String BUBBLE_DEFAULT_STYLE = "-fx-background-color: #F7F7F7; -fx-text-fill: #1A1A1A; -fx-border-color: #E5E7EB; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6 14 6 14; -fx-font-size: 12px;";
    private static final String BUBBLE_SELECTED_STYLE = "-fx-background-color: #E3F0FF; -fx-text-fill: #2F80ED; -fx-border-color: #2F80ED; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 6 14 6 14; -fx-font-size: 12px; -fx-font-weight: bold;";

    private final Label hint;
    private final Set<Category> selected = new LinkedHashSet<>();

    InterestPicker(FlowPane bubbles, Label hint, Collection<Category> initial) {
        this.hint = hint;
        for (Category category : Category.values()) {
            ToggleButton bubble = new ToggleButton(category.getDbValue());
            bubble.setSelected(initial.contains(category) && selected.size() < UserService.MAX_INTERESTS);
            if (bubble.isSelected()) {
                selected.add(category);
            }
            bubble.setStyle(bubble.isSelected() ? BUBBLE_SELECTED_STYLE : BUBBLE_DEFAULT_STYLE);
            bubble.setOnAction(e -> onToggled(bubble, category));
            bubbles.getChildren().add(bubble);
        }
        updateHint();
    }
    Set<Category> getSelected() {
        return Collections.unmodifiableSet(selected);
    }
    private void onToggled(ToggleButton bubble, Category category) {
        if (bubble.isSelected()) {
            if (selected.size() >= UserService.MAX_INTERESTS) {
                bubble.setSelected(false);
                return;
            }
            selected.add(category);
            bubble.setStyle(BUBBLE_SELECTED_STYLE);
        } else {
            selected.remove(category);
            bubble.setStyle(BUBBLE_DEFAULT_STYLE);
        }
        updateHint();
    }
    private void updateHint() {
        hint.setText(selected.size() + "/" + UserService.MAX_INTERESTS + " selected");
    }
}
