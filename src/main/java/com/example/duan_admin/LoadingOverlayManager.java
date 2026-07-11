package com.example.duan_admin;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class LoadingOverlayManager {

    private static StackPane currentOverlay = null;

    /**
     * HÀM BẮT ĐẦU: Hiển thị lớp phủ xoay vòng lên màn hình
     */
    public static void start(Node targetNode) {
        // Nếu đang có một lớp phủ hiển thị rồi thì không tạo thêm nữa
        if (currentOverlay != null || targetNode == null || targetNode.getScene() == null) {
            System.err.println("target node null!!!!!: "+targetNode+", "+currentOverlay+","+targetNode.getScene());
            return;

        }

        Scene scene = targetNode.getScene();
        Parent root = scene.getRoot();

        // 1. Tạo lớp phủ (Overlay)
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); // Nền đen mờ 60%
        overlay.setMouseTransparent(false);

        // 2. Tạo cụm vòng xoay + chữ thông báo
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(60, 60);

        Label label = new Label("Đang tải dữ liệu. Vui lòng chờ...");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        container.getChildren().addAll(progress, label);
        overlay.getChildren().add(container);

        currentOverlay = overlay;

        // 3. Thực hiện lồng cấu trúc một cách AN TOÀN trên luồng UI
        Platform.runLater(() -> {
            // Trường hợp 1: Nếu Root hiện tại ĐÃ LÀ một StackPane (ví dụ bạn đã bọc từ trước)
            if (root instanceof StackPane) {
                ((StackPane) root).getChildren().add(overlay);
            } 
            // Trường hợp 2: Root là layout khác (VBox, BorderPane...). 
            // Ta sẽ tráo đổi Scene Root bằng cách bọc một StackPane mới ra NGOÀI Root cũ.
            else {
                // Bước này giải quyết lỗi "already inside a scene-graph" bằng cách xóa root cũ ra khỏi scene trước
                scene.setRoot(new StackPane()); 
                
                // Bây giờ tạo StackPane bọc bên ngoài để chứa cả giao diện cũ lẫn lớp phủ
                StackPane wrapper = new StackPane();
                wrapper.getChildren().addAll(root, overlay);
                
                // Đặt lại wrapper làm Root chính thức cho Scene
                scene.setRoot(wrapper);
            }
        });
    }

    /**
     * HÀM DỪNG: Xóa bỏ hoàn toàn lớp phủ, trả lại giao diện bình thường
     */
    public static void stop() {
        if (currentOverlay == null) {
            return;
        }

        Platform.runLater(() -> {
            Parent overlayParent = currentOverlay.getParent();
            
            if (overlayParent instanceof StackPane) {
                StackPane wrapper = (StackPane) overlayParent;
                Scene scene = wrapper.getScene();

                // Xóa lớp phủ đi
                wrapper.getChildren().remove(currentOverlay);

                // Nếu wrapper này đang chứa Root cũ (tức là nó có 2 phần tử lúc trước, giờ còn 1)
                // Ta trả lại tự do cho giao diện cũ bằng cách đặt lại nó làm Root chính thức
                if (scene != null && wrapper.getChildren().size() > 0) {
                    Parent originalRoot = (Parent) wrapper.getChildren().get(0);
                    wrapper.getChildren().remove(originalRoot); // Tháo gỡ khỏi wrapper
                    scene.setRoot(originalRoot); // Đặt lại làm Root của Scene
                }
            }
            
            // Khôi phục trạng thái bộ nhớ
            currentOverlay = null;
        });
    }
}
