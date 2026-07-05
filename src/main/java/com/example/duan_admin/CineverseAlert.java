package com.example.duan_admin;



import javafx.util.Duration;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

public class CineverseAlert {

    
    public static void show(String title, String message, Region currentRegion) {
        try {
            Scene currentScene = currentRegion.getScene();
            if (currentScene == null) return;
            Window window = currentScene.getWindow();
            if (window == null) return;

            // 1. Dùng Popup để tạo hiệu ứng lớp phủ (Overlay) bao toàn bộ màn hình
            Popup popup = new Popup();

            // 2. Tạo một StackPane lớn làm container cho Popup để giả lập kích thước toàn Scene
            StackPane rootContainer = new StackPane();
            
            // Lấy chính xác độ rộng/cao của cửa sổ hiện tại để làm lớp mờ phủ kín ứng dụng
            rootContainer.prefWidthProperty().bind(currentScene.widthProperty());
            rootContainer.prefHeightProperty().bind(currentScene.heightProperty());
            
            // Nền đen mờ bao trùm toàn bộ ứng dụng
            rootContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");

            // 3. Tạo hộp thông báo bên trong
            VBox alertBox = new VBox(15);
            alertBox.getStyleClass().add("custom-alert-box");
            alertBox.setAlignment(Pos.CENTER);
            alertBox.setPadding(new Insets(22, 30, 22, 30));
            
            // Cố định kích thước hộp thông báo
            alertBox.setMinWidth(320);
            alertBox.setMaxWidth(320);
            alertBox.setMaxHeight(Region.USE_PREF_SIZE);

            // 4. Các thành phần giao diện của thông báo
            Label lblTitle = new Label(title.toUpperCase());
            lblTitle.getStyleClass().add("alert-title");

            Label lblMessage = new Label(message);
            lblMessage.getStyleClass().add("alert-message");
            lblMessage.setWrapText(true);
            lblMessage.setAlignment(Pos.CENTER);

            Button btnOk = new Button("ĐỒNG Ý");
            btnOk.getStyleClass().add("btn-alert-ok");

            // 5. Sự kiện khi bấm ĐỒNG Ý -> Tắt toàn bộ Popup thông báo
            btnOk.setOnAction(e -> popup.hide());

            alertBox.getChildren().addAll(lblTitle, lblMessage, btnOk);

            // 6. Nạp file CSS riêng biệt
            String cssPath = CineverseAlert.class.getResource("/org/net/demo/globalAlert.css").toExternalForm();
            alertBox.getStylesheets().add(cssPath);

            // 7. Thêm hộp thông báo vào giữa lớp nền mờ, rồi đưa vào popup
            rootContainer.getChildren().add(alertBox);
            StackPane.setAlignment(alertBox, Pos.CENTER);
            
            popup.getContent().add(rootContainer);

            // 8. Định vị popup luôn bám theo góc trên cùng bên trái của ứng dụng (0,0 so với Window)
            popup.setX(window.getX());
            popup.setY(window.getY());
            
            // Lắng nghe sự kiện di chuyển/co giãn cửa sổ chính để Popup tự động chạy theo bao phủ lại
            window.xProperty().addListener((obs, oldVal, newVal) -> popup.setX(newVal.doubleValue()));
            window.yProperty().addListener((obs, oldVal, newVal) -> popup.setY(newVal.doubleValue()));

            // 9. Hiển thị thông báo
            popup.show(window);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(String message, Region currentRegion) {
        try {
            Scene currentScene = currentRegion.getScene();
            if (currentScene == null) return;
            Window window = currentScene.getWindow();
            if (window == null) return;

            Popup popup = new Popup();
            StackPane toastBox = new StackPane();
            
            // Thiết kế hộp chứa nhỏ gọn, đệm viền vừa phải
            toastBox.getStyleClass().add("custom-toast-box");
            toastBox.setPadding(new Insets(15, 25, 15, 25));
            toastBox.setMaxWidth(350);

            // Nội dung text thông báo
            Label lblMessage = new Label("Cineverse thông báo: "+ message);
            lblMessage.getStyleClass().add("toast-message");
            lblMessage.setWrapText(true);
            lblMessage.setAlignment(Pos.CENTER);
            toastBox.getChildren().add(lblMessage);

            // Nạp file CSS riêng biệt
            String cssPath = CineverseAlert.class.getResource("/org/net/demo/globalAlert.css").toExternalForm();
            toastBox.getStylesheets().add(cssPath);
            popup.getContent().add(toastBox);

            // Định vị vị trí hiển thị: Góc trên bên phải của toàn bộ ứng dụng (cách mép 25px)
            popup.setX(window.getX() + window.getWidth() - 380); 
            popup.setY(window.getY() + 75); // Nằm ngay dưới thanh tiêu đề/header chính

            popup.show(window);

            // --- XỬ LÝ TỰ ĐỘNG BIẾN MẤT MƯỢT MÀ ---
            
            // 1. Chờ hiển thị trong 2.5 giây
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2.5), event -> {
                // 2. Tạo hiệu ứng mờ dần (Fade out) trong 0.5 giây tiếp theo
                FadeTransition fade = new FadeTransition(Duration.seconds(0.5), toastBox);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                
                // 3. Sau khi mờ hẳn thì xóa bỏ hoàn toàn popup
                fade.setOnFinished(e -> popup.hide());
                fade.play();
            }));
            
            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
