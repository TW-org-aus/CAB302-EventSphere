package com.eventsphere.app;

import javafx.fxml.FXML;

public class MessagesListController {

    @FXML
    protected void onConversationRowClick() {
        MessagesController controller = Router.navigateToWithController("messages-view.fxml");
        controller.setConversation("conv-1");
    }
}
