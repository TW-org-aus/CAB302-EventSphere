package com.eventsphere.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

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

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("landing-page.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1280, 800));
    }
}
