package com.example.duan_admin;

/**
 * Lớp lưu trữ trạng thái đăng nhập (token) dùng chung cho toàn bộ ứng dụng.
 * Vì các trường là static nên mọi Controller đều có thể truy cập trực tiếp
 * mà không cần truyền tham chiếu qua lại giữa các màn hình.
 *
 * Cách dùng:
 *   - Sau khi đăng nhập thành công: Session.setToken(token);
 *   - Ở bất kỳ controller nào cần gọi API: String token = Session.getToken();
 *   - Khi đăng xuất: Session.clear();
 */
public class Session {

    private static String token;
    private static String username;
    private static Long userId; // dùng Long (wrapper) để có thể null khi chưa có id

    private Session() {
        // Không cho khởi tạo instance, chỉ dùng static
    }

    public static void setToken(String newToken) {
        token = newToken;
    }

    public static String getToken() {
        return token;
    }

    public static void setUsername(String newUsername) {
        username = newUsername;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUserId(Long id) {
        userId = id;
    }

    public static Long getUserId() {
        return userId;
    }

    /** Kiểm tra đã đăng nhập hay chưa (đã có token) */
    public static boolean isLoggedIn() {
        return token != null && !token.isBlank();
    }

    /** Xoá toàn bộ thông tin phiên đăng nhập, gọi khi logout */
    public static void clear() {
        token = null;
        username = null;
        userId = null;
    }
}