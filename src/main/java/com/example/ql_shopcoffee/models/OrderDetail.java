package com.example.ql_shopcoffee.models;

public class OrderDetail {
    private int id;
    private int orderId;
    private int productId;
    private String productName;
    private int quantity;
    private double price;

    public OrderDetail(int productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderDetail(int id, int orderId, int productId, String productName, int quantity, double price) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }

    // Logic nghiệp vụ
    public double getSubtotal() {
        return price * quantity;
    }

    public String getFormattedPrice() {
        return String.format("%,.0f đ", price);
    }

    public String getFormattedSubtotal() {
        return String.format("%,.0f đ", getSubtotal());
    }

    @Override
    public String toString() {
        return String.format("OrderDetail[product='%s', qty=%d, price=%,.0f, subtotal=%,.0f]",
                productName, quantity, price, getSubtotal());
    }
}
