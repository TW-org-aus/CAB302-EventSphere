module com.eventsphere.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.eventsphere.app to javafx.fxml;
    exports com.eventsphere.app;
}