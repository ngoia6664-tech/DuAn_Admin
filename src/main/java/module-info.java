module com.example.duan_admin {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.zxing.javase;
    requires com.google.zxing;
    requires opencv;
    requires java.desktop;
    requires com.google.gson;
    requires org.json;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;


    opens com.example.duan_admin to javafx.fxml;
    exports com.example.duan_admin;
}