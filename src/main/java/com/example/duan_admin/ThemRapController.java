
        package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ThemRapController {

    @FXML
    private TextField txtTenRap;

    @FXML
    private TextArea txtDiaChi;

    @FXML
    private void themRap() {

        String tenRap = txtTenRap.getText().trim();
        String diaChi = txtDiaChi.getText().trim();

        if (tenRap.isEmpty() || diaChi.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập đầy đủ thông tin!");
            alert.show();

            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText("Thêm rạp thành công!");
        alert.show();

        txtTenRap.clear();
        txtDiaChi.clear();
    }

    @FXML
    private void thoat() {

        txtTenRap.clear();
        txtDiaChi.clear();
    }
}


