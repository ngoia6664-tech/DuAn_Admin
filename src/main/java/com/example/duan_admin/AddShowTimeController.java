package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 1. Class bọc thông tin Phim nhận về từ @GetMapping("/getMovies")
class MovieModel {
    Long id;
    String title; // Đổi từ name sang title cho khớp Backend

    public MovieModel(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}

// 2. Class bọc thông tin Phòng nhận về từ Database
class ShowRoomModel {
    Long id;
    String roomName; // SỬA: Đổi từ name sang roomName

    public ShowRoomModel(Long id, String roomName) {
        this.id = id;
        this.roomName = roomName;
    }
}

public class AddShowTimeController extends BaseController{

    @FXML private ComboBox<MovieModel> cmbMovie;
    @FXML private ComboBox<ShowRoomModel> cmbShowRoom;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;
    @FXML private TextField txtPrice;

    @FXML
    public void initialize() {
        // Cấu hình hiển thị tên trên ComboBox thay vì hiển thị mã hash của Object
        setupComboBoxConverters();

        // 🚀 Tự động kéo dữ liệu thật từ database ngay khi mở giao diện
        loadMoviesFromDatabase();
        loadShowRoomsFromDatabase();
    }

    private void loadMoviesFromDatabase() {
        String endpoint = "/api/feature/getMovies";

        HTTPService.sendFullRequestAsync("GET", endpoint, null, null, Session.getToken())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<MovieModel> movieList = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);

                            // 🌟 SỬA TẠI ĐÂY: Đổi từ "name" thành "title" để khớp với JSON Backend trả về!
                            Long id = obj.getLong("id");
                            String title = obj.getString("title");

                            movieList.add(new MovieModel(id, title));
                        }
                        Platform.runLater(() -> cmbMovie.getItems().setAll(movieList));
                    } else {
                        System.out.println("Lỗi gọi API phim, mã lỗi: " + response.statusCode());
                    }
                });
    }

    // 🌟 SỬA THÊM: Cập nhật hàm hiển thị ComboBox ở cuối file của bạn

    private void loadShowRoomsFromDatabase() {
        String endpoint = "/api/media/getShowRooms";

        HTTPService.sendFullRequestAsync("POST", endpoint, null, null, Session.getToken())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<ShowRoomModel> roomList = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);

                            // 🌟 QUAN TRỌNG: Lấy đúng tên thuộc tính trong ShowRoomDTO
                            Long id = obj.getLong("id");
                            String roomName = obj.getString("roomName");

                            roomList.add(new ShowRoomModel(id, roomName));
                        }
                        Platform.runLater(() -> cmbShowRoom.getItems().setAll(roomList));
                    } else {
                        System.out.println("Lỗi API: " + response.statusCode());
                    }
                });
    }

    @FXML
    private void handleSave() {
        try {
            MovieModel selectedMovie = cmbMovie.getValue();
            ShowRoomModel selectedRoom = cmbShowRoom.getValue();
            String priceStr = txtPrice.getText().trim();

            // 1. Lấy dữ liệu từ TextField
            String rawStart = txtStartTime.getText().trim();
            String rawEnd = txtEndTime.getText().trim();

            if (selectedMovie == null || selectedRoom == null || rawStart.isEmpty() || rawEnd.isEmpty() || priceStr.isEmpty()) {
                hienThongBao("Lỗi nhập liệu", "Vui lòng nhập đầy đủ thông tin!", AlertType.ERROR);
                return;
            }

            // 2. Xử lý định dạng thời gian cho Backend (Chuẩn ISO-8601: yyyy-MM-dd'T'HH:mm:ss)
            // Nếu người dùng nhập "2026-06-27 15:00", nó sẽ chuyển thành "2026-06-27T15:00:00"
            String startDateTime = rawStart.replace(" ", "T") + ":00";
            String endDateTime = rawEnd.replace(" ", "T") + ":00";

            // 3. Đóng gói JSON
            JSONObject json = new JSONObject();
            json.put("startTime", startDateTime); // Biến đã được định nghĩa ở trên
            json.put("endTime", endDateTime);
            json.put("price", Double.parseDouble(priceStr));
            json.put("movieID", selectedMovie.id);
            json.put("showRoomID", selectedRoom.id);

            String jsonBody = json.toString();
            System.out.println("JSON gửi đi: " + jsonBody);

            // 4. Gửi API
            String endpoint = "/api/feature/addShowTime";
            HTTPService.sendFullRequestAsync("POST", endpoint, null, jsonBody, Session.getToken())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                hienThongBao("Thành công", "Đã thêm suất chiếu!", AlertType.INFORMATION);
                                MainViewController.getInstance().hienTrangHome();
                            } else {
                                hienThongBao("Lỗi " + response.statusCode(), "Backend báo: " + response.body(), AlertType.ERROR);
                            }
                        });
                    })
                    .orTimeout(10, TimeUnit.SECONDS).exceptionally(ex -> {
                        Platform.runLater(() ->
                                hienThongBao("Mất kết nối", "Không thể kết nối tới Server!\nVui lòng kiểm tra lại kết nối.", AlertType.ERROR)
                        );
                        ex.printStackTrace();
                        return null;
                    });;


        } catch (NumberFormatException e) {
            hienThongBao("Lỗi", "Giá vé không hợp lệ!", AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            hienThongBao("Lỗi", "Có lỗi xảy ra: " + e.getMessage(), AlertType.ERROR);
        }
    }
    @FXML private void handleCancel() { MainViewController.getInstance().hienTrangHome(); }

    private void hienThongBao(String tieuDe, String noiDung, AlertType loaiThongBao) {
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

    private void setupComboBoxConverters() {
        cmbMovie.setConverter(new StringConverter<>() {
            @Override public String toString(MovieModel object) { return object == null ? "" : object.title; }
            @Override public MovieModel fromString(String string) { return null; }
        });
        cmbShowRoom.setConverter(new StringConverter<>() {
            @Override public String toString(ShowRoomModel object) { return object == null ? "" : object.roomName; }
            @Override public ShowRoomModel fromString(String string) { return null; }
        });
    }

    @Override
    public void Init() {

    }
}