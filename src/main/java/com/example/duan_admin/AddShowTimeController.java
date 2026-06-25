package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AddShowTimeController {

    @FXML private ComboBox<String> cmbMovie;
    @FXML private ComboBox<String> cmbShowRoom;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;
    @FXML private TextField txtPrice;

    @FXML
    public void initialize() {
        cmbMovie.getItems().addAll("Avatar 3", "Avengers");
        cmbShowRoom.getItems().addAll("Phòng chiếu 01", "Phòng chiếu 02");
    }

    @FXML
    private void handleSave() {
        System.out.println("Tạm tạo suất chiếu giá: " + txtPrice.getText());

        // Chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }

    @FXML
    private void handleCancel() {
        // Hủy bỏ và chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }
}