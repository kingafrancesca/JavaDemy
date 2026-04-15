module com.example.javademy {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;
    requires java.compiler;

    opens com.example.javademy to javafx.fxml;
    opens com.example.javademy.model to com.google.gson;
    opens com.example.javademy.auth to com.google.gson;
    opens com.example.javademy.ai to com.google.gson;

    exports com.example.javademy;
    exports com.example.javademy.ui;
    exports com.example.javademy.model;
    exports com.example.javademy.auth;
    exports com.example.javademy.storage;
    exports com.example.javademy.engine;
    exports com.example.javademy.ai;
}