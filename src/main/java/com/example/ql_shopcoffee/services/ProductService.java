package com.example.ql_shopcoffee.services;

import com.example.ql_shopcoffee.dao.impl.CategoryDAO;
import com.example.ql_shopcoffee.dao.impl.ProductDAO;
import com.example.ql_shopcoffee.dao.interfaces.ICategoryDAO;
import com.example.ql_shopcoffee.dao.interfaces.IProductDAO;
import com.example.ql_shopcoffee.models.Category;
import com.example.ql_shopcoffee.models.Product;

import java.util.List;

public class ProductService {

    private final IProductDAO productDAO;
    private final ICategoryDAO categoryDAO;
    private final SessionManager sessionManager;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    public ProductService(IProductDAO productDAO, ICategoryDAO categoryDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Lấy tất cả products active
     */
    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    /**
     * Lấy tất cả products (cả active và inactive) - CHỈ Manager
     */
    public List<Product> getAllProductsIncludingInactive() {
        if (!sessionManager.isManager()) {
            System.err.println("Chỉ Manager mới có quyền xem sản phẩm inactive");
            return productDAO.findAll(); // Return only active
        }
        return productDAO.findAllIncludingInactive();
    }

    /**
     * Lấy product theo ID
     */
    public Product getProductById(int id) {
        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return null;
        }
        return productDAO.findById(id);
    }

    /**
     * Lấy products theo category
     */
    public List<Product> getProductsByCategory(int categoryId) {
        if (categoryId <= 0) {
            System.err.println("Category ID không hợp lệ");
            return List.of();
        }
        return productDAO.findByCategory(categoryId);
    }

    /**
     * Tìm kiếm products theo tên
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.findByName(keyword.trim());
    }

    /**
     * Thêm product mới - CHỈ Manager
     */
    public boolean addProduct(String name, int categoryId, double price, String image) {
        // Kiểm tra quyền
        if (!sessionManager.hasPermission("MANAGE_PRODUCTS")) {
            System.err.println("Bạn không có quyền thêm sản phẩm");
            return false;
        }

        // Validation
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Tên sản phẩm không được để trống");
            return false;
        }

        if (name.trim().length() < 3) {
            System.err.println("Tên sản phẩm phải có ít nhất 3 ký tự");
            return false;
        }

        if (categoryId <= 0) {
            System.err.println("Phải chọn danh mục");
            return false;
        }

        // Kiểm tra category có tồn tại không
        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            System.err.println("Danh mục không tồn tại");
            return false;
        }

        if (price < 0) {
            System.err.println("Giá không được âm");
            return false;
        }

        if (price < 1000) {
            System.err.println("Giá phải lớn hơn 1,000 đ");
            return false;
        }

        // Tạo product mới
        Product product = new Product(name.trim(), categoryId, price, image);

        boolean result = productDAO.add(product);

        if (result) {
            System.out.println("Thêm sản phẩm thành công: " + product.getName());
        }

        return result;
    }

    /**
     * Cập nhật product - CHỈ Manager
     */
    public boolean updateProduct(int id, String name, int categoryId, double price, String image, boolean status) {
        // Kiểm tra quyền
        if (!sessionManager.hasPermission("MANAGE_PRODUCTS")) {
            System.err.println("Bạn không có quyền sửa sản phẩm");
            return false;
        }

        // Validation
        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return false;
        }

        Product product = productDAO.findById(id);
        if (product == null) {
            System.err.println("Sản phẩm không tồn tại");
            return false;
        }

        if (name == null || name.trim().isEmpty() || name.trim().length() < 3) {
            System.err.println("Tên sản phẩm không hợp lệ");
            return false;
        }

        if (categoryId <= 0 || categoryDAO.findById(categoryId) == null) {
            System.err.println("Danh mục không hợp lệ");
            return false;
        }

        if (price < 1000) {
            System.err.println("Giá không hợp lệ");
            return false;
        }

        // Update
        product.setName(name.trim());
        product.setCategoryId(categoryId);
        product.setPrice(price);
        product.setImage(image);
        product.setStatus(status);

        boolean result = productDAO.update(product);

        if (result) {
            System.out.println("Cập nhật sản phẩm thành công: " + product.getName());
        }

        return result;
    }

    /**
     * Xóa product (soft delete) - CHỈ Manager
     */
    public boolean deleteProduct(int id) {
        // Kiểm tra quyền
        if (!sessionManager.hasPermission("MANAGE_PRODUCTS")) {
            System.err.println("Bạn không có quyền xóa sản phẩm");
            return false;
        }

        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return false;
        }

        Product product = productDAO.findById(id);
        if (product == null) {
            System.err.println("Sản phẩm không tồn tại");
            return false;
        }

        boolean result = productDAO.delete(id);

        if (result) {
            System.out.println("Xóa sản phẩm thành công: " + product.getName());
        }

        return result;
    }

    /**
     * Kích hoạt lại product - CHỈ Manager
     */
    public boolean activateProduct(int id) {
        if (!sessionManager.hasPermission("MANAGE_PRODUCTS")) {
            System.err.println("Bạn không có quyền kích hoạt sản phẩm");
            return false;
        }

        Product product = productDAO.findById(id);
        if (product == null) {
            System.err.println("Sản phẩm không tồn tại");
            return false;
        }

        product.activate();
        return productDAO.update(product);
    }

    /**
     * Vô hiệu hóa product - CHỈ Manager
     */
    public boolean deactivateProduct(int id) {
        if (!sessionManager.hasPermission("MANAGE_PRODUCTS")) {
            System.err.println("Bạn không có quyền vô hiệu hóa sản phẩm");
            return false;
        }

        Product product = productDAO.findById(id);
        if (product == null) {
            System.err.println("Sản phẩm không tồn tại");
            return false;
        }

        product.deactivate();
        return productDAO.update(product);
    }

    /**
     * Đếm số lượng products
     */
    public int getProductCount() {
        return productDAO.count();
    }
}
