package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EventCommentsController {

    @FXML
    private TextField commentInput;

    @FXML
    protected void onPostCommentClick() {
        System.out.println("Post comment clicked: " + commentInput.getText());
    }

    @FXML
    protected void onBackClick() {
        Router.navigateTo("landing-page.fxml");
    }
}
