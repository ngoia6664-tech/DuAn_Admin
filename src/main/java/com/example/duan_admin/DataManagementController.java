package com.example.duan_admin;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ===== MODEL CLASSES =====

class Movie {
    private Long id;
    private String title;
    private String genre;
    private String image;
    private LocalDate releaseDate;

    public Movie(Long id, String title, String genre, String image, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.image = image;
        this.releaseDate = releaseDate;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getImage() { return image; }
    public LocalDate getReleaseDate() { return releaseDate; }
}

class Cinema2 {
    private Long id;
    private String name;
    private String address;

    public Cinema2(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
}

class ShowRoom {
    private Long id;
    private String roomName;
    private int capacity;

    public ShowRoom(Long id, String roomName, int capacity) {
        this.id = id;
        this.roomName = roomName;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getRoomName() { return roomName; }
    public int getCapacity() { return capacity; }
}

class ShowTime {
    private Long id;
    private String cinemaName;
    private String address;
    private LocalDateTime startTime;

    public ShowTime(Long id, String cinemaName, String address, LocalDateTime startTime) {
        this.id = id;
        this.cinemaName = cinemaName;
        this.address = address;
        this.startTime = startTime;
    }

    public Long getId() { return id; }
    public String getCinemaName() { return cinemaName; }
    public String getAddress() { return address; }
    public LocalDateTime getStartTime() { return startTime; }
}

// ===== CONTROLLER =====

public class DataManagementController {

    @FXML private TableView tblData;
    @FXML private ToggleButton btnTabCinemas;
    @FXML private ToggleButton btnTabShowrooms;
    @FXML private ToggleButton btnTabShowtimes;
    @FXML private ToggleButton btnTabMovies;
    // Cột Phim
    @FXML private TableColumn<Movie, Long> colId;
    @FXML private TableColumn<Movie, String> colTitle;
    @FXML private TableColumn<Movie, String> colGenre;
    @FXML private TableColumn<Movie, String> colImage;
    @FXML private TableColumn<Movie, LocalDate> colReleaseDate;

    // Cột Rạp Chiếu
    @FXML private TableColumn<Cinema2, Long> colCinemaId;
    @FXML private TableColumn<Cinema2, String> colCinemaName;
    @FXML private TableColumn<Cinema2, String> colCinemaAddress;

    // Cột Phòng Chiếu
    @FXML private TableColumn<ShowRoom, Long> colRoomId;
    @FXML private TableColumn<ShowRoom, String> colRoomName;
    @FXML private TableColumn<ShowRoom, Integer> colCapacity;

    // Cột Suất Chiếu
    @FXML private TableColumn<ShowTime, Long> colShowTimeId;
    @FXML private TableColumn<ShowTime, String> colCinemaName2;
    @FXML private TableColumn<ShowTime, String> colAddress;
    @FXML private TableColumn<ShowTime, String> colStartTime;

    private String currentTab = "MOVIE";
    public void initialize() {
        // Tạo ToggleGroup để chỉ 1 nút được chọn tại 1 thời điểm
        ToggleGroup tabGroup = new ToggleGroup();
        btnTabCinemas.setToggleGroup(tabGroup);
        btnTabShowrooms.setToggleGroup(tabGroup);
        btnTabShowtimes.setToggleGroup(tabGroup);
        btnTabMovies.setToggleGroup(tabGroup);

        // Không cho bỏ chọn tất cả
        tabGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });

        // Mặc định chọn tab Bộ Phim
        btnTabMovies.setSelected(true);
        btnMovie();
    }

