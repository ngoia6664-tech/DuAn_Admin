package com.example.duan_admin;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class HTTPService {

    private static String BASE_URL;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    static {
        try (InputStream input = HTTPService.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                BASE_URL = "http://localhost:8080";
            } else {
                prop.load(input);
                BASE_URL = prop.getProperty("base.url");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param params Map chứa các tham số query (vd: Map.of("id", "1", "name", "test"))
     */
    public static CompletableFuture<String> sendRequestAsync(
            String method, 
            String endpoint, 
            Map<String, String> params, // Thêm tham số này
            String jsonBody, 
            String token) {

        // 1. Xử lý nối Param vào URL
        StringBuilder urlBuilder = new StringBuilder(BASE_URL).append(endpoint);
        if (params != null && !params.isEmpty()) {
            StringJoiner joiner = new StringJoiner("&", "?", "");
            params.forEach((k, v) -> {
                String encodedValue = URLEncoder.encode(v, StandardCharsets.UTF_8);
                joiner.add(k + "=" + encodedValue);
            });
            urlBuilder.append(joiner.toString());
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Accept", "application/json");

        // 2. Thêm Token
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        // 3. Xử lý Method
        if ("POST".equalsIgnoreCase(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : ""));
        } else if ("PUT".equalsIgnoreCase(method)) {
            builder.header("Content-Type", "application/json");
            builder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : ""));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.DELETE();
        } else {
            builder.GET();
        }

        return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

  

public static CompletableFuture<HttpResponse<String>> sendFullRequestAsync(
        String method, 
        String endpoint, 
        Map<String, Object> params, // ĐÃ ĐỔI: Từ Map<String, String> sang Map<String, Object>
        String jsonBody, 
        String token) {

    // 1. Xử lý nối Param vào URL (Đã tối ưu để nhận mọi kiểu dữ liệu)
    StringBuilder urlBuilder = new StringBuilder(BASE_URL).append(endpoint);
    if (params != null && !params.isEmpty()) {
        StringJoiner joiner = new StringJoiner("&", "?", "");
        
        params.forEach((k, v) -> {
            // Kiểm tra null để tránh lỗi NullPointerException nếu value truyền vào bị rỗng
            if (v != null) {
                // v.toString() sẽ tự động chuyển Integer, Long, Boolean... thành String thích hợp
                String encodedValue = URLEncoder.encode(v.toString(), StandardCharsets.UTF_8);
                joiner.add(k + "=" + encodedValue);
            }
        });
        urlBuilder.append(joiner.toString());
    }

    HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(urlBuilder.toString()))
            .header("Accept", "application/json");

    // 2. Thêm Token
    if (token != null && !token.isBlank()) {
        builder.header("Authorization", "Bearer " + token);
    }

    // 3. Xử lý Method
    if ("POST".equalsIgnoreCase(method)) {
        builder.header("Content-Type", "application/json");
        builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : ""));
    } else if ("PUT".equalsIgnoreCase(method)) {
        builder.header("Content-Type", "application/json");
        builder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : ""));
    } else if ("DELETE".equalsIgnoreCase(method)) {
        builder.DELETE();
    } else {
        builder.GET();
    }

    // 4. Trả về CompletableFuture chứa toàn bộ HttpResponse
    return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
}
}