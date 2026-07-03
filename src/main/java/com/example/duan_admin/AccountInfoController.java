package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.json.JSONObject;

public class AccountInfoController {

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtRole;

    @FXML
    public void initialize() {

        loadAdminInfo();

    }

    private void loadAdminInfo() {

        Long adminId = AdminSession.getAdminId();
        System.out.println("AdminID = " + adminId);
        HTTPService.sendFullRequestAsync(
                "GET",
                "/api/admin/info/" + adminId,
                null,
                null,
                null
        ).thenAccept(response -> {

            if (response.statusCode() == 200) {

                JSONObject json = new JSONObject(response.body());

                Platform.runLater(() -> {

                    txtUsername.setText(json.getString("username"));
                    txtFullName.setText(json.getString("fullName"));
                    txtRole.setText(json.getString("role"));

                });

            } else {

                System.out.println(response.body());

            }

        }).exceptionally(ex -> {

            ex.printStackTrace();
            return null;

        });

    }

    @FXML
    private void switchToChangePassword() {

        AccountManagementController
                .getInstance()
                .showChangePassword();

    }
}