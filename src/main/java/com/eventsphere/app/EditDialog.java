package com.eventsphere.app;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.Supplier;

final class EditDialog {
    private static final ButtonType SAVE = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    private EditDialog() {
    }

    static void show(Window owner, String title, Supplier<Optional<String>> save, Node... fields) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(owner);

        Label error = new Label();
        error.setWrapText(true);
        error.setStyle("-fx-font-size: 12px; -fx-text-fill: #C0392B");
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);

        VBox content = new VBox(18, fields);
        content.getChildren().add(error);
        content.setPrefWidth(360);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(EditDialog.class.getResource("app.css").toExternalForm());
        pane.setContent(content);
        pane.getButtonTypes().addAll(SAVE, ButtonType.CANCEL);
        pane.lookupButton(SAVE).getStyleClass().add("primary-button");
        pane.lookupButton(ButtonType.CANCEL).getStyleClass().add("secondary-button");

        pane.lookupButton(SAVE).addEventFilter(ActionEvent.ACTION, event -> {
            Optional<String> problem;
            try {
                problem = save.get();
            } catch (RuntimeException e) {
                e.printStackTrace();
                problem = Optional.of("Could not save your changes. Please try again");
            }
            problem.ifPresent(message -> {
                error.setText(message);
                error.setVisible(true);
                event.consume();
            });
        });
        dialog.showAndWait();
    }

    static VBox field(String label, Node input) {
        Label caption = new Label(label);
        caption.getStyleClass().add("muted-text");
        return new VBox(6, caption, input);
    }
}
