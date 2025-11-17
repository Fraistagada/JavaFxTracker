module fr.esgi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.desktop;

    opens fr.esgi to javafx.fxml;
    exports fr.esgi;

    opens fr.esgi.controllers to javafx.fxml;
    exports fr.esgi.controllers;

    opens fr.esgi.models to javafx.fxml;
    exports fr.esgi.models;
}