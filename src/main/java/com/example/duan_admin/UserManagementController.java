package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserManagementController extends BaseController {

    // ── TableView & Columns ──────────────────────────────────────────────────
    @FXML private TableView<AccountDTO>   tableAccounts;
    @FXML private TableColumn<AccountDTO, Long>   colId;
    @FXML private TableColumn<AccountDTO, String> colUsername;
    @FXML private TableColumn<AccountDTO, String> colEmail;
    @FXML private TableColumn<AccountDTO, String> colRole;
    @FXML private TableColumn<AccountDTO, Double> colBalance;

    // ── Detail pane fields ───────────────────────────────────────────────────
    @FXML private TextField txtId;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRole;
    @FXML private TextField txtBalance;

    // ── Search field ─────────────────────────────────────────────────────────
    @FXML private TextField txtSearch;

    /** Toàn bộ dữ liệu gốc (dùng khi tìm kiếm) */
    private final List<AccountDTO> allAccounts = new ArrayList<>();

    // ────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

    }

    // ── Load tất cả tài khoản ────────────────────────────────────────────────
    private void loadAccounts() {
        LoadingOverlayManager.start(tableAccounts);
        HTTPService.sendFullRequestAsync("GET", "/api/account/all", null, null, Session.getToken())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<AccountDTO> list = parseAccounts(response.body());
                        Platform.runLater(() -> {
                            allAccounts.clear();
                            LoadingOverlayManager.stop();
                            allAccounts.addAll(list);
                            tableAccounts.getItems().setAll(list);
                        });
                    } else {
                        LoadingOverlayManager.stop();
                        showError("Không tải được danh sách tài khoản (HTTP "
                                + response.statusCode() + ")");
                    }
                }).orTimeout(10, TimeUnit.SECONDS).exceptionally(ex -> {
                    LoadingOverlayManager.stop();
                    System.err.println("Lỗi kết nối Movie: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });;
    }

    // ── Parse JSON → List<AccountDTO> ────────────────────────────────────────
    private List<AccountDTO> parseAccounts(String json) {
        List<AccountDTO> result = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            result.add(new AccountDTO(
                    o.getLong("id"),
                    o.getString("username"),
                    o.getString("email"),
                    o.getString("role"),
                    o.optDouble("balance", 0.0)
            ));
        }
        return result;
    }

    // ── Tìm kiếm (lọc phía client) ──────────────────────────────────────────
    @FXML
    private void searchAccount() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            tableAccounts.getItems().setAll(allAccounts);
            return;
        }
        List<AccountDTO> filtered = allAccounts.stream()
                .filter(a -> a.getUsername().toLowerCase().contains(keyword)
                        || a.getEmail()   .toLowerCase().contains(keyword))
                .toList();
        tableAccounts.getItems().setAll(filtered);
    }

    // ── Refresh ──────────────────────────────────────────────────────────────
    @FXML
    private void refreshTable() {
        txtSearch.clear();
        loadAccounts();
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void Init() {
        // 1. Bind mỗi cột vào đúng thuộc tính của AccountDTO
        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail   .setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole    .setCellValueFactory(new PropertyValueFactory<>("role"));
        colBalance .setCellValueFactory(new PropertyValueFactory<>("balance"));

        // 2. Khi click một hàng → hiển thị chi tiết bên phải
        loadAccounts();


    }
}