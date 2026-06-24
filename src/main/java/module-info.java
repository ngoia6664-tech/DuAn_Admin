module com.example.duan_admin {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.duan_admin to javafx.fxml;
    exports com.example.duan_admin;
}