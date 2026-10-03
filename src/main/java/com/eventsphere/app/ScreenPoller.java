package com.eventsphere.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

//
final class ScreenPoller {
    private static final Duration INTERVAL = Duration.seconds(5);

    private ScreenPoller() {
    }
    // polling stops after user leaves
    static void start(Node screenNode, Runnable refresh) {
        Timeline poll = new Timeline(new  KeyFrame(INTERVAL, event -> refresh.run()));
        poll.setCycleCount(Animation.INDEFINITE);
        screenNode.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) {
                scene.windowProperty().addListener((obs, oldWindow, window) -> {
                    if (window == null) {
                        poll.stop();
                    }
                });
            }
        });
        poll.play();
    }

}
