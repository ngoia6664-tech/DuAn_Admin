package com.example.duan_admin;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Labeled;

import java.util.Optional;

/**
 * Lớp tiện ích tạo các hộp thoại (Alert) đồng bộ theo theme "Cinema Gold"
 * dùng chung cho toàn bộ ứng dụng (đăng xuất, thông báo thêm rạp/phim/phòng/suất chiếu...).
 */
public final class DialogUtils {

    // ===== Bảng màu chủ đạo Cinema Gold =====
    private static final String BG_COLOR       = "#13131F";
    private static final String BORDER_COLOR   = "#C9A84C";
    private static final String TEXT_COLOR     = "#E8E0D0";
    private static final String GOLD           = "#C9A84C";
    private static final String GOLD_TEXT      = "#0D0D14";
    private static final String SECOND_TEXT    = "#6B6B8A";
    private static final String SECOND_BORDER  = "#1E1E30";

    private static final String DIALOG_STYLE =
            "-fx-background-color: " + BG_COLOR + ";" +
                    "-fx-border-color: " + BORDER_COLOR + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-background-radius: 10px;";

    private static final String CONTENT_STYLE =
            "-fx-text-fill: " + TEXT_COLOR + ";" +
                    "-fx-font-size: 14px;";

    private static final String HEADER_STYLE =
            "-fx-text-fill: " + GOLD + ";" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;";

    private static final String PRIMARY_BTN_STYLE =
            "-fx-background-color: " + GOLD + ";" +
                    "-fx-text-fill: " + GOLD_TEXT + ";" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8 18;" +
                    "-fx-cursor: hand;";

    private static final String SECONDARY_BTN_STYLE =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + SECOND_TEXT + ";" +
                    "-fx-border-color: " + SECOND_BORDER + ";" +
                    "-fx-border-radius: 6px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 8 18;" +
                    "-fx-cursor: hand;";

    private DialogUtils() {}

    /**
     * Áp style Cinema Gold lên phần khung + nội dung của DialogPane.
     */
    private static void applyBaseStyle(DialogPane dialogPane) {
        dialogPane.setStyle(DIALOG_STYLE);

        Labeled contentLabel = (Labeled) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle(CONTENT_STYLE);
            contentLabel.setWrapText(true);
        }

        Labeled headerLabel = (Labeled) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle(HEADER_STYLE);
        }
    }

    /**
     * Hiển thị thông báo 1 nút (Thành công / Cảnh báo / Lỗi / Thông tin)
     * theo đúng theme Cinema Gold, đồng bộ với hộp thoại đăng xuất.
     */
    public static void showThongBao(String tieuDe, String noiDung, AlertType loaiThongBao) {
        Alert alert = new Alert(loaiThongBao == null ? AlertType.INFORMATION : loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);

        ButtonType btnDong = new ButtonType("Đóng", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnDong);

        DialogPane dialogPane = alert.getDialogPane();
        applyBaseStyle(dialogPane);

        Labeled btn = (Labeled) dialogPane.lookupButton(btnDong);
        if (btn != null) {
            btn.setStyle(PRIMARY_BTN_STYLE);
        }

        alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại xác nhận 2 nút (giống hộp thoại đăng xuất),
     * dùng lại cho mọi xác nhận khác trong ứng dụng (xoá, huỷ, xác nhận...).
     *
     * @return true nếu người dùng bấm nút xác nhận (nút chính), false nếu bấm huỷ / đóng cửa sổ.
     */
    public static boolean showXacNhan(String tieuDe, String noiDung,
                                      String textXacNhan, String textHuy) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);

        ButtonType btnXacNhan = new ButtonType(textXacNhan, ButtonBar.ButtonData.OK_DONE);
        ButtonType btnHuy = new ButtonType(textHuy, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnXacNhan, btnHuy);

        DialogPane dialogPane = alert.getDialogPane();
        applyBaseStyle(dialogPane);

        Labeled btnXacNhanNode = (Labeled) dialogPane.lookupButton(btnXacNhan);
        if (btnXacNhanNode != null) {
            btnXacNhanNode.setStyle(PRIMARY_BTN_STYLE);
        }
        Labeled btnHuyNode = (Labeled) dialogPane.lookupButton(btnHuy);
        if (btnHuyNode != null) {
            btnHuyNode.setStyle(SECONDARY_BTN_STYLE);
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnXacNhan;
    }
}
