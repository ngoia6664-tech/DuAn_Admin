package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.json.JSONObject;

public class ChangePasswordController {

    @FXML
    private PasswordField txtOldPass;

    @FXML
    private PasswordField txtNewPass;

    @FXML
    private PasswordField txtConfirmPass;

    @FXML
    private Label lblStatus;

    @FXML
    private void handleUpdatePassword() {

        String oldPass = txtOldPass.getText().trim();
        String newPass = txtNewPass.getText().trim();
        String confirmPass = txtConfirmPass.getText().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            lblStatus.setText("Mật khẩu xác nhận không khớp!");
            return;
        }

        JSONObject body = new JSONObject();

        body.put("adminId", AdminSession.getAdminId());
        body.put("oldPassword", oldPass);
        body.put("newPassword", newPass);

        HTTPService.sendFullRequestAsync(
                "POST",
                "/api/admin/changePassword",
                null,
                body.toString(),
                null
        ).thenAccept(response -> {

            Platform.runLater(() -> {

                if (response.statusCode() == 200) {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Đổi mật khẩu thành công!");
                    alert.showAndWait();

                    AccountManagementController.getInstance().showAccountInfo();

                } else {

                    lblStatus.setText(response.body());

                }

            });

        }).exceptionally(ex -> {

            Platform.runLater(() ->
                    lblStatus.setText("Không kết nối được Server!")
            );

            ex.printStackTrace();
            return null;

        });

    }

    @FXML
    private void goBack() {

        AccountManagementController.getInstance().showAccountInfo();

    }
}