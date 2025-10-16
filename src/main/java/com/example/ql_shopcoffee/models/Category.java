package com.example.ql_shopcoffee.models;

import java.time.LocalDateTime;

public class Category {
    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    // Constructor cho insert
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Constructor full
    public Category(int id, String name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getter
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setter
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}
