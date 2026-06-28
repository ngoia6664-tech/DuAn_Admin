module com.example.duan_admin {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.net.http;
    requires com.google.gson;
    requires org.json;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;

    opens com.example.duan_admin to javafx.fxml, javafx.base;
    exports com.example.duan_admin;
}