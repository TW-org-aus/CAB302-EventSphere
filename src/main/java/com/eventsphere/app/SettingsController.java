package com.eventsphere.app;

import com.eventsphere.app.model.User;
import com.eventsphere.app.service.AuthService;
import com.eventsphere.app.service.UserService;
import com.eventsphere.app.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class SettingsController {

    private static final ButtonType SAVE = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    private final AuthService authService;
    private final UserService userService;
    private final SessionManager session;

    public SettingsController(AuthService authService, UserService userService, SessionManager session) {
        this.authService = authService;
        this.userService = userService;
        this.session = session;
    }

    @FXML private Button passwordRow;
    @FXML private Button emailRow;
    @FXML private Button nameRow;

    @FXML
    private ToggleButton notificationsToggle;

    @FXML
    protected void initialize() {
        refreshAccountRows();
    }
    private void refreshAccountRows() {
        Optional<User> user = session.getCurrentUser();
        emailRow.setText("Email: " + user.map(User::getEmail).orElse("-"));
        nameRow.setText("Name: " + user.map(User::getFullName).orElse("-"));
        passwordRow.setDisable(user.isEmpty());
        emailRow.setDisable(user.isEmpty());
        nameRow.setDisable(user.isEmpty());
    }

    @FXML
    protected void onUsernameClick() {
        System.out.println("Username setting clicked");
    }

    @FXML
    protected void onPasswordClick() {
        session.getCurrentUser().ifPresent(user -> {
            PasswordField current = new PasswordField();
            PasswordField fresh = new PasswordField();
            PasswordField confirm = new PasswordField();
            openEditor("Change password", () -> {
                if (!fresh.getText().equals(confirm.getText())) {
                    return Optional.of("New passwords don't match");
                }
                return userService.changePassword(user, current.getText(), fresh.getText());
            }, field("Current password", current), field("New password", fresh), field("Confirm new password", confirm));
        });
    }

    @FXML
    protected void onEmailClick() {
        session.getCurrentUser().ifPresent(user -> {
            TextField email = new TextField(user.getEmail());
            openEditor("Change email", () -> userService.changeEmail(user, email.getText()),
                    field("Email", email));
        });
    }

    @FXML
    protected void onNameClick() {
        session.getCurrentUser().ifPresent(user -> {
            TextField first = new TextField(user.getFirstName());
            TextField last = new TextField(user.getLastName());
            openEditor("Change name", () -> userService.changeName(user, first.getText(), last.getText()),
                    field("First name", first), field("Last name", last));
        });
    }

    private void openEditor(String title, Supplier<Optional<String>> save, Node... fields) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(nameRow.getScene().getWindow());

        Label error = new Label();
        error.setWrapText(true);
        error.setStyle("-fx-font-size: 12px; -fx-text-fill: #C0392B");
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);

        VBox content = new VBox(18, fields);
        content.getChildren().add(error);
        content.setPrefWidth(360);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
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
                problem = Optional.of("Could not save your changes. Please try again.");
            }
            problem.ifPresent(message -> {
                error.setText(message);
                error.setVisible(true);
                event.consume();
            });
        });

        dialog.showAndWait();
        refreshAccountRows();
    }

    private static VBox field(String label, TextInputControl input) {
        Label caption = new Label(label);
        caption.getStyleClass().add("muted-text");
        return new VBox(6, caption, input);
    }

    @FXML
    protected void onNightModeToggle() {
        System.out.println("Night mode toggled");
    }

    @FXML
    protected void onNotificationsToggle() {
        boolean enabled = notificationsToggle.isSelected();
        notificationsToggle.setText(enabled ? "On" : "Off");
        System.out.println("Notifications toggled " + (enabled ? "on" : "off"));
    }

    @FXML
    protected void onLogOutClick() {
        authService.logout();
        Router.navigateTo("login-view.fxml");
    }

    @FXML
    protected void onDeleteAccountClick() {
        System.out.println("Delete account clicked");
    }
}

