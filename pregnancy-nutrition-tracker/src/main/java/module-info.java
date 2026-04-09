module com.pregnancy.tracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;

    opens com.pregnancy.tracker to javafx.fxml;
    opens com.pregnancy.tracker.controller to javafx.fxml;
    opens com.pregnancy.tracker.model to javafx.fxml, com.google.gson;

    exports com.pregnancy.tracker;
    exports com.pregnancy.tracker.controller;
    exports com.pregnancy.tracker.model;
    exports com.pregnancy.tracker.dao;
    exports com.pregnancy.tracker.service;
    exports com.pregnancy.tracker.util;
}
