package com.example.ql_shopcoffee.models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Manager extends User{

    // Danh sách quyền của Manager
    private static final List<String> PERMISSIONS = Arrays.asList(
            // Employee permissions
            "VIEW_MENU",
            "CREATE_ORDER",
            "VIEW_OWN_ORDERS",
            // Manager exclusive permissions
            "MANAGE_PRODUCTS",
            "MANAGE_CATEGORIES",
            "MANAGE_USERS",
            "VIEW_ALL_ORDERS",
            "VIEW_REPORTS"
    );

    public Manager(String username, String password, String fullName) {
        super(username, password, fullName, "MANAGER");
    }

    public Manager(int id, String username, String password, String fullName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, username, password, fullName, "MANAGER", createdAt, updatedAt);
    }

    @Override
    public boolean hasPermission(String permission) {
        return PERMISSIONS.contains(permission);
    }

    // Method riêng của MANAGER
    public List<String> getPermissions() {
        return PERMISSIONS;
    }
    public boolean isAdmin() {
        return true;
    }
    public boolean canManagerUsers() {
        return hasPermission("MANAGE_USERS");
    }
    public boolean canManagerProducts() {
        return hasPermission("MANAGE_PRODUCTS");
    }

    @Override
    public String toString() {
        return String.format("Manager[id = %d, username = '%s', fullName = '%s]", getId(), getUsername(), getFullName());
    }
}
