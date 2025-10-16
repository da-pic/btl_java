package com.example.ql_shopcoffee.services;

import com.example.ql_shopcoffee.dao.impl.CategoryDAO;
import com.example.ql_shopcoffee.dao.impl.ProductDAO;
import com.example.ql_shopcoffee.dao.interfaces.ICategoryDAO;
import com.example.ql_shopcoffee.dao.interfaces.IProductDAO;
import com.example.ql_shopcoffee.models.Category;

import java.util.List;

public class CategoryService {

    private final ICategoryDAO categoryDAO;
    private final IProductDAO productDAO;
    private final SessionManager sessionManager;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.productDAO = new ProductDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    public CategoryService(ICategoryDAO categoryDAO, IProductDAO productDAO) {
        this.categoryDAO = categoryDAO;
        this.productDAO = productDAO;
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Lấy tất cả categories
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    /**
     * Lấy category theo ID
     */
    public Category getCategoryById(int id) {
        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return null;
        }
        return categoryDAO.findById(id);
    }

    /**
     * Thêm category mới - CHỈ Manager
     */
    public boolean addCategory(String name, String description) {
        // Kiểm tra quyền
        if (!sessionManager.hasPermission("MANAGE_CATEGORIES")) {
            System.err.println("Bạn không có quyền thêm danh mục");
            return false;
        }

        // Validation
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Tên danh mục không được để trống");
            return false;
        }

        if (name.trim().length() < 2) {
            System.err.println("Tên danh mục phải có ít nhất 2 ký tự");
            return false;
        }

        // Kiểm tra trùng tên
        if (categoryDAO.nameExists(name.trim())) {
            System.err.println("Tên danh mục đã tồn tại");
            return false;
        }

        // Tạo category mới
        Category category = new Category(name.trim(), description != null ? description.trim() : "");

        boolean result = categoryDAO.add(category);

        if (result) {
            System.out.println("Thêm danh mục thành công: " + category.getName());
        }

        return result;
    }

    /**
     * Cập nhật category - CHỈ Manager
     */
    public boolean updateCategory(int id, String name, String description) {
        // Kiểm tra quyền
        if (!sessionManager.hasPermission("MANAGE_CATEGORIES")) {
            System.err.println("Bạn không có quyền sửa danh mục");
            return false;
        }

        // Validation
        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return false;
        }

        Category category = categoryDAO.findById(id);
        if (category == null) {
            System.err.println("Danh mục không tồn tại");
            return false;
        }

        if (name == null || name.trim().isEmpty() || name.trim().length() < 2) {
            System.err.println("Tên danh mục không hợp lệ");
            return false;
        }

        // Kiểm tra trùng tên (trừ chính nó)
        if (!category.getName().equals(name.trim()) && categoryDAO.nameExists(name.trim())) {
            System.err.println("Tên danh mục đã tồn tại");
            return false;
        }

        // Update
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : "");

        boolean result = categoryDAO.update(category);

        if (result) {
            System.out.println("Cập nhật danh mục thành công: " + category.getName());
        }

        return result;
    }

    /**
     * Xóa category - CHỈ Manager
     * Không cho xóa nếu còn products trong category
     */
    public boolean deleteCategory(int id) {
        // Kiểm tra quyền
        if (!sessionManager.hasPermission("MANAGE_CATEGORIES")) {
            System.err.println("Bạn không có quyền xóa danh mục");
            return false;
        }

        if (id <= 0) {
            System.err.println("ID không hợp lệ");
            return false;
        }

        Category category = categoryDAO.findById(id);
        if (category == null) {
            System.err.println("Danh mục không tồn tại");
            return false;
        }

        // Kiểm tra xem có products nào trong category không
        List<?> products = productDAO.findByCategory(id);
        if (!products.isEmpty()) {
            System.err.println("Không thể xóa danh mục vì còn " + products.size() + " sản phẩm");
            return false;
        }

        boolean result = categoryDAO.delete(id);

        if (result) {
            System.out.println("Xóa danh mục thành công: " + category.getName());
        }

        return result;
    }

    /**
     * Đếm số lượng categories
     */
    public int getCategoryCount() {
        return categoryDAO.count();
    }

    /**
     * Kiểm tra tên category có tồn tại không
     */
    public boolean isCategoryNameExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return categoryDAO.nameExists(name.trim());
    }
}
