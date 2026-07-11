package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AccountManagementController {

    private static AccountManagementController instance;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        instance = this;

        // Mở luôn danh sách người dùng khi vào trang
        showUserList();
    }

    public static AccountManagementController getInstance() {
        return instance;
    }

    /**
     * Hiển thị danh sách người dùng
     */
    @FXML
    public void showUserList() {
        loadView("UserManagementView.fxml");
    }

    /**
     * Load FXML vào contentPane
     */
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}