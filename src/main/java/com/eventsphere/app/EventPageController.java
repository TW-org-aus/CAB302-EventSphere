package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EventPageController {

    @FXML
    private TextField commentInput;

    @FXML
    protected void onFilterTopClick() {
        System.out.println("Filter comments: Top");
    }

    @FXML
    protected void onFilterLatestClick() {
        System.out.println("Filter comments: Latest");
    }

    @FXML
    protected void onFilterHostRepliesClick() {
        System.out.println("Filter comments: Host Replies");
    }

    @FXML
    protected void onSortTopClick() {
        System.out.println("Sort comments: Top");
    }

    @FXML
    protected void onSortLatestClick() {
        System.out.println("Sort comments: Latest");
    }

    @FXML
    protected void onSortHostRepliesClick() {
        System.out.println("Sort comments: Host Replies");
    }

    @FXML
    protected void onPostCommentClick() {
        System.out.println("Post comment clicked: " + commentInput.getText());
    }
}
