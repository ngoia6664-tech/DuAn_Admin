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
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private String movieName;
    private String cinemaName;
    private String address;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double price;

    public ShowTime(Long id, String movieName, String cinemaName, String address,
                    LocalDateTime startTime, LocalDateTime endTime, double price) {
        this.id = id;
        this.movieName = movieName;
        this.cinemaName = cinemaName;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getMovieName() { return movieName; }
    public String getCinemaName() { return cinemaName; }
    public String getAddress() { return address; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getPrice() { return price; }
}

// ===== CONTROLLER =====

public class DataManagementController extends BaseController {

    // Định dạng ngày giờ dễ đọc: 15/01/2027 19:00
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    @FXML private TableColumn<ShowTime, String> colMovieName;
    @FXML private TableColumn<ShowTime, String> colCinemaName2;
    @FXML private TableColumn<ShowTime, String> colAddress;
    @FXML private TableColumn<ShowTime, String> colStartTime;
    @FXML private TableColumn<ShowTime, String> colEndTime;
    @FXML private TableColumn<ShowTime, String> colPrice;


    public void initialize() {

    }

    // ===== RẠP CHIẾU =====
    @FXML
    private void btnCinema() {
        LoadingOverlayManager.start(btnTabCinemas);

        HTTPService.sendFullRequestAsync("GET", "/api/media/getCinemas", null, null, Session.getToken())
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
                            LoadingOverlayManager.stop();
                            tblData.getColumns().addAll(colCinemaId, colCinemaName, colCinemaAddress);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        LoadingOverlayManager.stop();
                        System.out.println("Lỗi API Cinema: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    LoadingOverlayManager.stop();
                    System.err.println("Lỗi kết nối Cinema: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    // ===== PHÒNG CHIẾU =====
    @FXML
    private void btnShowRoom() {
        LoadingOverlayManager.start(btnTabShowrooms);
        HTTPService.sendFullRequestAsync("POST", "/api/media/getShowRooms", null, null, Session.getToken())
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
                            LoadingOverlayManager.stop();
                            tblData.getColumns().addAll(colRoomId, colRoomName, colCapacity);
                            tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        LoadingOverlayManager.stop();
                        System.out.println("Lỗi API ShowRoom: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    LoadingOverlayManager.stop();
                    System.err.println("Lỗi kết nối ShowRoom: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    // ===== SUẤT CHIẾU =====
    @FXML
    private void btnShowTime() {
        LoadingOverlayManager.start(btnTabShowtimes);
        System.out.println("Đang lấy danh sách suất chiếu...");

        String body = "{\"id\": 1}";

        HTTPService.sendFullRequestAsync("GET", "/api/feature/GetShowTimeAdmin", null, body, Session.getToken())
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
                                String movieName = obj.optString("movieName", "");
                                String cinemaName = obj.optString("cinemaName", "");
                                String address = obj.optString("address", "");

                                String startTimeStr = obj.optString("startTime", "");
                                LocalDateTime startTime = startTimeStr.isEmpty() ? null : LocalDateTime.parse(startTimeStr);

                                String endTimeStr = obj.optString("endTime", "");
                                LocalDateTime endTime = endTimeStr.isEmpty() ? null : LocalDateTime.parse(endTimeStr);

                                double price = obj.optDouble("price", 0);

                                list.add(new ShowTime(id, movieName, cinemaName, address, startTime, endTime, price));
                            } catch (Exception e) {
                                System.err.println("Lỗi parse ShowTime: " + e.getMessage());
                            }
                        }
                        Platform.runLater(() -> {
                            colShowTimeId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
                            colMovieName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMovieName()));
                            colCinemaName2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCinemaName()));
                            colAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));

                            colStartTime.setCellValueFactory(data -> new SimpleStringProperty(
                                    formatDateTime(data.getValue().getStartTime())
                            ));
                            colEndTime.setCellValueFactory(data -> new SimpleStringProperty(
                                    formatDateTime(data.getValue().getEndTime())
                            ));
                            colPrice.setCellValueFactory(data -> new SimpleStringProperty(
                                    formatPrice(data.getValue().getPrice())
                            ));

                            tblData.getColumns().clear();
                            LoadingOverlayManager.stop();
                            tblData.getColumns().addAll(colShowTimeId, colMovieName, colCinemaName2,
                                    colAddress, colStartTime, colEndTime, colPrice);
                            tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        LoadingOverlayManager.stop();
                        System.out.println("Lỗi API ShowTime: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    LoadingOverlayManager.stop();
                    System.err.println("Lỗi kết nối ShowTime: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    // Định dạng LocalDateTime -> "dd/MM/yyyy HH:mm", trả về "" nếu null
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    // Định dạng giá vé -> "95.000 đ"
    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " đ";
    }

    // ===== BỘ PHIM =====
    @FXML
    private void btnMovie() {
        LoadingOverlayManager.start(btnTabMovies);
        HTTPService.sendFullRequestAsync("GET", "/api/feature/getMovies", null, null, Session.getToken())
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
                            LoadingOverlayManager.stop();
                            tblData.getColumns().addAll(colId, colTitle, colGenre, colImage, colReleaseDate);
                            tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                            tblData.getItems().setAll(list);
                        });
                    } else {
                        LoadingOverlayManager.stop();
                        System.out.println("Lỗi API Movie: " + response.statusCode());
                    }
                })
                .orTimeout(10, TimeUnit.SECONDS).exceptionally(ex -> {
                    LoadingOverlayManager.stop();
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

    @Override
    public void Init() {
        ToggleGroup tabGroup = new ToggleGroup();
        btnTabCinemas.setToggleGroup(tabGroup);
        btnTabShowrooms.setToggleGroup(tabGroup);
        btnTabShowtimes.setToggleGroup(tabGroup);
        btnTabMovies.setToggleGroup(tabGroup);

        tabGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });

        LoadingOverlayManager.start(btnTabShowtimes);
        btnMovie();

    }
}