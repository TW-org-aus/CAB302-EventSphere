package com.eventsphere.app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class LandingPageController {
    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;

    @FXML
    protected void onPrevHero() {
        System.out.println("prev");
    }

    @FXML
    protected void onNextHero() {
        System.out.println("next");
    }
}