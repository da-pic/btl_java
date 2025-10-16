package com.example.ql_shopcoffee.models;

import java.time.LocalDateTime;

public abstract class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor cho insert(không có id)
    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Constructor đầy đủ(query từ db)
    public User(int id, String username, String password, String fullName, String role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Abstract method
    public abstract boolean hasPermission(String permission);

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public String toString() {
        return String.format("User[id = %d, username = '%s', fullName = '%s', role = '%s']", id, username, fullName, role);
    }
}
