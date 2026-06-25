package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainViewController {

    @FXML private StackPane contentPane;

    // Khai báo chính xác các FX:ID từ file FXML để điều khiển ẩn/hiện chuyển đổi giao diện
    @FXML private AnchorPane loginPane;
    @FXML private BorderPane dashboardPane;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private static MainViewController instance;

    public static MainViewController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        // Mặc định ban đầu: Bắt người dùng đăng nhập trước (Hiện Login, Ẩn Dashboard)
        showLoginScreen();
    }

    /**
     * Sự kiện khi nhấn nút ĐĂNG NHẬP
     */
    @FXML
    private void login() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Kiểm tra tài khoản cứng (Tạm thời test offline không cần database)
        if ("admin".equals(username) && "123".equals(password)) {
            lblStatus.setText(""); // Xóa thông báo lỗi cũ nếu có

            // 1. Ẩn màn hình đăng nhập
            loginPane.setVisible(false);
            loginPane.setManaged(false);

            // 2. Hiển thị màn hình Dashboard quản trị
            dashboardPane.setVisible(true);
            dashboardPane.setManaged(true);

            // 3. Tự động load trang tổng quan (Home) vào vùng nội dung chính
            hienTrangHome();

            System.out.println("Đăng nhập thành công tài khoản: " + username);
        } else {
            // Hiển thị thông báo lỗi lên nhãn trạng thái dưới nút Login
            lblStatus.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
            lblStatus.setText("Sai tài khoản hoặc mật khẩu!");
        }
    }

    /**
     * Sự kiện khi nhấn nút ĐĂNG XUẤT ở góc dưới Sidebar
     */
    @FXML
    private void logout() {
        // Xóa sạch dữ liệu đã nhập ở form login trước đó
        txtUsername.clear();
        txtPassword.clear();
        lblStatus.setText("");

        // Quay về giao diện đăng nhập ban đầu
        showLoginScreen();
        System.out.println("Đã đăng xuất khỏi hệ thống.");
    }

    /**
     * Hàm hỗ trợ chuyển đổi nhanh về trạng thái màn hình Đăng nhập
     */
    private void showLoginScreen() {
        // Hiện màn hình Login
        loginPane.setVisible(true);
        loginPane.setManaged(true);

        // Ẩn màn hình Dashboard quản trị
        dashboardPane.setVisible(false);
        dashboardPane.setManaged(false);
    }

    // --- CÁC HÀM CHUYỂN TAB CŨ GIỮ NGUYÊN KHÔNG ĐỔI ---

    public void hienTrangHome() {
        loadView("Home.fxml");
    }


    @FXML
    private void moThemPhim() {
        loadView("AddMovie.fxml");
    }

    @FXML
    private void moThemRap() {
        loadView("AddCinema.fxml");
    }

    @FXML
    private void moThemPhong() {
        loadView("AddShowRoom.fxml");
    }

    @FXML
    private void moThemSuat() {
        loadView("AddShowTime.fxml");
    }

    @FXML
    private void quetQR() {
        System.out.println("Chức năng quét QR đầu mục");
    }

    private void loadView(String fxmlPath) {
        try {
            contentPane.getChildren().clear();
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("Không thể tải file FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}