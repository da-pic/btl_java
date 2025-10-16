package com.example.ql_shopcoffee; // Thay đổi package nếu cần

import com.example.ql_shopcoffee.dao.impl.UserDAO;
import com.example.ql_shopcoffee.models.Order;
import com.example.ql_shopcoffee.models.Product;
import com.example.ql_shopcoffee.models.User;
import com.example.ql_shopcoffee.services.*;
import com.example.ql_shopcoffee.utils.DatabaseConnection;
import com.example.ql_shopcoffee.utils.DatabaseInitializer;

import java.util.List;

/**
 * Lớp này dùng để kiểm thử (test) toàn bộ chức năng của ứng dụng
 * thông qua một phương thức main duy nhất.
 *
 * Cách chạy: Click chuột phải vào file này và chọn "Run 'MainTestRunner.main()'"
 */
public class MainTestRunner {

    // Khai báo các service cần thiết
    private static AuthService authService;
    private static CategoryService categoryService;
    private static ProductService productService;
    private static OrderService orderService;
    private static SessionManager sessionManager;

    public static void main(String[] args) {
        // --- 1. KHỞI TẠO ---
        System.out.println("===== BẮT ĐẦU CHƯƠNG TRÌNH TEST TOÀN DIỆN =====");
        initialize();

        try {
            // --- 2. CHẠY CÁC KỊCH BẢN TEST ---
            testAuthentication();
            testCategoryManagement();
            testProductManagement();
            testProductViewing();
            testOrderWorkflow();
            testOrderViewingAndStats();

        } catch (Exception e) {
            System.err.println("!!! ĐÃ XẢY RA LỖI KHÔNG MONG MUỐN TRONG QUÁ TRÌNH TEST !!!");
            e.printStackTrace();
        } finally {
            // --- 3. DỌN DẸP ---
            // Đảm bảo kết nối DB luôn được đóng sau khi chạy xong
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("\n===== KẾT THÚC CHƯƠNG TRÌNH TEST =====");
        }
    }

    /**
     * Khởi tạo database về trạng thái sạch và khởi tạo các service.
     */
    private static void initialize() {
        System.out.println("\n--- [Bước 0] Khởi tạo và Reset Database ---");
        DatabaseInitializer dbInitializer = new DatabaseInitializer();
        dbInitializer.resetDatabase(); // Reset DB để mỗi lần chạy test đều giống nhau

        // Khởi tạo các service
        sessionManager = SessionManager.getInstance();
        authService = new AuthService();
        categoryService = new CategoryService();
        productService = new ProductService();
        orderService = new OrderService();
        System.out.println(">>> Khởi tạo hoàn tất.");
    }

    /**
     * Kịch bản 1: Kiểm tra Đăng nhập, Đăng xuất.
     */
    private static void testAuthentication() {
        System.out.println("\n--- [Test 1] Kịch bản Xác thực (Authentication) ---");

        System.out.println("1.1. Đăng nhập với vai trò MANAGER (admin/123)...");
        User manager = authService.login("admin", "123");
        if (manager != null) {
            System.out.println(">>> THÀNH CÔNG: Chào mừng Manager '" + manager.getFullName() + "'");
        } else {
            System.out.println(">>> THẤT BẠI");
        }
        authService.logout();

        System.out.println("\n1.2. Đăng nhập với vai trò EMPLOYEE (nhanvien1/123)...");
        User employee = authService.login("nhanvien1", "123");
        if (employee != null) {
            System.out.println(">>> THÀNH CÔNG: Chào mừng Employee '" + employee.getFullName() + "'");
        } else {
            System.out.println(">>> THẤT BẠI");
        }
        authService.logout();

        System.out.println("\n1.3. Đăng nhập thất bại (sai mật khẩu)...");
        User failedUser = authService.login("admin", "wrongpassword");
        if (failedUser == null) {
            System.out.println(">>> THÀNH CÔNG: Đăng nhập thất bại đúng như mong đợi.");
        } else {
            System.out.println(">>> THẤT BẠI: Lẽ ra không đăng nhập được.");
        }
    }

