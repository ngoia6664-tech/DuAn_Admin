package com.example.duan_admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 1. Class bọc thông tin Phim
class MovieModel {
    Long id;
    String title;

    public MovieModel(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}

// 2. Class bọc thông tin Rạp chiếu
class CinemaModel {
    Long id;
    String name;

    public CinemaModel(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

// 3. Class bọc thông tin Phòng chiếu
class ShowRoomModel {
    Long id;
    String roomName;

    public ShowRoomModel(Long id, String roomName) {
        this.id = id;
        this.roomName = roomName;
    }
}

public class AddShowTimeController extends BaseController {

    @FXML private ComboBox<MovieModel> cmbMovie;
    @FXML private ComboBox<CinemaModel> cmbCinema;
    @FXML private ComboBox<ShowRoomModel> cmbShowRoom;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;
    @FXML private TextField txtPrice;

    @FXML
    public void initialize() {
        setupComboBoxConverters();

        loadMoviesFromDatabase();
        loadCinemasFromDatabase();

        // 🚀 Khi chọn rạp -> tự động load phòng chiếu thuộc rạp đó
        cmbCinema.valueProperty().addListener((obs, oldCinema, newCinema) -> {
            cmbShowRoom.getItems().clear();
            cmbShowRoom.setValue(null);

            if (newCinema != null) {
                cmbShowRoom.setDisable(false);
                cmbShowRoom.setPromptText("Đang tải phòng chiếu...");
                loadShowRoomsByCinema(newCinema.id);
            } else {
                cmbShowRoom.setDisable(true);
                cmbShowRoom.setPromptText("Vui lòng chọn rạp trước...");
            }
        });
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

    // 🌟 Load danh sách rạp chiếu
    private void loadCinemasFromDatabase() {
        String endpoint = "/api/media/getCinemas";

        HTTPService.sendFullRequestAsync("GET", endpoint, null, null, Session.getToken())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<CinemaModel> cinemaList = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Long id = obj.getLong("id");
                            String name = obj.getString("name");
                            cinemaList.add(new CinemaModel(id, name));
                        }
                        Platform.runLater(() -> cmbCinema.getItems().setAll(cinemaList));
                    } else {
                        System.out.println("Lỗi gọi API rạp chiếu, mã lỗi: " + response.statusCode());
                    }
                });
    }

    // 🌟 Load phòng chiếu THEO rạp đã chọn
    private void loadShowRoomsByCinema(Long cinemaId) {
        String endpoint = "/api/media/getShowRoomsByCinema/" + cinemaId;

        HTTPService.sendFullRequestAsync("GET", endpoint, null, null, Session.getToken())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        List<ShowRoomModel> roomList = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Long id = obj.getLong("id");
                            String roomName = obj.getString("roomName");
                            roomList.add(new ShowRoomModel(id, roomName));
                        }
                        Platform.runLater(() -> {
                            cmbShowRoom.getItems().setAll(roomList);
                            cmbShowRoom.setPromptText(roomList.isEmpty()
                                    ? "Rạp này chưa có phòng chiếu"
                                    : "Chọn phòng máy thuộc rạp đã chọn...");
                        });
                    } else {
                        System.out.println("Lỗi API phòng chiếu theo rạp: " + response.statusCode());
                        Platform.runLater(() -> cmbShowRoom.setPromptText("Lỗi tải danh sách phòng"));
                    }
                });
    }

    @FXML
    private void handleSave() {
        try {
            MovieModel selectedMovie = cmbMovie.getValue();
            CinemaModel selectedCinema = cmbCinema.getValue();
            ShowRoomModel selectedRoom = cmbShowRoom.getValue();
            String priceStr = txtPrice.getText().trim();

            String rawStart = txtStartTime.getText().trim();
            String rawEnd = txtEndTime.getText().trim();

            if (selectedMovie == null || selectedCinema == null || selectedRoom == null
                    || rawStart.isEmpty() || rawEnd.isEmpty() || priceStr.isEmpty()) {
                DialogUtils.showThongBao("Lỗi nhập liệu", "Vui lòng nhập đầy đủ thông tin!", AlertType.ERROR);
                return;
            }

            String startDateTime = rawStart.replace(" ", "T") + ":00";
            String endDateTime = rawEnd.replace(" ", "T") + ":00";

            JSONObject json = new JSONObject();
            json.put("startTime", startDateTime);
            json.put("endTime", endDateTime);
            json.put("price", Double.parseDouble(priceStr));
            json.put("movieID", selectedMovie.id);
            json.put("showRoomID", selectedRoom.id);

            String jsonBody = json.toString();
            System.out.println("JSON gửi đi: " + jsonBody);

            String endpoint = "/api/feature/addShowTime";
            HTTPService.sendFullRequestAsync("POST", endpoint, null, jsonBody, Session.getToken())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                DialogUtils.showThongBao("Thành công", "Đã thêm suất chiếu!", AlertType.INFORMATION);
                                MainViewController.getInstance().hienTrangHome();
                            } else {
                                DialogUtils.showThongBao("Lỗi " + response.statusCode(), "Backend báo: " + response.body(), AlertType.ERROR);
                            }
                        });
                    })
                    .orTimeout(10, TimeUnit.SECONDS).exceptionally(ex -> {
                        Platform.runLater(() ->
                                DialogUtils.showThongBao("Mất kết nối", "Không thể kết nối tới Server!\nVui lòng kiểm tra lại kết nối.", AlertType.ERROR)
                        );
                        ex.printStackTrace();
                        return null;
                    });

        } catch (NumberFormatException e) {
            DialogUtils.showThongBao("Lỗi", "Giá vé không hợp lệ!", AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showThongBao("Lỗi", "Có lỗi xảy ra: " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML private void handleCancel() { MainViewController.getInstance().hienTrangHome(); }

    private void setupComboBoxConverters() {
        cmbMovie.setConverter(new StringConverter<>() {
            @Override public String toString(MovieModel object) { return object == null ? "" : object.title; }
            @Override public MovieModel fromString(String string) { return null; }
        });
        cmbCinema.setConverter(new StringConverter<>() {
            @Override public String toString(CinemaModel object) { return object == null ? "" : object.name; }
            @Override public CinemaModel fromString(String string) { return null; }
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
