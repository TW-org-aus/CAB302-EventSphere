package com.eventsphere.app;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LandingPageController {

    @FXML private StackPane rootPane;

    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;

    @FXML private HBox drawer;
    @FXML private Button drawerTab;
    @FXML private VBox detailsPanel;
    @FXML private StackPane mapPanel;

    private double mapX;
    private double closedX;
    private int state = 0;   // 0 = closed, 1 = map, 2 = map + details

    @FXML
    public void initialize() {
        // Map fills whatever width is left after the drawer tab and details panel,
        // rather than assuming the window's full default width.
        mapPanel.prefWidthProperty().bind(
                rootPane.widthProperty()
                        .subtract(detailsPanel.widthProperty())
                        .subtract(drawerTab.widthProperty()));

        drawer.widthProperty().addListener((obs, oldVal, newVal) -> {
            mapX = detailsPanel.getWidth();
            closedX = mapX + mapPanel.getWidth();
            if (state == 0) {
                drawer.setTranslateX(closedX);
            }
        });
    }

    @FXML
    protected void onMoreInfoClick() {
        Router.navigateTo("EventPage.fxml");
    }

    private void slideTo(double x) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), drawer);
        tt.setToX(x);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    protected void onToggleDrawer() {
        if (state == 0) {
            state = 1;
            drawerTab.setText("›");
            slideTo(mapX);
        } else {
            state = 0;
            drawerTab.setText("‹");
            slideTo(closedX);
        }
    }

    public void showEventDetails() {
        state = 2;
        drawerTab.setText("›");
        slideTo(0);
    }

    @FXML
    protected void onPrevHero() {
        System.out.println("prev");
    }

    @FXML
    protected void onNextHero() {
        System.out.println("next");
    }
}
