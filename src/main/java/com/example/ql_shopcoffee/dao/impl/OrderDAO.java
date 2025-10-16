package com.example.ql_shopcoffee.dao.impl;

import com.example.ql_shopcoffee.dao.interfaces.IOrderDAO;
import com.example.ql_shopcoffee.models.Order;
import com.example.ql_shopcoffee.models.OrderDetail;
import com.example.ql_shopcoffee.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements IOrderDAO {

    private final Connection connection;

    public OrderDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ========== ORDER OPERATIONS ==========

    @Override
    public Order findById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = extractOrderFromResultSet(rs);

                order.setOrderDetails(findOrderDetailsByOrderId(id));
                return order;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn theo id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderDetails(findOrderDetailsByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm toàn bộ sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> findByEmployee(int employeeId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE employee_id = ? ORDER BY order_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderDetails(findOrderDetailsByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn được tạo bới nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, start.toString().replace("T", " "));
            pstmt.setString(2, end.toString().replace("T", " "));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderDetails(findOrderDetailsByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn theo khoảng ngày: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return findByDateRange(start, end);
    }

    @Override
    public List<Order> findByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setOrderDetails(findOrderDetailsByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn theo trạng thái: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public boolean insert(Order order) {
        String sqlOrder = "INSERT INTO orders (employee_id, order_date, total_amount, status, note) VALUES (?, ?, ?, ?, ?)";

        try {
            // Tắt auto-commit để thực hiện transaction
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getEmployeeId());
                pstmt.setString(2, order.getOrderDate().toString().replace("T", " "));
                pstmt.setDouble(3, order.getTotalAmount());
                pstmt.setString(4, order.getStatus());
                pstmt.setString(5, order.getNote());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    // Lấy ID vừa insert
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        order.setId(orderId);

                        // Insert tất cả order details
                        for (OrderDetail detail : order.getOrderDetails()) {
                            detail.setOrderId(orderId);
                            if (!insertOrderDetail(detail)) {
                                // Nếu insert detail thất bại, rollback
                                connection.rollback();
                                connection.setAutoCommit(true);
                                return false;
                            }
                        }

                        // Commit transaction
                        connection.commit();
                        connection.setAutoCommit(true);

                        System.out.println("Hóa đơn đã được thêm thành công: " + order.getOrderDetails().size() + " items");
                        return true;
                    }
                }
            }

            connection.rollback();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm hóa đơn: " + e.getMessage());
            e.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean update(Order order) {
        String sql = "UPDATE orders SET employee_id = ?, total_amount = ?, status = ?, note = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, order.getEmployeeId());
            pstmt.setDouble(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus());
            pstmt.setString(4, order.getNote());
            pstmt.setInt(5, order.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Cập nhật hóa đơn thành công: ID " + order.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        // CASCADE delete sẽ tự động xóa order_details
        String sql = "DELETE FROM orders WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Xóa hóa đơn thành công: ID " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========== ORDER DETAIL OPERATIONS ==========

    @Override
    public List<OrderDetail> findOrderDetailsByOrderId(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                details.add(extractOrderDetailFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lõi khi tìm chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return details;
    }

    @Override
    public boolean insertOrderDetail(OrderDetail detail) {
        String sql = "INSERT INTO order_details (order_id, product_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, detail.getOrderId());
            pstmt.setInt(2, detail.getProductId());
            pstmt.setString(3, detail.getProductName());
            pstmt.setInt(4, detail.getQuantity());
            pstmt.setDouble(5, detail.getPrice());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    detail.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateOrderDetail(OrderDetail detail) {
        String sql = "UPDATE order_details SET quantity = ?, price = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, detail.getQuantity());
            pstmt.setDouble(2, detail.getPrice());
            pstmt.setInt(3, detail.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteOrderDetail(int detailId) {
        String sql = "DELETE FROM order_details WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, detailId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteAllOrderDetails(int orderId) {
        String sql = "DELETE FROM order_details WHERE order_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa toàn bộ chi tiets hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========== STATISTICS ==========

    @Override
    public int countTodayOrders() {
        String sql = "SELECT COUNT(*) as total FROM orders WHERE DATE(order_date) = DATE('now')";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("lỗi khi đếm số hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double getTodayRevenue() {
        String sql = "SELECT SUM(total_amount) as revenue FROM orders WHERE DATE(order_date) = DATE('now') AND status = 'COMPLETED'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("revenue");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng doanh thu: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double getRevenueByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT SUM(total_amount) as revenue FROM orders WHERE order_date BETWEEN ? AND ? AND status = 'COMPLETED'";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, start.toString().replace("T", " "));
            pstmt.setString(2, end.toString().replace("T", " "));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("revenue");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu theo khảng thời gian: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public List<Object[]> getTopSellingProducts(int limit) {
        List<Object[]> result = new ArrayList<>();
        String sql = """
            SELECT 
                od.product_name,
                SUM(od.quantity) as total_quantity,
                SUM(od.quantity * od.price) as total_revenue
            FROM order_details od
            JOIN orders o ON od.order_id = o.id
            WHERE o.status = 'COMPLETED'
            GROUP BY od.product_id, od.product_name
            ORDER BY total_quantity DESC
            LIMIT ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("product_name");
                row[1] = rs.getInt("total_quantity");
                row[2] = rs.getDouble("total_revenue");
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy top sản phẩm bán chạy: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // ========== HELPER METHODS ==========

    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int employeeId = rs.getInt("employee_id");
        String orderDateStr = rs.getString("order_date");
        double totalAmount = rs.getDouble("total_amount");
        String status = rs.getString("status");
        String note = rs.getString("note");

        LocalDateTime orderDate = orderDateStr != null ?
                LocalDateTime.parse(orderDateStr.replace(" ", "T")) : LocalDateTime.now();

        return new Order(id, employeeId, orderDate, totalAmount, status, note);
    }

    private OrderDetail extractOrderDetailFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int orderId = rs.getInt("order_id");
        int productId = rs.getInt("product_id");
        String productName = rs.getString("product_name");
        int quantity = rs.getInt("quantity");
        double price = rs.getDouble("price");

        return new OrderDetail(id, orderId, productId, productName, quantity, price);
    }
}
