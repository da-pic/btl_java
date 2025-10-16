package com.example.ql_shopcoffee.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int employeeId;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;
    private String note;
    private List<OrderDetail>  orderDetails;

    public Order(int employeeId) {
        this.employeeId = employeeId;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = 0.0;
        this.status = "PENDING";
        this.orderDetails = new ArrayList<>();
    }

    public Order(int id, int employeeId, LocalDateTime orderDate, double totalAmount, String status, String note) {
        this.id = id;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.note = note;
        this.orderDetails = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
    public List<OrderDetail> getOrderDetails() { return orderDetails; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setNote(String note) { this.note = note; }
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        recalculateTotal();
    }

    // Logic nghiệp vụ
    public void addItem(OrderDetail orderDetail) {
        for(OrderDetail item : orderDetails) {
            if(item.getProductId() == orderDetail.getProductId()) {
                item.setQuantity(item.getQuantity() + orderDetail.getQuantity());
                recalculateTotal();
                return;
            }
        }
        orderDetails.add(orderDetail);
        recalculateTotal();
    }

    public void removeItem(OrderDetail orderDetail) {
        orderDetails.removeIf(item -> item.getProductId() == orderDetail.getProductId());
        recalculateTotal();
    }

    public void updateItemQuantity(int productId, int quantity) {
        for(OrderDetail item : orderDetails) {
            if(item.getProductId() == productId) {
                item.setQuantity(quantity);
                recalculateTotal();
                return;
            }
        }
    }

    public void recalculateTotal() {
        this.totalAmount = orderDetails.stream().mapToDouble(OrderDetail::getSubtotal).sum();
    }

    public void complete() {
        this.status = "COMPLETED";
    }
    public void cancel() {
        this.status = "CANCELLED";
    }

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
    public boolean isCancelled() {
        return "CANCELLED".equals(this.status);
    }

    public int getItemCount() {
        return orderDetails.stream().mapToInt(OrderDetail::getQuantity).sum();
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return orderDate.format(formatter);
    }

    public String getFormattedTotal() {
        return String.format("%,.0f đ", totalAmount);
    }

    public String toString() {
        return String.format("Order[id = %d, date = %s, total = %,.0f, status = %s, items = %d]",
                id, getFormattedDate(), totalAmount, status, orderDetails.size());
    }
}
