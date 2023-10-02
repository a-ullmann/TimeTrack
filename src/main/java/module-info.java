module com.ullmann.timetrack {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires java.prefs;

    opens com.ullmann.timetrack to javafx.fxml;
    exports com.ullmann.timetrack;
    exports com.ullmann.timetrack.models;
    opens com.ullmann.timetrack.models to javafx.fxml;
    exports com.ullmann.timetrack.controllers;
    opens com.ullmann.timetrack.controllers to javafx.fxml;
    exports com.ullmann.timetrack.services;
    opens com.ullmann.timetrack.services to javafx.fxml;
}