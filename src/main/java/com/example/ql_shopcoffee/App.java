package com.example.ql_shopcoffee;

import com.example.ql_shopcoffee.utils.DatabaseConnection;
import com.example.ql_shopcoffee.utils.DatabaseInitializer;
import com.example.ql_shopcoffee.utils.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("========================================");
            System.out.println("Coffee Shop Management System");
            System.out.println("========================================");

            // 1. Khởi tạo database
            System.out.println("\n[1/3] Initializing database...");
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();

            // 2. Set primary stage cho SceneManager
            System.out.println("[2/3] Setting up Scene Manager...");
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.setPrimaryStage(primaryStage);

            // 3. Load Login Screen
            System.out.println("[3/3] Loading Login Screen...");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("views/login.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 400, 500);

            // Set stage properties
            primaryStage.setTitle("Coffee Shop - Đăng nhập");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Show stage
            primaryStage.show();

            System.out.println("\n✓ Application started successfully!");
            System.out.println("========================================\n");
        } catch (Exception e) {
            System.err.println("✗ Failed to start application!");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        // Cleanup khi đóng ứng dụng
        System.out.println("\n========================================");
        System.out.println("Shutting down application...");

        // Đóng database connection
        DatabaseConnection.getInstance().closeConnection();

        System.out.println("✓ Application stopped successfully!");
        System.out.println("========================================");
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        launch(args);
    }
}
