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


    opens com.example.duan_admin to javafx.fxml;
    exports com.example.duan_admin;
}