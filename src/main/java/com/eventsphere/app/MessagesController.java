package com.eventsphere.app;

import com.eventsphere.app.model.Conversation;
import com.eventsphere.app.model.Message;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.GoingService;
import com.eventsphere.app.service.MessagingService;
import com.eventsphere.app.service.SessionManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


public class MessagesController {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_AND_TIME = DateTimeFormatter.ofPattern("d MMM, h:mm a");
    private static final double MAX_MESSAGE_WIDTH = 450;

    @FXML
    private ScrollPane scroll;
    @FXML
    private Label avatarLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private VBox messageList;
    @FXML
    private Label sendError;
    @FXML
    private TextField messageInput;

    private final MessagingService messaging;
    private final SessionManager session;

    private int userId;
    private Conversation conversation;
    private User other;
    private List<Message> shown;

    public MessagesController(MessagingService messaging, SessionManager session) {
        this.messaging = messaging;
        this.session = session;
    }

    @FXML
    private void initialize() {
        sendError.managedProperty().bind(sendError.visibleProperty());
        // keeps newest message in view
        messageList.heightProperty().addListener((observable, oldHeight, height) -> scroll.setVvalue(1.0));
        ScreenPoller.start(messageList, this::refresh);
    }

    public void setConversation(Conversation conversation, User other) {
        this.conversation = conversation;
        this.other = other;
        userId = session.getCurrentUser().map(User::getUserId).orElse(-1);
        avatarLabel.setText(GoingService.initialsFor(other));
        nameLabel.setText(other.getFullName());
        refresh();
    }

    @FXML
    protected void onSendClick() {
        if (conversation == null) {
            return;
        }
        Optional<String> problem;
        try {
            problem = messaging.send(conversation.getConversationId(), userId, messageInput.getText());
        } catch (RuntimeException e) {
            e.printStackTrace();
            problem = Optional.of("Could not send your message. Please try again");
        }
        if (problem.isPresent()) {
            sendError.setText(problem.get());
            sendError.setVisible(true);
            return;
        }
        sendError.setVisible(false);
        messageInput.clear();
        refresh();
    }

    @FXML
    protected void onBackClick() {
        Router.navigateTo("messages-list-view.fxml");
    }

    private void refresh() {
        if (conversation == null) {
            return;
        }
        List<Message> latest;
        try {
            latest = messaging.openChat(conversation.getConversationId(), userId);
        } catch (RuntimeException e) {
            System.err.println("Could not load messages: " + e.getMessage());
            return;
        }
        if (latest.equals(shown)) {
            return;
        }
        shown = latest;
        if (latest.isEmpty()) {
            Label empty = new Label("No messages yet");
            empty.getStyleClass().add("muted-text");
            messageList.getChildren().setAll(empty);
            return;
        }
        messageList.getChildren().setAll(latest.stream().map(this::buildMessage).toList());
    }

    private HBox buildMessage(Message message) {
        boolean mine = message.getSenderId() == userId;

        Label text = new Label(message.getContent());
        text.getStyleClass().add("body-text");
        text.setWrapText(true);
        text.setMaxWidth(MAX_MESSAGE_WIDTH);
        Label meta = new Label((mine ? "You" : other.getFullName()) + "\u00B7 " + timeOf(message.getSentAt()));
        meta.getStyleClass().add("muted-text");

        VBox content = new VBox(2, text, meta);
        content.setAlignment(mine ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        HBox row = new HBox(content);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }

    private static String timeOf(Instant sentAt) {
        if (sentAt == null) {
            return "";
        }
        ZonedDateTime local = sentAt.atZone(ZoneId.systemDefault());
        return local.toLocalDate().equals(LocalDate.now()) ? TIME.format(local) : DATE_AND_TIME.format(local);
    }
}

