module com.eventsphere.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.bootstrapicons;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.eventsphere.app to javafx.fxml;
    exports com.eventsphere.app;
}