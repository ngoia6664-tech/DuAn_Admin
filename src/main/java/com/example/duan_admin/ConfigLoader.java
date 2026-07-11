package com.example.duan_admin;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();
    private static String baseUrl = null;

    // Khối static chạy một lần duy nhất khi ứng dụng nạp Class để đọc file config
    static {
        // Tìm file config.properties trong thư mục resources
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties. Sử dụng giá trị mặc định!");
                // Giá trị fallback dự phòng nếu quên không tạo file hoặc sai đường dẫn
                baseUrl = "http://localhost:8080";
            } else {
                // Tải dữ liệu từ file vào đối tượng properties
                properties.load(input);
                // Lấy giá trị từ key "base.url", nếu không có sẽ lấy giá trị mặc định ở vế sau
                baseUrl = properties.getProperty("base.url", "http://localhost:8080");
            }
        } catch (IOException ex) {
            System.err.println("Lỗi khi đọc file config.properties: " + ex.getMessage());
            baseUrl = "http://localhost:8080";
            ex.printStackTrace();
            System.out.println(baseUrl+"SIBAY DUY ANH TUAT");

        }
        System.out.println(baseUrl+"SIBAY DUY ANH TUAT");
    }

    /**
     * Hàm static lấy Base URL từ cấu hình hệ thống
     * @return String chứa địa chỉ URL gốc của Server Backend
     */
    public static String getBaseUrl() {
        return baseUrl;
    }
}