package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    // Sau này bạn gọi dữ liệu tổng quan từ database rồi set vào các nhãn này
    @FXML private Label lblDoanhThu;
    @FXML private Label lblVeDaBan;
    @FXML private Label lblSuatChieu;

    @FXML
    public void initialize() {
        System.out.println("Trang Home tổng quan đã khởi tạo thành công!");
        // Chỗ này để viết logic load số liệu lên màn hình
    }
}