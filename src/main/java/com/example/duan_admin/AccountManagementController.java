package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class AccountManagementController {

    private static AccountManagementController instance;

    @FXML
    private StackPane contentPane;

    @FXML
    private VBox adminMenu;

    @FXML
    private VBox userMenu;

    @FXML
    private ToggleButton btnAdmin;

    @FXML
    private ToggleButton btnUser;

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {

        instance = this;

        // Gom 2 ToggleButton thành 1 nhóm
        toggleGroup = new ToggleGroup();
        btnAdmin.setToggleGroup(toggleGroup);
        btnUser.setToggleGroup(toggleGroup);

        // Mặc định chọn Admin
        btnAdmin.setSelected(true);

        adminMenu.setVisible(true);
        adminMenu.setManaged(true);

        userMenu.setVisible(false);
        userMenu.setManaged(false);

        showAccountInfo();
    }

    public static AccountManagementController getInstance() {
        return instance;
    }

    /**
     * ===========================
     * Chuyển sang ADMIN
     * ===========================
     */
    @FXML
    private void switchToAdmin() {

        btnAdmin.setSelected(true);

        adminMenu.setVisible(true);
        adminMenu.setManaged(true);

        userMenu.setVisible(false);
        userMenu.setManaged(false);

        showAccountInfo();
    }

    /**
     * ===========================
     * Chuyển sang USER
     * ===========================
     */
    @FXML
    private void switchToUser() {

        btnUser.setSelected(true);

        adminMenu.setVisible(false);
        adminMenu.setManaged(false);

        userMenu.setVisible(true);
        userMenu.setManaged(true);

        showUserList();
    }

    /**
     * Admin
     */
    @FXML
    public void showAccountInfo() {
        loadView("AccountInfoView.fxml");
    }

    @FXML
    public void showChangePassword() {
        loadView("ChangePasswordView.fxml");
    }

    /**
     * User
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