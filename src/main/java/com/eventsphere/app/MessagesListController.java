package com.eventsphere.app;

import com.eventsphere.app.model.User;
import com.eventsphere.app.service.GoingService;
import com.eventsphere.app.service.MessagingService;
import com.eventsphere.app.service.MessagingService.ConversationPreview;
import com.eventsphere.app.service.SessionManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Optional;

public class MessagesListController {

    @FXML private VBox conversationList;

    private final MessagingService messaging;
    private final SessionManager session;

    private List<ConversationPreview> shown;

    public MessagesListController(MessagingService messaging, SessionManager session) {
        this.messaging = messaging;
        this.session = session;
    }

    @FXML
    private void initialize() {
        Optional<User> user = session.getCurrentUser();
        if (user.isEmpty()) {
            showMessage("Log in to see your messages.");
            return;
        }

        int userId = user.get().getUserId();
        refresh(userId);
        ScreenPoller.start(conversationList, () -> refresh(userId));
    }

    private void refresh(int userId) {
        List<ConversationPreview> latest;
        try {
            latest = messaging.inboxFor(userId);
        } catch (RuntimeException e) {
            System.err.println("Could not load conversations: " + e.getMessage());
            return;
        }
        if (latest.equals(shown)) {
            return;
        }
        shown = latest;
        if (latest.isEmpty()) {
            showMessage("No conversations yet.");
            return;
        }
        conversationList.getChildren().setAll(latest.stream().map(this::buildRow).toList());
    }
    // layout
    private HBox buildRow(ConversationPreview preview) {
        Label avatar = new Label(GoingService.initialsFor(preview.other()));
        avatar.getStyleClass().add("avatar");

        Label name = new Label(preview.other().getFullName());
        name.getStyleClass().add("card-title");
        name.setStyle("-fx-font-size: 15px");
        Label last = new Label(preview.lastMessage().isEmpty() ? "No messages yet" : preview.lastMessage());
        last.getStyleClass().add("muted-text");

        VBox text = new VBox(name, last);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox row = new HBox(16, avatar, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("event-row");
        row.setOnMouseClicked(event -> {
            MessagesController chat = Router.navigateToWithController("messages-view.fxml");
            chat.setConversation(preview.conversation(), preview.other());
        });
        return row;
    }

    private void showMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        label.setWrapText(true);
        conversationList.getChildren().setAll(label);
    }
}
