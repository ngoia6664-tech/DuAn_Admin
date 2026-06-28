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

    @FXML private AnchorPane loginPane;
    @FXML private BorderPane dashboardPane;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private static MainViewController instance;

    // ĐÃ ĐỔI: Sử dụng chính xác HomeController (Trang quản lý doanh số)
    private HomeController homeController;

    public static MainViewController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        showLoginScreen();
    }

    /**
     * Dành cho HomeController tự đăng ký thực thể của nó với hệ thống điều hướng chính
     */
    public void setHomeController(HomeController controller) {
        this.homeController = controller;
    }

    @FXML
    private void login() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if ("admin".equals(username) && "123".equals(password)) {
            lblStatus.setText("");

            loginPane.setVisible(false);
            loginPane.setManaged(false);

            dashboardPane.setVisible(true);
            dashboardPane.setManaged(true);

            // Đăng nhập xong -> Vào thẳng trang doanh số tổng quan
            hienTrangHome();

            System.out.println("Đăng nhập thành công tài khoản: " + username);
        } else {
            lblStatus.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
            lblStatus.setText("Sai tài khoản hoặc mật khẩu!");
        }
    }

    @FXML
    private void logout() {
        txtUsername.clear();
        txtPassword.clear();
        lblStatus.setText("");

        // Giải phóng reference khi đăng xuất để giải phóng bộ nhớ
        this.homeController = null;

        showLoginScreen();
        System.out.println("Đã đăng xuất khỏi hệ thống.");
    }

    private void showLoginScreen() {
        loginPane.setVisible(true);
        loginPane.setManaged(true);

        dashboardPane.setVisible(false);
        dashboardPane.setManaged(false);
    }

    // --- CÁC HÀM ĐIỀU HƯỚNG GIAO DIỆN (TABS) ---

    public void hienTrangHome() {
        try {
            contentPane.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
            Parent view = loader.load();

            // Nhận thực thể HomeController (Dashboard doanh số)
            this.homeController = loader.getController();

            contentPane.getChildren().add(view);

            // BỔ SUNG QUAN TRỌNG: Mỗi lần chuyển tab về Home, bắt nó chạy hàm load lại doanh thu, số vé...
            if (this.homeController != null) {
                // Giả sử sau này bạn viết hàm này bên HomeController để gọi API lấy doanh số
                // this.homeController.loadThongKeTongQuan();
            }

        } catch (IOException e) {
            System.err.println("Không thể tải file FXML trang chủ: Home.fxml");
            e.printStackTrace();
        }
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
;
    @FXML
    private void quetQR() {loadView("QRScanner.fxml");}
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
    @FXML
    private void moTrangHome() {
        hienTrangHome();
    }
}