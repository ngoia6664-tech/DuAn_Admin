package com.example.duan_admin;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddCinemaController extends BaseController {

    @FXML private TextField txtName;
    @FXML private TextField txtAddress;

    @FXML
    private void handleSave() {

        try {
            String name = txtName.getText().trim();
            String address = txtAddress.getText().trim();

            // 1. Kiểm tra từng trường cụ thể
            if (name.isEmpty() && address.isEmpty()) {
                hienThongBao("Thiếu thông tin", "Vui lòng nhập đầy đủ tên rạp và địa chỉ!", Alert.AlertType.WARNING);
                return;
            }
            if (name.isEmpty()) {
                hienThongBao("Thiếu tên rạp", "Tên rạp chiếu không được để trống!", Alert.AlertType.WARNING);
                txtName.requestFocus();
                return;
            }
            if (address.isEmpty()) {
                hienThongBao("Thiếu địa chỉ", "Địa chỉ rạp không được để trống!", Alert.AlertType.WARNING);
                txtAddress.requestFocus();
                return;
            }
            if (name.length() < 3) {
                hienThongBao("Tên không hợp lệ", "Tên rạp phải có ít nhất 3 ký tự!", Alert.AlertType.WARNING);
                txtName.requestFocus();
                return;
            }
            if (address.length() < 5) {
                hienThongBao("Địa chỉ không hợp lệ", "Địa chỉ phải có ít nhất 5 ký tự!", Alert.AlertType.WARNING);
                txtAddress.requestFocus();
                return;
            }

            // 2. Chuyển đổi dữ liệu thành JSON string
            Map<String, String> cinemaData = Map.of(
                    "name", name,
                    "address", address
            );
            String jsonBody = new Gson().toJson(cinemaData);

            // 3. Đường dẫn API
            String endpoint = "/api/media/addCinema";

            System.out.println("Đang gửi yêu cầu thêm rạp sang Backend...");

            // 4. Gọi API bằng HTTPService
            HTTPService.sendFullRequestAsync("POST", endpoint, null, jsonBody, Session.getToken())
                    .thenAccept(response -> {
                        int statusCode = response.statusCode();
                        Platform.runLater(() -> {
                            if (statusCode == 200 || statusCode == 201) {
                                hienThongBao("Thành công", "Đã thêm rạp chiếu thành công!", Alert.AlertType.INFORMATION);
                                txtName.clear();
                                txtAddress.clear();
                            } else if (statusCode == 409) {
                                hienThongBao("Trùng dữ liệu", "Tên rạp này đã tồn tại trong hệ thống!", Alert.AlertType.ERROR);
                            } else {
                                hienThongBao("Lỗi " + statusCode, "Thêm rạp thất bại!\nChi tiết: " + response.body(), Alert.AlertType.ERROR);
                            }
                        });
                    })
                    .orTimeout(10, TimeUnit.SECONDS).exceptionally(ex -> {
                        Platform.runLater(() ->
                                hienThongBao("Mất kết nối", "Không thể kết nối tới Server!\nVui lòng kiểm tra lại kết nối.", Alert.AlertType.ERROR)
                        );
                        ex.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            hienThongBao("Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleCancel() {
        // Hủy bỏ và chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }
    private void hienThongBao(String tieuDe, String noiDung, Alert.AlertType loaiThongBao) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        // Style hộp thoại theo theme Cinema Gold
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #13131F;" +
                        "-fx-border-color: #C9A84C;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;"
        );

        // Style nội dung
        ((Label) dialogPane.lookup(".content.label")).setStyle(
                "-fx-text-fill: #E8E0D0;" +
                        "-fx-font-size: 14px;"
        );

        // Style nút OK
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle(
                "-fx-background-color: #C9A84C;" +
                        "-fx-text-fill: #0D0D14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 8 18;" +
                        "-fx-cursor: hand;"
        );
        alert.showAndWait();


    }

    @Override
    public void Init() {

    }
}