    /**
     * Kịch bản 2: Kiểm tra Quản lý Danh mục (chỉ Manager).
     */
    private static void testCategoryManagement() {
        System.out.println("\n--- [Test 2] Kịch bản Quản lý Danh mục ---");

        // Đăng nhập với vai trò Manager
        sessionManager.login(authService.login("admin", "123"));
        System.out.println("2.1. Manager thêm danh mục mới 'Nước Ép'...");
        boolean added = categoryService.addCategory("Nước Ép", "Các loại nước ép tươi");
        System.out.println(added ? ">>> THÀNH CÔNG." : ">>> THẤT BẠI.");

        // Đăng xuất Manager, đăng nhập Employee
        authService.logout();
        sessionManager.login(authService.login("nhanvien1", "123"));
        System.out.println("\n2.2. Employee thử thêm danh mục 'Đồ Ăn Vặt' (dự kiến thất bại)...");
        added = categoryService.addCategory("Đồ Ăn Vặt", "Snack");
        System.out.println(!added ? ">>> THÀNH CÔNG: Employee không thể thêm đúng như mong đợi." : ">>> THẤT BẠI: Lẽ ra Employee không có quyền này.");

        // Đăng xuất Employee, đăng nhập Manager
        authService.logout();
        sessionManager.login(authService.login("admin", "123"));
        System.out.println("\n2.3. Manager thử xóa danh mục 'Cà Phê' đang có sản phẩm (dự kiến thất bại)...");
        boolean deleted = categoryService.deleteCategory(1); // ID 1 là 'Cà Phê'
        System.out.println(!deleted ? ">>> THÀNH CÔNG: Không thể xóa danh mục đang dùng đúng như mong đợi." : ">>> THẤT BẠI: Lẽ ra không xóa được.");
        authService.logout();
    }

    /**
     * Kịch bản 3: Kiểm tra Quản lý Sản phẩm (chỉ Manager).
     */
    private static void testProductManagement() {
        System.out.println("\n--- [Test 3] Kịch bản Quản lý Sản phẩm ---");
        sessionManager.login(authService.login("admin", "123"));

        System.out.println("3.1. Manager thêm sản phẩm mới 'Trà Oolong' vào danh mục 'Trà' (ID=2)...");
        boolean added = productService.addProduct("Trà Oolong", 2, 50000, null);
        System.out.println(added ? ">>> THÀNH CÔNG." : ">>> THẤT BẠI.");

        System.out.println("\n3.2. Manager cập nhật giá 'Cà Phê Đen' (ID=1) thành 28,000đ...");
        boolean updated = productService.updateProduct(1, "Cà Phê Đen", 1, 28000, null, true);
        Product p = productService.getProductById(1);
        System.out.println(updated && p.getPrice() == 28000 ? ">>> THÀNH CÔNG: Giá mới là " + p.getFormattedPrice() : ">>> THẤT BẠI.");

        System.out.println("\n3.3. Manager ngừng bán (soft delete) 'Bánh Croissant' (ID=13)...");
        boolean deleted = productService.deleteProduct(13);
        System.out.println(deleted ? ">>> THÀNH CÔNG." : ">>> THẤT BẠI.");
        authService.logout();
    }

    /**
     * Kịch bản 4: Kiểm tra quyền xem sản phẩm của các vai trò khác nhau.
     */
    private static void testProductViewing() {
        System.out.println("\n--- [Test 4] Kịch bản Xem Sản phẩm ---");

        sessionManager.login(authService.login("admin", "123"));
        int managerViewCount = productService.getAllProductsIncludingInactive().size();
        System.out.println("4.1. Manager xem: thấy " + managerViewCount + " sản phẩm (bao gồm sản phẩm đã ngừng bán).");
        authService.logout();

        sessionManager.login(authService.login("nhanvien1", "123"));
        int employeeViewCount = productService.getAllProducts().size();
        System.out.println("4.2. Employee xem: thấy " + employeeViewCount + " sản phẩm (chỉ sản phẩm đang bán).");
        authService.logout();

        System.out.println(managerViewCount > employeeViewCount ? ">>> KẾT QUẢ ĐÚNG: Manager thấy nhiều sản phẩm hơn." : ">>> KẾT QUẢ SAI: Số lượng sản phẩm có vấn đề.");
    }


