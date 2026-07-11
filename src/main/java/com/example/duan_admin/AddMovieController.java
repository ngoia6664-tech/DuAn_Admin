package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.StandardCopyOption;

public class AddMovieController extends BaseController{

    @FXML private TextField txtTitle;
    @FXML private TextField txtGenre;
    @FXML private ComboBox<String> cmbRating;
    @FXML private DatePicker dpReleaseDate;
    @FXML private TextField txtDuration;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtImageUrl;
    @FXML private Label lblFileName;

    private File selectedImageFile;

    @FXML
    public void initialize() {
        cmbRating.getItems().addAll("P", "T13", "T16", "T18", "C");
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh poster phim");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(txtTitle.getScene().getWindow());
        if (file != null) {
            this.selectedImageFile = file;
            this.lblFileName.setText("Đang chọn file: " + file.getName());
            this.txtImageUrl.setText("");
        }
    }

    @FXML
    private void handleSave() {
        String urlText = txtImageUrl.getText().trim();

        // 1. Kiểm tra nhanh xem các ô nhập chữ có bị trống không trước khi xử lý ảnh
        if (txtTitle.getText().isEmpty() || txtGenre.getText().isEmpty() ||
                cmbRating.getValue() == null || dpReleaseDate.getValue() == null ||
                txtDuration.getText().isEmpty()) {
            showAlert("Thông báo", "Vui lòng điền đầy đủ các thông tin chữ của phim!");
            return;
        }

        // 2. Xử lý tải ảnh từ URL về file tạm nếu người dùng dán link và không chọn file cứng
        if (selectedImageFile == null && !urlText.isEmpty()) {
            try {
                File tempFile = File.createTempFile("downloaded_poster", ".jpg");
                tempFile.deleteOnExit();
                try (InputStream in = new URL(urlText).openStream()) {
                    Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                // Gán file tạm vào biến chính để chuẩn bị gửi Multipart
                this.selectedImageFile = tempFile;
            } catch (Exception e) {
                showAlert("Lỗi", "Không thể tải ảnh từ link URL vừa nhập. Vui lòng kiểm tra lại link internet của ảnh!");
                return;
            }
        }

        // 3. Sau khi đã xử lý URL, nếu vẫn không có file ảnh nào được chọn -> Báo lỗi chặn lại
        if (selectedImageFile == null) {
            showAlert("Thông báo", "Vui lòng chọn một file ảnh từ máy hoặc nhập link URL ảnh hợp lệ!");
            return;
        }

        // 4. Đóng gói chuỗi JSON khớp cấu trúc AddMovieRequest của Backend
        String jsonRequest = String.format(
                "{\"title\":\"%s\",\"description\":\"%s\",\"genre\":\"%s\",\"rating\":\"%s\",\"releaseDate\":\"%s\",\"duration\":%s}",
                txtTitle.getText().replace("\"", "\\\""),
                txtDescription.getText().replace("\"", "\\\""),
                txtGenre.getText().replace("\"", "\\\""),
                cmbRating.getValue(),
                dpReleaseDate.getValue().toString(),
                txtDuration.getText().trim()
        );

        // 5. Khởi chạy Thread phụ để gọi API
        new Thread(() -> {
            try {
                String boundary = "JavaFXBoundary-" + UUID.randomUUID().toString();
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                // Dòng này nằm trong try-catch và gọi hàm helper phía dưới sẽ hết lỗi đỏ hoàn toàn
                byte[] multipartBody = createMultipartBody(boundary, jsonRequest, selectedImageFile);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(ConfigLoader.getBaseUrl()+"api/feature/addMovieInfo"))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", "Bearer " + Session.getToken())
                        .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                        .build();

                System.out.println("Đang gửi dữ liệu Multipart lên Backend...");
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                // In kết quả phản hồi ra Console để tiện theo dõi
                System.out.println("Mã phản hồi từ Server: " + response.statusCode());
                System.out.println("Chi tiết phản hồi: " + response.body());

                // Trả kết quả xử lý về luồng giao diện chính
                Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        showAlert("Thành công", "Đã thêm phim mới vào cơ sở dữ liệu thành công!");
                        MainViewController.getInstance().hienTrangHome();
                    } else {
                        showAlert("Thất bại", "Lỗi từ Backend (" + response.statusCode() + "): " + response.body());
                    }
                });

            } catch (Exception e) {
                System.err.println(" Lỗi xảy ra trong quá trình đóng gói hoặc gửi API:");
                e.printStackTrace();

                Platform.runLater(() ->
                        showAlert("Lỗi kết nối", "Không thể chèn phim hoặc kết nối đến Server Backend: " + e.getMessage())
                );
            }
        }).start(); // Kích hoạt chạy Thread
    }

    /**
     * HÀM BỔ SUNG: Đóng gói dữ liệu định dạng Multipart/form-data dạng chuỗi Byte
     */
    private byte[] createMultipartBody(String boundary, String jsonText, File file) throws Exception {
        List<byte[]> byteArrays = new ArrayList<>();
        String lineEnd = "\r\n";

        // Phần 1: Gửi chuỗi dữ liệu JSON thông tin phim
        String dataPart = "--" + boundary + lineEnd +
                "Content-Disposition: form-data; name=\"data\"" + lineEnd +
                "Content-Type: application/json; charset=UTF-8" + lineEnd + lineEnd +
                jsonText + lineEnd;
        byteArrays.add(dataPart.getBytes("UTF-8"));

        // Phần 2: Gửi dữ liệu File Binary của ảnh Poster
        String filePartHeader = "--" + boundary + lineEnd +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + lineEnd +
                "Content-Type: " + Files.probeContentType(file.toPath()) + lineEnd + lineEnd;
        byteArrays.add(filePartHeader.getBytes("UTF-8"));

        byteArrays.add(Files.readAllBytes(file.toPath()));
        byteArrays.add(lineEnd.getBytes("UTF-8"));

        // Vạch kết thúc gói tin Multipart
        String endBoundary = "--" + boundary + "--" + lineEnd;
        byteArrays.add(endBoundary.getBytes("UTF-8"));

        // Hợp nhất toàn bộ mảng bytes lại làm một mảng duy nhất để truyền tải đi
        int totalLength = byteArrays.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] arr : byteArrays) {
            System.arraycopy(arr, 0, result, currentIndex, arr.length);
            currentIndex += arr.length;
        }
        return result;
    }

    @FXML
    private void handleCancel() {
        MainViewController.getInstance().hienTrangHome();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void Init() {

    }
}