package com.example.duan_admin;

public class AdminSession {

    private static Long adminId;

    public static Long getAdminId() {
        return adminId;
    }

    public static void setAdminId(Long adminId) {
        AdminSession.adminId = adminId;
    }

    public static void clear() {
        adminId = null;
    }
}