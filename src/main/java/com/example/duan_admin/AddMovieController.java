package com.example.duan_admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddMovieController {

    @FXML private TextField txtTitle;
    @FXML private TextField txtGenre;
    @FXML private ComboBox<String> cmbRating;
    @FXML private DatePicker dpReleaseDate;
    @FXML private TextField txtDuration;
    @FXML private TextArea txtDescription;

    @FXML
    public void initialize() {
        cmbRating.getItems().addAll("P", "T13", "T16", "T18", "C");
    }

    @FXML
    private void handleSave() {
        System.out.println("Tạm lưu phim: " + txtTitle.getText());

        // Chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }

    @FXML
    private void handleCancel() {
        // Hủy bỏ và chuyển tab quay về trang chủ
        MainViewController.getInstance().hienTrangHome();
    }
}