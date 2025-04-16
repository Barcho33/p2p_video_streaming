module com.p2p_streaming.client {
    requires javafx.fxml;
    requires jdk.compiler;
    requires Common;
    requires java.sql;
    requires com.fasterxml.jackson.annotation;
    requires jdk.httpserver;
    requires javafx.web;
    requires javafx.graphics;


    opens ui.screens to javafx.fxml;
    opens fxml_files to javafx.fxml;
    opens logic.screen to javafx.fxml;


    exports ui.screens;
    exports logic.screen;
    exports main;

}