    /**
     * Kịch bản 5: Mô phỏng quy trình bán hàng của một nhân viên.
     */
    private static void testOrderWorkflow() {
        System.out.println("\n--- [Test 5] Kịch bản Quy trình Bán hàng ---");
        sessionManager.login(authService.login("nhanvien1", "123"));

        System.out.println("5.1. Nhân viên 'Nguyễn Văn A' tạo đơn hàng mới...");
        Order order = orderService.createNewOrder();
        System.out.println(">>> Đã tạo order tạm.");

        System.out.println("\n5.2. Thêm 2 'Cà Phê Sữa' (ID=2) và 1 'Bánh Flan' (ID=11)...");
        orderService.addProductToOrder(order, 2, 2); // 2 x 30,000
        orderService.addProductToOrder(order, 11, 1); // 1 x 20,000
        System.out.println(">>> Order hiện tại: " + order.getItemCount() + " món, tổng tiền " + order.getFormattedTotal());

        System.out.println("\n5.3. Cập nhật số lượng 'Bánh Flan' thành 3...");
        orderService.updateProductQuantity(order, 11, 3); // 3 x 20,000
        System.out.println(">>> Order cập nhật: " + order.getItemCount() + " món, tổng tiền " + order.getFormattedTotal());


        System.out.println("\n5.4. Xóa 'Cà Phê Sữa' khỏi đơn...");
        orderService.removeProductFromOrder(order, 2);
        System.out.println(">>> Order cập nhật: " + order.getItemCount() + " món, tổng tiền " + order.getFormattedTotal());


        System.out.println("\n5.5. Thanh toán đơn hàng...");
        boolean checkedOut = orderService.checkout(order, "Khách quen, ít ngọt.");
        System.out.println(checkedOut ? ">>> THÀNH CÔNG: Đã thanh toán và lưu đơn hàng vào DB." : ">>> THẤT BẠI.");
        authService.logout();
    }

    /**
     * Kịch bản 6: Kiểm tra xem đơn hàng và thống kê.
     */
    private static void testOrderViewingAndStats() {
        System.out.println("\n--- [Test 6] Kịch bản Xem Đơn hàng và Thống kê ---");

        sessionManager.login(authService.login("nhanvien1", "123"));
        List<Order> myOrders = orderService.getMyOrders();
        System.out.println("6.1. Employee 'nhanvien1' xem lại đơn hàng của mình: " + myOrders.size() + " đơn hàng.");
        authService.logout();

        sessionManager.login(authService.login("admin", "123"));
        List<Order> allOrders = orderService.getAllOrders();
        System.out.println("\n6.2. Manager 'admin' xem tất cả đơn hàng: " + allOrders.size() + " đơn hàng.");

        System.out.println("\n6.3. Manager xem doanh thu hôm nay...");
        double revenue = orderService.getTodayRevenue();
        System.out.println(">>> Doanh thu hôm nay: " + String.format("%,.0f đ", revenue));

        System.out.println("\n6.4. Manager xem top sản phẩm bán chạy nhất...");
        List<Object[]> topProducts = orderService.getTopSellingProducts(3);
        System.out.println(">>> Top 3 sản phẩm bán chạy:");
        for (Object[] row : topProducts) {
            System.out.printf("    - %s: %d sản phẩm, doanh thu %,.0f đ\n", row[0], row[1], row[2]);
        }
        authService.logout();
    }
}