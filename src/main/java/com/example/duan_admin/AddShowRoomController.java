package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AddShowRoomController {

    @FXML private ComboBox<String> cmbCinema;
    @FXML private TextField txtRoomName;

    @FXML
    public void initialize() {
        cmbCinema.getItems().addAll("Cơ sở Hùng Vương Plazza", "Cơ sở Nguyễn Du");
    }

    @FXML
    private void handleSave() {
        System.out.println("Tạm tạo phòng: " + txtRoomName.getText());

        // Chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }

    @FXML
    private void handleCancel() {
        // Hủy bỏ và chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }
}