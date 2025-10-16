package com.example.ql_shopcoffee.services;

import com.example.ql_shopcoffee.dao.impl.UserDAO;
import com.example.ql_shopcoffee.dao.interfaces.IUserDAO;
import com.example.ql_shopcoffee.models.User;

public class AuthService {
    private final IUserDAO userDAO;
    private final SessionManager sessionManager;
    public AuthService(IUserDAO userDAO, SessionManager sessionManager) {
        this.userDAO = userDAO;
        this.sessionManager = sessionManager;
    }
    public AuthService() {
        this.userDAO = new UserDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Đăng nhập
     * @return User nếu thành công, null nếu thất bại
     */
    public User login(String username, String password) {
        if(username == null || username.trim().isEmpty()) {
            System.err.println("Username không được để trống");
            return null;
        }

        if(password == null || password.trim().isEmpty()) {
            System.err.println("Password không được để trống");
            return null;
        }

        User user = userDAO.findByUsername(username.trim());

        if(user == null) {
            System.err.println("Username không tồn tại");
        }

        if (!user.getPassword().equals(password)) {
            System.err.println("Password không đúng");
            return null;
        }

        System.out.println("Đăng nhập thành công: " + user.getFullName() + " (" + user.getRole() + ")");

        return user;
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        sessionManager.logout();
    }

    /**
     * Lấy user hiện tại
     */
    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    /**
     * Kiểm tra đã login chưa
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Kiểm tra user có quyền Manager không
     */
    public boolean isManager() {
        return sessionManager.isManager();
    }

    /**
     * Kiểm tra user có quyền Employee không
     */
    public boolean isEmployee() {
        return sessionManager.isEmployee();
    }

    /**
     * Kiểm tra permission
     */
    public boolean hasPermission(String permission) {
        return sessionManager.hasPermission(permission);
    }

    /**
     * Đổi password
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        User currentUser = sessionManager.getCurrentUser();

        if(currentUser == null) {
            System.err.println("Chưa đăng nhập");
            return false;
        }

        if(newPassword == null || newPassword.length() < 3) {
            System.err.println("Password mới phải có ít nhất 3 ký tự");
            return false;
        }

        if (!currentUser.getPassword().equals(oldPassword)) {
            System.err.println("Password cũ không đúng");
            return false;
        }

        boolean updated = userDAO.changePassword(currentUser.getId(), newPassword);

        if (updated) {
            currentUser.setPassword(newPassword);
            System.out.println("Đổi password thành công");
        }

        return updated;
    }

    /**
     * Validate username (dùng khi register)
     */
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().length() < 3) {
            return false;
        }
        return !userDAO.usernameExists(username.trim());
    }
}
