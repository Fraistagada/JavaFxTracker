module fr.esgi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens fr.esgi to javafx.fxml;
    exports fr.esgi;

    opens fr.esgi.controllers to javafx.fxml;
    exports fr.esgi.controllers;

    opens fr.esgi.models to javafx.fxml;
    exports fr.esgi.models;
}