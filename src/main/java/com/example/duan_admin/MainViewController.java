        package com.example.duan_admin;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class MainViewController {

    @FXML
    private AnchorPane loginPane;

    @FXML
    private BorderPane dashboardPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblStatus;

    @FXML
    private void login() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {

            lblStatus.setText("Vui lòng nhập đầy đủ thông tin!");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập tài khoản và mật khẩu!");
            alert.show();

            return;
        }

        // Tài khoản mặc định
        if (username.equals("admin") && password.equals("123")) {

            // Ẩn form đăng nhập
            loginPane.setVisible(false);
            loginPane.setManaged(false);

            // Hiện dashboard
            dashboardPane.setVisible(true);
            dashboardPane.setManaged(true);

            lblStatus.setText("");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Đăng nhập");
            alert.setHeaderText(null);
            alert.setContentText("Đăng nhập thành công!");
            alert.show();

        } else {

            lblStatus.setText("Sai tài khoản hoặc mật khẩu!");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Sai tài khoản hoặc mật khẩu!");
            alert.show();
        }
    }

    @FXML
    private void logout() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Đã đăng xuất!");
        alert.show();

        // Ẩn dashboard
        dashboardPane.setVisible(false);
        dashboardPane.setManaged(false);

        // Hiện login
        loginPane.setVisible(true);
        loginPane.setManaged(true);

        // Xóa dữ liệu cũ
        txtUsername.clear();
        txtPassword.clear();
        lblStatus.setText("");
    }

}

