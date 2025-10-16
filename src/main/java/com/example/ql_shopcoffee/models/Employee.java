package com.example.ql_shopcoffee.models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Employee extends User {

    // Danh sách quyền của Employee
    private static final List<String> PERMISSION = Arrays.asList(
            "VIEW_MENU",
            "CREATE_ORDER",
            "VIEW_OWN_ORDERS"
    );

    public Employee(String username, String password, String fullName) {
        super(username, password, fullName, "EMPLOYEE");
    }

    public Employee(int id, String username, String password, String fullName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id,username, password, fullName, "EMPLOYEE",createdAt, updatedAt);
    }

    @Override
    public boolean hasPermission(String permission) {
        return PERMISSION.contains(permission);
    }

    // Method riêng của EMPLOYEE
    public List<String> getPermission() {
        return PERMISSION;
    }
    public boolean canCreateOrder() {
        return hasPermission("CREATE_ORDER");
    }

    @Override
    public String toString() {
        return String.format("Employee[id = %d, username = '%s', fullName = '%s]", getId(), getUsername(), getFullName());
    }
}
