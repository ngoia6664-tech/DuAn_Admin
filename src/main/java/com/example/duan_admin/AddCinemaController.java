package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Map;

public class AddCinemaController {

    @FXML private TextField txtName;
    @FXML private TextField txtAddress;

    @FXML
    private void handleSave() {

        // Tạm thời in ra màn hình console kiểm tra dữ liệu nhập
        System.out.println("Tạm lưu rạp: " + txtName.getText() + " - Địa chỉ: " + txtAddress.getText());

        // Chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }

    @FXML
    private void handleCancel() {
        // Hủy bỏ và chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }
}