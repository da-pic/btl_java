package com.example.ql_shopcoffee.services;

import com.example.ql_shopcoffee.dao.impl.OrderDAO;
import com.example.ql_shopcoffee.dao.impl.ProductDAO;
import com.example.ql_shopcoffee.dao.interfaces.IOrderDAO;
import com.example.ql_shopcoffee.dao.interfaces.IProductDAO;
import com.example.ql_shopcoffee.models.Order;
import com.example.ql_shopcoffee.models.OrderDetail;
import com.example.ql_shopcoffee.models.Product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderService {

    private final IOrderDAO orderDAO;
    private final IProductDAO productDAO;
    private final SessionManager sessionManager;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    // Constructor cho testing
    public OrderService(IOrderDAO orderDAO, IProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.sessionManager = SessionManager.getInstance();
    }

    // ========== ORDER CREATION & MANAGEMENT ==========

    /**
     * Tạo order mới (trạng thái PENDING)
     * Phải login mới tạo được
     */
    public Order createNewOrder() {
        if (!sessionManager.isLoggedIn()) {
            System.err.println("Phải đăng nhập để tạo order");
            return null;
        }

        if (!sessionManager.hasPermission("CREATE_ORDER")) {
            System.err.println("Bạn không có quyền tạo order");
            return null;
        }

        int employeeId = sessionManager.getCurrentUserId();
        Order order = new Order(employeeId);

        System.out.println("Tạo order mới thành công cho " + sessionManager.getCurrentFullName());
        return order;
    }

    /**
     * Thêm sản phẩm vào order
     */
    public boolean addProductToOrder(Order order, int productId, int quantity) {
        // Validation
        if (order == null) {
            System.err.println("Order không hợp lệ");
            return false;
        }

        if (productId <= 0) {
            System.err.println("Product ID không hợp lệ");
            return false;
        }

        if (quantity <= 0) {
            System.err.println("Số lượng phải lớn hơn 0");
            return false;
        }

        // Kiểm tra product có tồn tại và active không
        Product product = productDAO.findById(productId);
        if (product == null) {
            System.err.println("Sản phẩm không tồn tại");
            return false;
        }

        if (!product.isAvailable()) {
            System.err.println("Sản phẩm không còn bán");
            return false;
        }

        // Tạo OrderDetail
        OrderDetail detail = new OrderDetail(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice()
        );

        // Thêm vào order
        order.addItem(detail);

        System.out.println("Đã thêm " + quantity + "x " + product.getName() +
                " vào order (Subtotal: " + detail.getFormattedSubtotal() + ")");
        return true;
    }

    /**
     * Xóa sản phẩm khỏi order
     */
    public boolean removeProductFromOrder(Order order, int productId) {
        if (order == null) {
            System.err.println("Order không hợp lệ");
            return false;
        }

        if (productId <= 0) {
            System.err.println("Product ID không hợp lệ");
            return false;
        }

        // Tìm OrderDetail có productId tương ứng
        OrderDetail detailToRemove = null;
        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getProductId() == productId) {
                detailToRemove = detail;
                break;
            }
        }

        if (detailToRemove == null) {
            System.err.println("Sản phẩm không có trong order");
            return false;
        }

        // Xóa OrderDetail
        order.removeItem(detailToRemove);
        System.out.println("Đã xóa sản phẩm khỏi order");
        return true;
    }

    /**
     * Cập nhật số lượng sản phẩm trong order
     */
    public boolean updateProductQuantity(Order order, int productId, int newQuantity) {
        if (order == null) {
            System.err.println("Order không hợp lệ");
            return false;
        }

        if (newQuantity <= 0) {
            System.err.println("Số lượng phải lớn hơn 0");
            return false;
        }

        order.updateItemQuantity(productId, newQuantity);
        System.out.println("Đã cập nhật số lượng");
        return true;
    }

    /**
     * Thanh toán order (lưu vào database)
     */
    public boolean checkout(Order order) {
        return checkout(order, null);
    }

    /**
     * Thanh toán order với note
     */
    public boolean checkout(Order order, String note) {
        // Validation
        if (order == null) {
            System.err.println("Order không hợp lệ");
            return false;
        }

        if (order.getOrderDetails().isEmpty()) {
            System.err.println("Order không có sản phẩm nào");
            return false;
        }

        if (!sessionManager.isLoggedIn()) {
            System.err.println("Phải đăng nhập để thanh toán");
            return false;
        }

        // Set note nếu có
        if (note != null && !note.trim().isEmpty()) {
            order.setNote(note.trim());
        }

        // Set status = COMPLETED
        order.complete();

        // Tính lại tổng tiền
        order.recalculateTotal();

        // Lưu vào database
        boolean result = orderDAO.insert(order);

        if (result) {
            System.out.println("  Thanh toán thành công!");
            System.out.println("  Order ID: " + order.getId());
            System.out.println("  Tổng tiền: " + order.getFormattedTotal());
            System.out.println("  Số items: " + order.getItemCount());
        } else {
            System.err.println("✗ Thanh toán thất bại!");
        }

        return result;
    }

    /**
     * Hủy order (chỉ với order đã lưu)
     */
    public boolean cancelOrder(int orderId) {
        if (!sessionManager.isLoggedIn()) {
            System.err.println("Phải đăng nhập");
            return false;
        }

        Order order = orderDAO.findById(orderId);
        if (order == null) {
            System.err.println("Order không tồn tại");
            return false;
        }

        // Chỉ Manager hoặc chính nhân viên tạo order mới được hủy
        if (!sessionManager.isManager() &&
                order.getEmployeeId() != sessionManager.getCurrentUserId()) {
            System.err.println("Bạn không có quyền hủy order này");
            return false;
        }

        order.cancel();
        boolean result = orderDAO.update(order);

        if (result) {
            System.out.println("Đã hủy order #" + orderId);
        }

        return result;
    }

    // ========== ORDER RETRIEVAL ==========

    /**
     * Lấy order theo ID
     */
    public Order getOrderById(int id) {
        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return null;
        }

        Order order = orderDAO.findById(id);

        // Kiểm tra quyền xem
        if (order != null && !sessionManager.isManager()) {
            // Employee chỉ xem được order của mình
            if (order.getEmployeeId() != sessionManager.getCurrentUserId()) {
                System.err.println("Bạn không có quyền xem order này");
                return null;
            }
        }

        return order;
    }

    /**
     * Lấy tất cả orders (CHỈ Manager)
     */
    public List<Order> getAllOrders() {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới xem được tất cả orders");
            return List.of();
        }
        return orderDAO.findAll();
    }

    /**
     * Lấy orders của user hiện tại
     */
    public List<Order> getMyOrders() {
        if (!sessionManager.isLoggedIn()) {
            System.err.println("Phải đăng nhập");
            return List.of();
        }

        int employeeId = sessionManager.getCurrentUserId();
        return orderDAO.findByEmployee(employeeId);
    }

    /**
     * Lấy orders của một nhân viên cụ thể (CHỈ Manager)
     */
    public List<Order> getOrdersByEmployee(int employeeId) {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới xem được orders của nhân viên khác");
            return List.of();
        }

        return orderDAO.findByEmployee(employeeId);
    }

    /**
     * Lấy orders hôm nay
     */
    public List<Order> getTodayOrders() {
        if (sessionManager.isManager()) {
            return orderDAO.findByDate(LocalDate.now());
        } else {
            // Employee chỉ xem của mình
            List<Order> myOrders = getMyOrders();
            LocalDate today = LocalDate.now();
            return myOrders.stream()
                    .filter(o -> o.getOrderDate().toLocalDate().equals(today))
                    .toList();
        }
    }

    /**
     * Lấy orders theo khoảng thời gian (CHỈ Manager)
     */
    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới xem được reports");
            return List.of();
        }

        return orderDAO.findByDateRange(start, end);
    }

    /**
     * Lấy orders theo status
     */
    public List<Order> getOrdersByStatus(String status) {
        if (!sessionManager.isManager()) {
            // Employee chỉ xem của mình
            List<Order> myOrders = getMyOrders();
            return myOrders.stream()
                    .filter(o -> status.equals(o.getStatus()))
                    .toList();
        }

        return orderDAO.findByStatus(status);
    }

    // ========== STATISTICS ==========

    /**
     * Đếm số orders hôm nay
     */
    public int getTodayOrderCount() {
        if (sessionManager.isManager()) {
            return orderDAO.countTodayOrders();
        } else {
            return getTodayOrders().size();
        }
    }

    /**
     * Tính doanh thu hôm nay (CHỈ Manager)
     */
    public double getTodayRevenue() {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới xem được doanh thu");
            return 0.0;
        }

        return orderDAO.getTodayRevenue();
    }

    /**
     * Tính doanh thu theo khoảng thời gian (CHỈ Manager)
     */
    public double getRevenueByDateRange(LocalDateTime start, LocalDateTime end) {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới xem được doanh thu");
            return 0.0;
        }

        return orderDAO.getRevenueByDateRange(start, end);
    }

    /**
     * Lấy top sản phẩm bán chạy (CHỈ Manager)
     */
    public List<Object[]> getTopSellingProducts(int limit) {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới xem được thống kê");
            return List.of();
        }

        return orderDAO.getTopSellingProducts(limit);
    }

    // ========== HELPER METHODS ==========

    /**
     * Validate order trước khi checkout
     */
    public String validateOrder(Order order) {
        if (order == null) {
            return "Order không hợp lệ";
        }

        if (order.getOrderDetails().isEmpty()) {
            return "Order không có sản phẩm nào";
        }

        if (order.getTotalAmount() <= 0) {
            return "Tổng tiền không hợp lệ";
        }

        // Kiểm tra tất cả products vẫn còn available
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = productDAO.findById(detail.getProductId());
            if (product == null || !product.isAvailable()) {
                return "Sản phẩm '" + detail.getProductName() + "' không còn bán";
            }
        }

        return null; // No errors
    }

    /**
     * Tính tổng items trong order
     */
    public int getOrderItemCount(Order order) {
        if (order == null) {
            return 0;
        }
        return order.getItemCount();
    }

    /**
     * Format order summary
     */
    public String getOrderSummary(Order order) {
        if (order == null) {
            return "Order không hợp lệ";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Order #").append(order.getId()).append("\n");
        summary.append("Ngày: ").append(order.getFormattedDate()).append("\n");
        summary.append("Nhân viên: ").append(sessionManager.getCurrentFullName()).append("\n");
        summary.append("Trạng thái: ").append(order.getStatus()).append("\n");
        summary.append("---\n");

        for (OrderDetail detail : order.getOrderDetails()) {
            summary.append(String.format("%dx %s - %s\n",
                    detail.getQuantity(),
                    detail.getProductName(),
                    detail.getFormattedSubtotal()
            ));
        }

        summary.append("---\n");
        summary.append("Tổng cộng: ").append(order.getFormattedTotal());

        return summary.toString();
    }
}