    // ===== RẠP CHIẾU =====
    @FXML
    private void btnCinema() {
        currentTab = "CINEMA";
        System.out.println("Đang lấy danh sách rạp chiếu...");

        HTTPService.sendFullRequestAsync("GET", "/api/media/getCinemas", null, null, null)
                .thenAccept(response -> {
                    System.out.println("Cinema status: " + response.statusCode());
                    System.out.println("Cinema body: " + response.body());
                    if (response.statusCode() == 200) {
                        List<Cinema2> list = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            try {
                                Long id = obj.getLong("id");
                                String name = obj.optString("name", "");
                                String address = obj.optString("address", "");
                                list.add(new Cinema2(id, name, address));
                            } catch (Exception e) {
                                System.err.println("Lỗi parse Cinema: " + e.getMessage());
                            }
                        }
                        Platform.runLater(() -> {
                            colCinemaId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
                            colCinemaName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
                            colCinemaAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));

                            tblData.getColumns().clear();
                            tblData.getColumns().addAll(colCinemaId, colCinemaName, colCinemaAddress);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        System.out.println("Lỗi API Cinema: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Lỗi kết nối Cinema: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    // ===== PHÒNG CHIẾU =====
    @FXML
    private void btnShowRoom() {
        currentTab = "SHOWROOM";
        System.out.println("Đang lấy danh sách phòng chiếu...");
        HTTPService.sendFullRequestAsync("POST", "/api/media/getShowRooms", null, null, null)
                .thenAccept(response -> {
                    System.out.println("ShowRoom status: " + response.statusCode());
                    System.out.println("ShowRoom body: " + response.body());
                    if (response.statusCode() == 200) {
                        List<ShowRoom> list = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            try {
                                Long id = obj.getLong("id");
                                String roomName = obj.optString("roomName", "");
                                int capacity = obj.optInt("capacity", 0);
                                list.add(new ShowRoom(id, roomName, capacity));
                            } catch (Exception e) {
                                System.err.println("Lỗi parse ShowRoom: " + e.getMessage());
                            }
                        }
                        Platform.runLater(() -> {
                            colRoomId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
                            colRoomName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomName()));
                            colCapacity.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCapacity()));

                            tblData.getColumns().clear();
                            tblData.getColumns().addAll(colRoomId, colRoomName, colCapacity);
                            tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        System.out.println("Lỗi API ShowRoom: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Lỗi kết nối ShowRoom: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    // ===== SUẤT CHIẾU =====
    @FXML
    private void btnShowTime() {
        currentTab = "SHOWTIME";
        System.out.println("Đang lấy danh sách suất chiếu...");

        String body = "{\"id\": 1}";

        HTTPService.sendFullRequestAsync("POST", "/api/feature/getShowTime", null, body, null)
                .thenAccept(response -> {
                    System.out.println("ShowTime status: " + response.statusCode());
                    System.out.println("ShowTime body: " + response.body());
                    if (response.statusCode() == 200) {
                        List<ShowTime> list = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            try {
                                Long id = obj.getLong("id");
                                String cinemaName = obj.optString("cinemaName", "");
                                String address = obj.optString("address", "");
                                String startTimeStr = obj.optString("startTime", "");
                                LocalDateTime startTime = startTimeStr.isEmpty() ? null : LocalDateTime.parse(startTimeStr);
                                list.add(new ShowTime(id, cinemaName, address, startTime));
                            } catch (Exception e) {
                                System.err.println("Lỗi parse ShowTime: " + e.getMessage());
                            }
                        }
                        Platform.runLater(() -> {
                            colShowTimeId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
                            colCinemaName2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCinemaName()));
                            colAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
                            colStartTime.setCellValueFactory(data -> new SimpleStringProperty(
                                    data.getValue().getStartTime() != null ? data.getValue().getStartTime().toString() : ""
                            ));

                            tblData.getColumns().clear();
                            tblData.getColumns().addAll(colShowTimeId, colCinemaName2, colAddress, colStartTime);
                            tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        System.out.println("Lỗi API ShowTime: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Lỗi kết nối ShowTime: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    // ===== BỘ PHIM =====
    @FXML
    private void btnMovie() {
        System.out.println("Đang lấy dữ liệu phim...");

        HTTPService.sendFullRequestAsync("GET", "/api/feature/getMovies", null, null, null)
                .thenAccept(response -> {
                    System.out.println("Movie status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        List<Movie> list = new ArrayList<>();
                        JSONArray arr = new JSONArray(response.body());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            try {
                                Long id = obj.getLong("id");
                                String title = obj.optString("title", "");
                                String genre = obj.optString("genre", "");
                                String image = obj.optString("image", "");
                                String dateStr = obj.optString("releaseDate", "");
                                LocalDate releaseDate = dateStr.isEmpty() ? null : LocalDate.parse(dateStr);
                                list.add(new Movie(id, title, genre, image, releaseDate));
                            } catch (Exception e) {
                                System.err.println("Lỗi parse Movie: " + e.getMessage());
                            }
                        }
                        Platform.runLater(() -> {
                            colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
                            colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
                            colGenre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));
                            colImage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImage()));
                            colReleaseDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getReleaseDate()));

                            tblData.getColumns().clear();
                            tblData.getColumns().addAll(colId, colTitle, colGenre, colImage, colReleaseDate);
                            tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        System.out.println("Lỗi API Movie: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Lỗi kết nối Movie: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }


    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}