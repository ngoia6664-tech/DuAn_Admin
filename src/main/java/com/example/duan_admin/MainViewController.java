package com.example.duan_admin;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.json.JSONObject;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.List;

public class MainViewController {

    @FXML private StackPane contentPane;

    // Khai báo chính xác các FX:ID từ file FXML để điều khiển ẩn/hiện chuyển đổi giao diện
    @FXML private AnchorPane loginPane;
    @FXML private BorderPane dashboardPane;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;
    @FXML private ImageView loginBgImage;
    @FXML private Button btnTrangChu;
    @FXML private Button btnThemPhim;
    @FXML private Button btnThemRap;
    @FXML private Button btnThemPhong;
    @FXML private Button btnThemSuat;
    @FXML private Button btnQuetQR;
    @FXML private Button btnQuanTriDuLieu;
    @FXML private Button btnQuanLyTaiKhoan;
    private java.util.List<Button> menuButtons;
    private static MainViewController instance;

    public static MainViewController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        loginBgImage.fitWidthProperty().bind(
                ((StackPane) loginBgImage.getParent()).widthProperty()
        );
        loginBgImage.fitHeightProperty().bind(
                ((StackPane) loginBgImage.getParent()).heightProperty()
        );
        menuButtons = List.of(
                btnTrangChu, btnThemPhim, btnThemRap, btnThemPhong,
                btnThemSuat, btnQuetQR, btnQuanTriDuLieu, btnQuanLyTaiKhoan
        );
        showLoginScreen();
    }
    private void setActiveButton(Button active) {
        for (Button btn : menuButtons) {
            btn.getStyleClass().remove("active-tab");
        }
        active.getStyleClass().add("active-tab");
    }

    /**
     * Sự kiện khi nhấn nút ĐĂNG NHẬP
     */
    @FXML
    private void login() {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Kiểm tra nhập liệu
        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
            lblStatus.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        // Tạo JSON gửi lên Backend
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);

        HTTPService.sendFullRequestAsync(
                "POST",
                "/api/auth/login",
                null,
                body.toString(),
                 Session.getToken()
        ).thenAccept(response -> {

            System.out.println("========== RESPONSE ==========");
            System.out.println("Status = " + response.statusCode());
            System.out.println("Body = " + response.body());

            Platform.runLater(() -> {

                if (response.statusCode() == 200) {
                    JSONObject obj = new JSONObject(response.body());
                    Session.setToken(obj.getString("token"));
                    System.out.println(Session.getToken());

                    lblStatus.setText("");

                    loginPane.setVisible(false);
                    loginPane.setManaged(false);

                    dashboardPane.setVisible(true);
                    dashboardPane.setManaged(true);

                    hienTrangHome();

                    System.out.println("Đăng nhập thành công!");

                } else {

                    lblStatus.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                    lblStatus.setText("Sai tài khoản hoặc mật khẩu!");

                    System.out.println(response.body());
                }

            });

        }).exceptionally(ex -> {

            Platform.runLater(() -> {
                lblStatus.setStyle("-fx-text-fill: #ff4444;");
                lblStatus.setText("Không kết nối được tới Server!");
            });

            ex.printStackTrace();
            return null;
        });

    }

    /**
     * Sự kiện khi nhấn nút ĐĂNG XUẤT ở góc dưới Sidebar
     */
    @FXML
    private void logout() {
        // Tạo Alert xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất không?");

        // Tùy chỉnh 2 nút
        ButtonType btnTiepTuc = new ButtonType("Tiếp tục đăng xuất");
        ButtonType btnQuayLai = new ButtonType("Quay lại", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnTiepTuc, btnQuayLai);

        // Style hộp thoại theo theme Cinema Gold
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #13131F;" +
                        "-fx-border-color: #C9A84C;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;"
        );
        dialogPane.lookup(".content.label").setStyle(
                "-fx-text-fill: #E8E0D0;" +
                        "-fx-font-size: 14px;"
        );

        // Style nút bên trong Alert
        dialogPane.lookupButton(btnTiepTuc).setStyle(
                "-fx-background-color: #C9A84C;" +
                        "-fx-text-fill: #0D0D14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;"
        );
        dialogPane.lookupButton(btnQuayLai).setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6B6B8A;" +
                        "-fx-border-color: #1E1E30;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;"
        );

        // Xử lý kết quả
        alert.showAndWait().ifPresent(result -> {
            if (result == btnTiepTuc) {
                AdminSession.clear();
                txtUsername.clear();
                txtPassword.clear();
                lblStatus.setText("");
                showLoginScreen();
                System.out.println("Đã đăng xuất khỏi hệ thống.");
            }
            // Nếu chọn "Quay lại" thì không làm gì cả
        });
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
        setActiveButton(btnTrangChu);
    }

    @FXML
    private void moTrangChu() {
        hienTrangHome();
    }

    @FXML
    private void moThemPhim() {
        loadView("AddMovie.fxml");
        setActiveButton(btnThemPhim);
    }

    @FXML
    private void moThemRap() {
        loadView("AddCinema.fxml");
        setActiveButton(btnThemRap);
    }

    @FXML
    private void moThemPhong() {
        loadView("AddShowRoom.fxml");
        setActiveButton(btnThemPhong);
    }

    @FXML
    private void moThemSuat() {
        loadView("AddShowTime.fxml");
        setActiveButton(btnThemSuat);
    }

    @FXML
    private void quetQR() {
        loadView("QRScanner.fxml");
        setActiveButton(btnQuetQR);
    }

    @FXML
    private void moQuanTriDuLieu(){
        loadView("DataManagementView.fxml");
        setActiveButton(btnQuanTriDuLieu);
    }

    @FXML
    private void moQuanLyTaiKhoan(){
        loadView("UserManagementView.fxml");
        setActiveButton(btnQuanLyTaiKhoan);
    }
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            view.setOpacity(0);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
            BaseController controller = loader.getController();
            controller.Init();
        } catch (IOException e) {
            System.err.println("Không thể tải file FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}