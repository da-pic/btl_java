package com.example.ql_shopcoffee.utils;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Khởi tạo lược đồ cở sở dữ liệu và dữ liệu mẫu
 * Tạo tables, indexes, triggers và thêm dữ liệu mẫu
 */
public class DatabaseInitializer {
    private final Connection connection;

    public DatabaseInitializer() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Khởi tạo toàn bộ database
     */
    public void initialize() {
        try {
            System.out.println(" Bắt đầu khởi tạo dữ liệu. ");

            createTables();
            createIndexes();
            createTriggers();
            insertSampleData();

            System.out.println(" Khởi tạo cơ sở dữ liệu hoàn tất. ");
        } catch (SQLException e) {
            System.err.println(" Khởi tạo cơ sở dữ liệu thất bại.");
            e.printStackTrace();
        }
    }

    /**
     * Tạo tất cả tables
     */
    private void createTables() throws SQLException {
        try(Statement stmt = connection.createStatement()) {

            // Bảng: users(người dùng)
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role in ('EMPLOYEE', 'MANAGER')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """);

            // Bảng: categories(loại sản phẩm)
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """);

            // Bảng: products(sản phẩm)
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                category_id INTEGER NOT NULL,
                price REAL NOT NULL CHECK(price >= 0),
                image TEXT,
                status INTEGER DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE ON UPDATE RESTRICT
            )
            """);

            // Bảng: orders(hóa đơn)
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                employee_id INTEGER NOT NULL,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                total_amount REAL NOT NULL DEFAULT 0 CHECK(total_amount >= 0),
                status TEXT DEFAULT 'COMPLETED' CHECK(status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
                note TEXT
            )
            """);

            // Bảng: order_details
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS order_details (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                price REAL NOT NULL CHECK(price >= 0),
                FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
            )
            """);

            System.out.println("Tạo bảng hoàn tất.");
        }
    }

    /**
     * Tạo indexes để tăng hiệu suất
     */
    private void createIndexes() throws SQLException {
        try(Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)"); // Tìm user nhanh hơn
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id)"); // Tìm sản phẩm theo loại nhanh hơn
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_status ON products(status)"); // Tìm sản phẩm theo trạng thái nhanh hơn
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_employee ON orders(employee_id)"); // Lấy đơn theo nhân viên
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date)"); // Lấy đơn theo ngày
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_details_order ON order_details(order_id)"); // Lấy chi tiết đơn hàng nhanh

            System.out.println("Tạo xong indexes");
        }
    }

    /**
     * Tạo triggers để tự động cập nhật dữ liệu
     */
    private void createTriggers() throws SQLException {
        try(Statement stmt = connection.createStatement()) {
            // Trigger: Tự động cập nhật updated_at cho users
            stmt.execute("""
            CREATE TRIGGER IF NOT EXISTS update_users_timestamp
            AFTER UPDATE ON users
            BEGIN
                UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
            END    
            """);

            // Trigger: Tự động cập nhật updated_at cho products
            stmt.execute("""
            CREATE TRIGGER IF NOT EXISTS update_products_timestamp
            AFTER UPDATE ON products
            BEGIN
                UPDATE products SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
            END
            """);

            // Trigger: Tự động cập nhật  total_amount khi insert order_details
            stmt.execute("""
            CREATE TRIGGER IF NOT EXISTS update_order_total_insert
            AFTER INSERT ON order_details
            BEGIN
                UPDATE orders
                SET total_amount = (
                    SELECT SUM(quantity * price)
                    FROM order_details
                    WHERE order_id = NEW.order_id
                )
                WHERE id = NEW.order_id;
            END
            """);

            // Trigger: Tự động cập nhật total_amount khi update order_details
            stmt.execute("""
            CREATE TRIGGER IF NOT EXISTS uppdate_order_total_update
            AFTER UPDATE ON order_details
            BEGIN
                UPDATE orders
                SET total_amount = (
                    SELECT SUM(quantity * price)
                    FROM orders_details
                    WHERE order_id = NEW.order_id
                )
                WHERE id = NEW.order_id;
            END
            """);

            // Trigger: Tự động cập nhật total_amount khi delete order_details
            stmt.execute("""
            CREATE TRIGGER IF NOT EXISTS update_order_total_delete
            AFTER DELETE ON order_details
            BEGIN
                UPDATE orders 
                SET total_amount = (
                    SELECT COALESCE(SUM(quantity * price), 0)
                    FROM order_details 
                    WHERE order_id = OLD.order_id
                 )
                WHERE id = OLD.order_id;
            END
            """);

            System.out.println("Tạo xong Triggers.");
        }
    }

    /**
     * Insert dữ liệu mẫu để test
     */
    private void insertSampleData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // Kiểm tra xem đã có dữ liệu chưa
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Dữ liệu mẫu đã tồn tại.");
                return;
            }

            // Thêm người dùng
            stmt.execute("""
                INSERT INTO users (username, password, full_name, role) VALUES
                ('admin', '123', 'Giang Trung Đức', 'MANAGER'),
                ('nhanvien1', '123', 'Nguyễn Văn A', 'EMPLOYEE'),
                ('nhanvien2', '123', 'Trần Văn B', 'EMPLOYEE')
            """);

            // Thêm loại sản phẩm
            stmt.execute("""
                INSERT INTO categories (name, description) VALUES
                ('Cà Phê', 'Các loại cà phê truyền thống'),
                ('Trà', 'Trà các loại'),
                ('Sinh Tố', 'Sinh tố hoa quả'),
                ('Bánh Ngọt', 'Bánh và đồ ăn nhẹ')
            """);

            // Thêm sản phẩm
            stmt.execute("""
                INSERT INTO products (name, category_id, price, status) VALUES
                ('Cà Phê Đen', 1, 25000, 1),
                ('Cà Phê Sữa', 1, 30000, 1),
                ('Bạc Xỉu', 1, 30000, 1),
                ('Cappuccino', 1, 45000, 1),
                ('Trà Đào', 2, 35000, 1),
                ('Trà Chanh', 2, 30000, 1),
                ('Trà Sữa Trân Châu', 2, 40000, 1),
                ('Sinh Tố Bơ', 3, 40000, 1),
                ('Sinh Tố Dâu', 3, 35000, 1),
                ('Sinh Tố Xoài', 3, 35000, 1),
                ('Bánh Flan', 4, 20000, 1),
                ('Bánh Tiramisu', 4, 45000, 1),
                ('Bánh Croissant', 4, 25000, 1)
            """);

            System.out.println("Thêm xong dữ liệu mẫu.");
        }
    }

    /**
     * Xóa toàn bộ database
     */
    public void clearTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS order_details");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS products");
            stmt.execute("DROP TABLE IF EXISTS categories");
            stmt.execute("DROP TABLE IF EXISTS users");

            System.out.println("Hoàn thành xóa hết bảng.");
        }
    }

    public void resetDatabase() {
        try {
            System.out.println("Bắt đầu reset dữ liệu.");
            clearTables();
            initialize();
            System.out.println("Hoàn thành Reset dữ liệu.");
        } catch (SQLException e) {
            System.err.println("Reset dữ liệu thất bại.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DatabaseInitializer initializer = new DatabaseInitializer();

        initializer.initialize();
    }
}
