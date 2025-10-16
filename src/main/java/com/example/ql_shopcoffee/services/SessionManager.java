package com.example.ql_shopcoffee.services;

import com.example.ql_shopcoffee.models.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Đăng nhập user
     */
    public void login(User user) {
        this.currentUser = user;
        System.out.println("User logged in: " + user.getUsername());
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        if(currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Lấy User hiện tại
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra đã login chưa
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Kiểm tra user có phải Manager không
     */
    public boolean isManager() {
        return currentUser != null && "MANAGER".equals(currentUser.getRole());
    }

    /**
     * Kiểm tra User có phải Employee không
     */
    public boolean isEmployee() {
        return currentUser != null && "EMPLOYEE".equals(currentUser.getRole());
    }

    /**
     * Kiểm tra user có quyền ko
     */
    public boolean hasPermission(String permission) {
        return currentUser != null && currentUser.hasPermission(permission);
    }

    /**
     * Lấy id hiện tại
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    /**
     * Lấy username hiện tại
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    /**
     * Lấy tên
     */
    public String getCurrentFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
}