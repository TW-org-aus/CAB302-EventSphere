package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MessagesController {

    @FXML
    private TextField messageInput;

    private String conversationId;

    public void setConversation(String conversationId) {
        this.conversationId = conversationId;
        System.out.println("Loaded conversation: " + conversationId);
    }

    @FXML
    protected void onSendClick() {
        System.out.println("Send clicked: " + messageInput.getText());
    }
}
