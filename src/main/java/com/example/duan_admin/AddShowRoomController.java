package com.example.duan_admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
                            setStatus("Không thể tải danh sách rạp! Mã lỗi: " + response.statusCode(), true)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Không thể kết nối tới server!", true));
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

        // Validate
        if (selectedCinema == null) {
            setStatus("Vui lòng chọn rạp chiếu!", true);
            return;
        }
        if (roomName.isEmpty()) {
            setStatus("Vui lòng nhập tên phòng chiếu!", true);
            return;
        }

        setStatus("Đang xử lý...", false);

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
                        DialogUtils.showThongBao("Thành công",
                                "Đã tạo phòng: " + result.get("roomName") +
                                        "\nSức chứa: " + result.get("capacity") + " ghế",
                                Alert.AlertType.INFORMATION);
                        MainViewController.getInstance().hienTrangHome();
                    });
                } else {
                    Platform.runLater(() ->
                            setStatus("Thêm phòng thất bại! Mã lỗi: " + response.statusCode(), true)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Không thể kết nối server!", true));
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

    // ============ HELPER ============
    private void setStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError
                ? "-fx-text-fill: #ff4444; -fx-font-size: 13px;"
                : "-fx-text-fill: #44bb44; -fx-font-size: 13px;");
    }

    @Override
    public void Init() {

    }

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
