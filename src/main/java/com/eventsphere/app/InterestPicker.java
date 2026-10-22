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

/** Reusable category bubble-picker component used on sign-up and in settings to select event interests. */
public class InterestPicker {
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
            bubble.getStyleClass().add("interest-bubble");
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
        } else {
            selected.remove(category);
        }
        updateHint();
    }
    private void updateHint() {
        hint.setText(selected.size() + "/" + UserService.MAX_INTERESTS + " selected");
    }
}
