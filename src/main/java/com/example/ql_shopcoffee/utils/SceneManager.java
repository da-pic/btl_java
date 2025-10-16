package com.example.ql_shopcoffee.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Chuyển sang scene khác
     * @param fxmlPath đường dẫn file FXML (VD: "/com/coffeeshop/views/login.fxml")
     * @param title tiêu đề window
     */
    public void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();
            AlertUtil.showError("Lỗi", "Không thể tải màn hình: " + fxmlPath);
        }
    }

    /**
     * Chuyển scene và set size
     */
    public void switchScene(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();
            AlertUtil.showError("Lỗi", "Không thể tải màn hình: " + fxmlPath);
        }
    }

    /**
     * Load FXML và trả về controller
     * Dùng khi cần access controller
     */
    public <T> T loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}
