package com.example.duan_admin;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QRScannerController extends BaseController {

    @FXML private ImageView cameraView;
    @FXML private Label lblResult;
    @FXML private TextField txtShowtimeId;
    @FXML private Button btnStart;
    @FXML private Button btnStop;

    private VideoCapture capture;
    private ScheduledExecutorService scheduler;
    private boolean isScanning = false;
    private volatile boolean qrDetected = false; // volatile giúp đồng bộ chính xác giữa các Thread

    private static final String API_URL = "http://localhost:8080/api/Ticket/validateQR";
    private final MultiFormatReader qrReader = new MultiFormatReader();
    private final Map<DecodeHintType, Object> hints = new HashMap<>();

    @FXML
    public void initialize() {
        nu.pattern.OpenCV.loadLocally();
        btnStop.setDisable(true);

        // Cấu hình bộ đọc QR một lần duy nhất để tiết kiệm tài nguyên
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    // ============ BẬT CAMERA ============
    @FXML
    private void handleStart() {
        if (txtShowtimeId.getText().trim().isEmpty()) {
            setStatus("Vui lòng nhập Showtime ID!", true);
            return;
        }

        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            setStatus("Không thể mở camera!", true);
            return;
        }

        isScanning = true;
        qrDetected = false;
        btnStart.setDisable(true);
        btnStop.setDisable(false);
        setStatus("Đang quét... Hướng mã QR vào camera", false);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Quét mỗi 150ms để tối ưu tải cho CPU thay vì 100ms cũ
        scheduler.scheduleAtFixedRate(this::processFrame, 0, 150, TimeUnit.MILLISECONDS);
    }

    // ============ TẮT CAMERA ============
    @FXML
    private void handleStop() {
        stopCamera();
        setStatus("Đã dừng quét.", false);
        Platform.runLater(() -> cameraView.setImage(null)); // Xóa frame cuối cùng trên giao diện
    }

    private void stopCamera() {
        isScanning = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        Platform.runLater(() -> {
            btnStart.setDisable(false);
            btnStop.setDisable(true);
        });
    }

    // ============ XỬ LÝ TỪNG FRAME ============
    private void processFrame() {
        if (!isScanning) return;

        Mat frame = new Mat();
        if (!capture.read(frame) || frame.empty()) return;

        // 1. Hiển thị ngay frame gốc lên màn hình giao diện
        Image fxImage = matToImage(frame);
        Platform.runLater(() -> cameraView.setImage(fxImage));

        // Nếu đang mở popup hoặc đang chờ gọi API trước đó, đóng băng quét QR tiếp theo
        if (qrDetected) return;

        // 2. Tăng nhẹ độ sáng/tương phản trên bản sao để nhận diện tốt hơn ở môi trường thiếu sáng
        Mat processed = new Mat();
        Core.convertScaleAbs(frame, processed, 1.3, 20);

        try {
            // Chuyển đổi siêu tốc Mat sang BufferedImage không cần nén PNG/JPEG để tăng tốc độ nhận dạng
            BufferedImage buffered = matToBufferedImageFast(processed);
            if (buffered == null) return;

            LuminanceSource source = new BufferedImageLuminanceSource(buffered);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = qrReader.decode(bitmap, hints);

            // Đọc thành công mã QR
            String qrCode = result.getText();
            qrDetected = true; // Đóng băng tạm thời trạng thái quét

            Platform.runLater(() -> {
                setStatus("Đọc được mã: " + qrCode + "\nĐang xác thực...", false);
                validateTicket(qrCode);
            });

        } catch (NotFoundException e) {
            // Không tìm thấy mã trong frame này, bỏ qua để đợi frame tiếp theo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============ GỌI API XÁC THỰC VÉ ============
    private void validateTicket(String ticketCode) {
        Long showtimeId = Long.parseLong(txtShowtimeId.getText().trim());

        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ConfigLoader.getBaseUrl() + "/api/Ticket/validateQR" + "?ticketCode=" + ticketCode + "&showtimeId=" + showtimeId ))
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                final int statusCode = response.statusCode();
                final String responseBody = response.body();

                System.out.println("QR Code: " + ticketCode);
                System.out.println("Showtime ID: " + showtimeId);
                System.out.println("Status: " + statusCode);
                System.out.println("Body: " + responseBody);

                Platform.runLater(() -> {
                    if (statusCode == 200) {
                        setStatus("✅ " + responseBody, false);
                        // 🟢 POPUP THÀNH CÔNG - Màu xanh lá (#44dd44)
                        showCustomAlert(
                                "Xác thực thành công",
                                "Vé hợp lệ!",
                                "Nội dung phản hồi: " + responseBody,
                                "#44dd44",
                                true
                        );
                    } else {
                        setStatus("Lỗi " + statusCode + ": " + responseBody, true);
                        // 🔴 POPUP THẤT BẠI - Màu đỏ (#ff4444)
                        showCustomAlert(
                                "Xác thực thất bại",
                                "Mã lỗi hệ thống: " + statusCode,
                                "Chi tiết lỗi: " + responseBody,
                                "#ff4444",
                                false
                        );
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("Không thể kết nối server!", true);
                    // 🟠 POPUP MẤT KẾT NỐI - Màu vàng cam (#ffbb33)
                    showCustomAlert(
                            "Lỗi kết nối",
                            "Mất kết nối tới máy chủ Backend!",
                            "Không thể thực hiện gửi yêu cầu kiểm tra vé. Vui lòng kiểm tra lại mạng hoặc server.",
                            "#ffbb33",
                            false
                    );
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ============ BACK ============
    @FXML
    private void handleBack() {
        stopCamera();
        MainViewController.getInstance().hienTrangHome();
    }

    // ============ HELPER / CUSTOM COMPONENT ============

    // Hàm hiển thị Cửa sổ Popup đẹp mắt, phong cách Dark Mode và viền màu linh hoạt
    private void showCustomAlert(String title, String header, String content, String baseColor, boolean isSuccess) {
        Alert alert = new Alert(isSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();

        // CSS Style hiện đại cho khung Popup
        dialogPane.setStyle(
                "-fx-background-color: #1e1e1e; " +
                        "-fx-border-color: " + baseColor + "; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-background-radius: 6px;"
        );

        // Xóa màu nền xám mặc định của thanh chứa tiêu đề
        if (dialogPane.lookup(".header-panel") != null) {
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: transparent;");
        }

        // Tùy biến chữ tiêu đề chính
        dialogPane.lookup(".label.header-text").setStyle(
                "-fx-text-fill: " + baseColor + "; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );

        // Tùy biến chữ nội dung thông báo
        dialogPane.lookup(".content.label").setStyle(
                "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 13px;"
        );

        // Định dạng lại nút xác nhận (OK) tương thích với màu chủ đạo
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: " + baseColor + "; " +
                            "-fx-text-fill: #ffffff; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand;"
            );
        }

        // Chờ người dùng click nút đóng cửa sổ thông báo
        alert.showAndWait();

        // SAU KHI ĐÓNG POPUP: Tự động mở khóa đóng băng để cho phép camera quét tiếp vé mới
        if (isScanning) {
            qrDetected = false;
            setStatus("Đang quét... Hướng mã QR vào camera", false);
        }
    }

    private void setStatus(String message, boolean isError) {
        lblResult.setText(message);
        lblResult.setStyle(isError
                ? "-fx-text-fill: #ff4444; -fx-font-size: 14px; -fx-font-weight: bold;"
                : "-fx-text-fill: #44dd44; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    // Chuyển đổi frame dùng hiển thị đồ họa JavaFX (giữ định dạng nén PNG truyền thống)
    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    // Cơ chế trích xuất mảng byte thô cực nhanh trực tiếp từ bộ nhớ RAM (không qua nén tệp tin), giúp giải mã ZXing mượt mà không delay
    private BufferedImage matToBufferedImageFast(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    @Override
    public void Init() {
        // Triển khai bổ sung nếu kế thừa lớp trừu tượng
    }
}