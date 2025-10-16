package com.example.ql_shopcoffee.dao.impl;

import com.example.ql_shopcoffee.dao.interfaces.IProductDAO;
import com.example.ql_shopcoffee.models.Product;
import com.example.ql_shopcoffee.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements IProductDAO {

    private final Connection connection;

    public ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Product findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm sản phẩm theo id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 1 ORDER BY name";

        try(Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tất cả sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findAllIncludingInactive() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name";

        try(Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while(rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tất cả sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ? AND status = 1 ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding products by category: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByName(String name) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? AND status = 1 ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public boolean add(Product product) {
        String sql = "INSERT INTO products (name, category_id, price, image, status) VALUES (?, ?, ?, ? ,?)";

        try(PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getCategoryId());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getImage());
            pstmt.setBoolean(5, product.isStatus());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if(generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
                System.out.println("Thêm sản phẩm thành công: " + product.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, category_id = ?, price = ?, image = ?, status = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getCategoryId());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getImage());
            pstmt.setBoolean(5, product.isStatus());
            pstmt.setInt(6, product.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Update sản phẩm thành công: " + product.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi updating sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE products SET status = 0 WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Đưa sản phẩm về hết hàng thành công: ID " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đưa sản phẩm về hết hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hardDelete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Xóa sản phẩm thành công: ID " + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa hẳn sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) as total FROM products WHERE status = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) as total FROM products WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Kiểm tra sản phẩm thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int categoryId = rs.getInt("category_id");
        double price = rs.getDouble("price");
        String image = rs.getString("image");
        boolean status = rs.getBoolean("status");

        String createdAtStr = rs.getString("created_at");
        String updatedAtStr = rs.getString("updated_at");
        LocalDateTime createdAt = createdAtStr != null ? LocalDateTime.parse(createdAtStr.replace(" ", "T")) : null;
        LocalDateTime updatedAt = updatedAtStr != null ? LocalDateTime.parse(updatedAtStr.replace(" ", "T")) : null;

        return new Product(id, name, categoryId, price, image, status, createdAt, updatedAt);
    }
}
