package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


public class AdminController {
    @FXML
    private AnchorPane loginPane;

    @FXML
    private BorderPane dashboardPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void login() {

        if(txtUsername.getText().equals("admin")
                && txtPassword.getText().equals("123")) {

            loginPane.setVisible(false);
            loginPane.setManaged(false);

            dashboardPane.setVisible(true);
            dashboardPane.setManaged(true);

        } else {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Sai tài khoản hoặc mật khẩu");
            alert.show();
        }
    }
}
