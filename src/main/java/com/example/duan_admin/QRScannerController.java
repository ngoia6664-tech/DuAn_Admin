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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QRScannerController {

    @FXML private ImageView cameraView;
    @FXML private Label lblResult;
    @FXML private TextField txtShowtimeId;
    @FXML private Button btnStart;
    @FXML private Button btnStop;

    private VideoCapture capture;
    private ScheduledExecutorService scheduler;
    private boolean isScanning = false;
    private boolean qrDetected = false;

    private static final String API_URL = "http://localhost:8080/api/Ticket/validateQR";

    @FXML
    public void initialize() {
        nu.pattern.OpenCV.loadLocally();
        btnStop.setDisable(true);
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
        scheduler.scheduleAtFixedRate(this::processFrame, 0, 100, TimeUnit.MILLISECONDS);
    }

    // ============ TẮT CAMERA ============
    @FXML
    private void handleStop() {
        stopCamera();
        setStatus("Đã dừng quét.", false);
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
        if (!isScanning || qrDetected) return;

        Mat frame = new Mat();
        if (!capture.read(frame) || frame.empty()) return;

        // Tăng độ sáng và contrast
        Mat processed = new Mat();
        Core.convertScaleAbs(frame, processed, 1.5, 30);

        // Hiển thị frame gốc
        Platform.runLater(() -> cameraView.setImage(matToImage(frame)));

        // Đọc QR
        try {
            BufferedImage buffered = matToBufferedImage(processed);
            if (buffered == null) return;

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            LuminanceSource source = new BufferedImageLuminanceSource(buffered);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap, hints);

            // Đọc được QR → gọi API
            String qrCode = result.getText();
            qrDetected = true;
            stopCamera();

            Platform.runLater(() -> {
                setStatus("Đọc được mã: " + qrCode + "\nĐang xác thực...", false);
                validateTicket(qrCode); // 👈 Gọi hàm validateTicket
            });

        } catch (NotFoundException e) {
            // Chưa đọc được → tiếp tục quét
        }
    }

    // ============ GỌI API XÁC THỰC VÉ ============
    private void validateTicket(String ticketCode) {
        Long showtimeId = Long.parseLong(txtShowtimeId.getText().trim());

        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL
                                + "?ticketCode=" + ticketCode
                                + "&showtimeId=" + showtimeId))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                // Lưu vào biến final trước khi dùng trong lambda
                final int statusCode = response.statusCode();
                final String responseBody = response.body();

                System.out.println("QR Code: " + ticketCode);
                System.out.println("Showtime ID: " + showtimeId);
                System.out.println("Status: " + statusCode);
                System.out.println("Body: " + responseBody);

                Platform.runLater(() -> {
                    if (statusCode == 200) {
                        setStatus("✅ " + responseBody, false);
                    } else {
                        qrDetected = false;
                        setStatus("❌ Lỗi " + statusCode + ": " + responseBody, true);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    qrDetected = false;
                    setStatus("❌ Không thể kết nối server!", true);
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

    // ============ HELPER ============
    private void setStatus(String message, boolean isError) {
        lblResult.setText(message);
        lblResult.setStyle(isError
                ? "-fx-text-fill: #ff4444; -fx-font-size: 14px; -fx-font-weight: bold;"
                : "-fx-text-fill: #44dd44; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private BufferedImage matToBufferedImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return ImageIO.read(new ByteArrayInputStream(buffer.toArray()));
        } catch (IOException e) {
            return null;
        }
    }
}