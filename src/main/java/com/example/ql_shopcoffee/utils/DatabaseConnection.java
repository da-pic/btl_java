package com.example.ql_shopcoffee.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class để quản lý database connection
 * Chức năng: tạo vào quản lí connection
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String dbURL = "jdbc:sqlite:cf.db";

    private DatabaseConnection() {
        try {
            // Tải driver JDBC
            Class.forName("org.sqlite.JDBC");

            // Tạo connection
            connection = DriverManager.getConnection(dbURL);

            // Bật khóa phụ
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");

            System.out.println(" Kết nối cơ sở dữ liệu đã được thiết lập thành công. ");

        } catch (ClassNotFoundException e) {
            System.err.println(" Không tìm thấy driver JDBC của SQLite");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Không thể kết nối đến cơ sở dữ liệu.");
            e.printStackTrace();
        }
    }

    /**
     * Lấy instance duy nhất
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Lấy connection object
     * Tự động tạo lại nếu connection bị đóng
     */
    public Connection getConnection() {
        try {
            if(connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbURL);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Kiểm tra connection có đang hoạt động không
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Đóng connection khi ứng dụng kết thúc
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
