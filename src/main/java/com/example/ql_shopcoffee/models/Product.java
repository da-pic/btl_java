package com.example.ql_shopcoffee.models;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String name;
    private int categoryId;
    private double price;
    private String image;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor cho insert
    public Product(String name, int categoryId, double price, String image) {
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.image = image;
        this.status = true;
    }

    // Constructor full
    public Product(int id, String name, int categoryId, double price, String image, boolean status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.image = image;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter
    public int getId() { return id; }
    public String getName() { return name; }
    public int getCategoryId() { return categoryId; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public boolean isStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setter
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setPrice(double price) { this.price = price; }
    public void setImage(String image) { this.image = image; }
    public void setStatus(boolean status) { this.status = status; }

    // Logic nghiệp vụ
    public boolean isAvailable() { return status; }

    public void activate() { this.status = true; }
    public void deactivate() { this.status = false; }

    public String getFormattedPrice() {
        return String.format("%, .0f đ", price);
    }

    @Override
    public String toString() {
        return String.format("Product[id = %d, name = '%s', price = %, .0f, status = %s]",
                id, name, price, status ? "Active" : "Inactive");
    }
}
