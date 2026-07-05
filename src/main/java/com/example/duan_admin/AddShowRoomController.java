package com.example.duan_admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class AddShowRoomController extends BaseController {

    @FXML private ComboBox<CinemaItem> cmbCinema;
    @FXML private TextField txtRoomName;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        loadCinemas();
    }

    // ============ LOAD DANH SÁCH RẠP ============
    private void loadCinemas() {
        Thread thread = new Thread(() -> {
            try {
                System.out.println(">>> ĐANG GỌI URL: " + "http://localhost:8080/api/media/cinemas");
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ConfigLoader.getBaseUrl()+"/api/media/cinemas"))
                        .timeout(Duration.ofSeconds(60))
                        .header("Authorization", "Bearer " + Session.getToken())
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> cinemas = mapper.readValue(
                            response.body(),
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    Platform.runLater(() -> {
                        cmbCinema.getItems().clear();
                        for (Map<String, Object> cinema : cinemas) {
                            Long id = ((Number) cinema.get("id")).longValue();
                            String name = (String) cinema.get("name");
                            cmbCinema.getItems().add(new CinemaItem(id, name));
                        }
                    });
                } else {
                    Platform.runLater(() ->
                            showPopup("Lỗi hệ thống", "Không thể tải danh sách rạp!\nMã lỗi từ Server: " + response.statusCode())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showPopup("Lỗi kết nối", "Không thể kết nối tới server để tải danh sách rạp!"));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============ XỬ LÝ LƯU PHÒNG ============
    @FXML
    private void handleSave() {
        CinemaItem selectedCinema = cmbCinema.getValue();
        String roomName = txtRoomName.getText().trim();

        // Validate dữ liệu đầu vào
        if (selectedCinema == null) {
            showPopup("Dữ liệu không hợp lệ", "Vui lòng chọn rạp chiếu trước khi tạo phòng!");
            return;
        }
        if (roomName.isEmpty()) {
            showPopup("Dữ liệu không hợp lệ", "Vui lòng nhập tên phòng chiếu!");
            return;
        }

        if (lblStatus != null) lblStatus.setText("Đang xử lý...");

        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                ObjectMapper mapper = new ObjectMapper();

                // Build request body
                String json = mapper.writeValueAsString(Map.of(
                        "cinemaID", selectedCinema.getId(),
                        "roomName", roomName
                ));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ConfigLoader.getBaseUrl()+"/api/media/addShowRoom"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + Session.getToken())
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Map<String, Object> result = mapper.readValue(
                            response.body(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    Platform.runLater(() -> {
                        if (lblStatus != null) lblStatus.setText("");
                        showPopup("Tạo phòng thành công",
                                "Đã tạo phòng: " + result.get("roomName") +
                                        "\nSức chứa: " + result.get("capacity") + " ghế");
                        MainViewController.getInstance().hienTrangHome();
                    });
                } else {
                    Platform.runLater(() -> {
                        if (lblStatus != null) lblStatus.setText("");
                        showPopup("Thêm phòng thất bại", "Mã phản hồi từ hệ thống: " + response.statusCode());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (lblStatus != null) lblStatus.setText("");
                    showPopup("Lỗi kết nối", "Hệ thống không thể truyền tải dữ liệu tới máy chủ. Vui lòng thử lại!");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============ HỦY ============
    @FXML
    private void handleCancel() {
        MainViewController.getInstance().hienTrangHome();
    }

    // ============ HELPER POPUP DIALOG (ĐÃ FIX THEME GOLD) ============
    private void showPopup(String title, String message) {
        // Sử dụng INFORMATION thay vì ERROR để tránh sinh ra cái icon dấu X đỏ mặc định của JavaFX
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Chuẩn màu Cinema Gold đồng bộ
        String goldColor = "#C9A84C";
        String darkBgColor = "#13131F";

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + darkBgColor + ";" +
                        "-fx-border-color: " + goldColor + ";" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;"
        );

        // Style nội dung văn bản chữ màu kem sáng sang trọng
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle(
                    "-fx-text-fill: #E8E0D0;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
        }

        // Style nút OK thành nền vàng chữ đen (Khớp hoàn toàn với nút ADD SHOW ROOM)
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: " + goldColor + ";" +
                            "-fx-text-fill: #0D0D14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6px;" +
                            "-fx-padding: 8 18;" +
                            "-fx-cursor: hand;"
            );
        }
        alert.showAndWait();
    }

    @Override
    public void Init() {}

    // ============ INNER CLASS ============
    public static class CinemaItem {
        private final Long id;
        private final String name;

        public CinemaItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

}