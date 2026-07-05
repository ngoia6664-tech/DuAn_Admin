package com.example.duan_admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class HomeController extends BaseController {

    @FXML
    private Label lblDoanhThu;

    @FXML
    private Label lblVeDaBan;

    @FXML
    private Label lblSuatChieu;

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        loadOverview();
    }

    private void loadOverview() {

        String endpoint = "/api/Overview";

        HTTPService.sendFullRequestAsync(
                "GET",
                endpoint,
                null,
                null,
                Session.getToken()
        ).thenAccept(response -> {

            Platform.runLater(() -> {

                if (response.statusCode() == 200) {

                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);

                    double doanhThu = json.get("totalRevenue").getAsDouble();
                    long veDaBan = json.get("totalTickets").getAsLong();
                    long suatChieu = json.get("activeShowTimes").getAsLong();

                    lblDoanhThu.setText(String.format("%,.0f VNĐ", doanhThu));
                    lblVeDaBan.setText(String.valueOf(veDaBan));
                    lblSuatChieu.setText(String.valueOf(suatChieu));

                } else {

                    hienThongBao(
                            "Lỗi",
                            "Không lấy được dữ liệu tổng quan!\n"
                                    + response.body(),
                            Alert.AlertType.ERROR
                    );

                }

            });

        }).exceptionally(ex -> {

            Platform.runLater(() ->
                    hienThongBao(
                            "Mất kết nối",
                            "Không thể kết nối tới Backend!",
                            Alert.AlertType.ERROR
                    )
            );

            ex.printStackTrace();
            return null;
        });
    }

    private void hienThongBao(String title,
                              String content,
                              Alert.AlertType type) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

    }

    @Override
    public void Init() {

    }
}