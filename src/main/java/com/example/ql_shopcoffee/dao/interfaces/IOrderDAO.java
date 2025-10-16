package com.example.ql_shopcoffee.dao.interfaces;

import com.example.ql_shopcoffee.models.Order;
import com.example.ql_shopcoffee.models.OrderDetail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderDAO {
    // ========== ORDER OPERATIONS ==========

    /**
     * Tìm order theo ID (bao gồm cả order details)
     */
    Order findById(int id);

    /**
     * Lấy tất cả orders
     */
    List<Order> findAll();

    /**
     * Lấy orders của một nhân viên cụ thể
     */
    List<Order> findByEmployee(int employeeId);

    /**
     * Lấy orders theo khoảng thời gian
     */
    List<Order> findByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Lấy orders theo ngày (hôm nay, hôm qua...)
     */
    List<Order> findByDate(LocalDate date);

    /**
     * Lấy orders theo status
     */
    List<Order> findByStatus(String status);

    /**
     * Thêm order mới (transaction: insert order + insert order details)
     */
    boolean insert(Order order);

    /**
     * Cập nhật order (chỉ update thông tin order, không update details)
     */
    boolean update(Order order);

    /**
     * Xóa order (cascade delete sẽ tự động xóa order details)
     */
    boolean delete(int id);

    // ========== ORDER DETAIL OPERATIONS ==========

    /**
     * Lấy tất cả order details của một order
     */
    List<OrderDetail> findOrderDetailsByOrderId(int orderId);

    /**
     * Thêm một item vào order
     */
    boolean insertOrderDetail(OrderDetail detail);

    /**
     * Cập nhật order detail (thay đổi quantity, price...)
     */
    boolean updateOrderDetail(OrderDetail detail);

    /**
     * Xóa một item khỏi order
     */
    boolean deleteOrderDetail(int detailId);

    /**
     * Xóa tất cả items của một order
     */
    boolean deleteAllOrderDetails(int orderId);

    // ========== STATISTICS ==========

    /**
     * Đếm số orders hôm nay
     */
    int countTodayOrders();

    /**
     * Tính tổng doanh thu hôm nay
     */
    double getTodayRevenue();

    /**
     * Tính doanh thu theo khoảng thời gian
     */
    double getRevenueByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Lấy top sản phẩm bán chạy
     */
    List<Object[]> getTopSellingProducts(int limit);
}
