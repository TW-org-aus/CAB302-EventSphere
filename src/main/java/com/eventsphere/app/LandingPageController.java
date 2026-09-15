package com.eventsphere.app;

import javafx.scene.Node;
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
import org.kordamp.ikonli.javafx.FontIcon;

public class LandingPageController {

    @FXML private StackPane rootPane;

    @FXML private ImageView heroImage;
    @FXML private Label heroTitle;
    @FXML private Label heroBlurb;
    @FXML private HBox heroNav;

    @FXML private HBox drawer;
    @FXML private Button drawerTab;
    @FXML private FontIcon drawerTabIcon;
    @FXML private VBox detailsPanel;
    @FXML private StackPane mapPanel;

    private static final double TAB_W = 28.0;
    private static final double DETAILS_W = 420.0;

    private int state = 0;   // 0 closed, 1 map, 2 map + details

    @FXML
    public void initialize() {
        drawer.prefWidthProperty().bind(rootPane.widthProperty());
        drawer.maxWidthProperty().bind(rootPane.widthProperty());

        detailsPanel.setTranslateX(-DETAILS_W);
        drawer.setTranslateX(2000);

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (state == 0) drawer.setTranslateX(newVal.doubleValue() - TAB_W);
        });
    }

    private void slide(Node node, double x) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), node);
        tt.setToX(x);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    protected void onMoreInfoClick() {
        Router.navigateTo("EventPage.fxml");
    }

    @FXML
    protected void onToggleDrawer() {
        if (state == 0) {
            state = 1;
            drawerTabIcon.setIconLiteral("bi-chevron-right");
            slide(drawer, 0);
        } else {
            state = 0;
            drawerTabIcon.setIconLiteral("bi-chevron-left");
            slide(drawer, rootPane.getWidth() - TAB_W);
            slide(detailsPanel, -DETAILS_W);
        }
    }

    public void showEventDetails() {
        state = 2;
        drawerTabIcon.setIconLiteral("bi-chevron-right");
        slide(detailsPanel, TAB_W);
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
