package com.example.ql_shopcoffee.controllers;

import com.example.ql_shopcoffee.models.User;
import com.example.ql_shopcoffee.services.AuthService;
import com.example.ql_shopcoffee.utils.AlertUtil;
import com.example.ql_shopcoffee.utils.SceneManager;
import com.example.ql_shopcoffee.utils.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class LoginController {

    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final AuthService authService;
    private final SceneManager sceneManager;

    public LoginController() {
        this.authService = new AuthService();
        this.sceneManager = SceneManager.getInstance();
    }

    /**
     * Initialize - tự động gọi sau khi FXML load
     */
    @FXML
    public void initialize() {
        System.out.println("LoginController initialized");

        // Focus vào username field khi mở màn hình
        usernameField.requestFocus();

        // Thêm listener để ẩn error khi user bắt đầu gõ
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> hideError());

        // Xử lý phím Enter
        usernameField.setOnKeyPressed(this::handleKeyPressed);
        passwordField.setOnKeyPressed(this::handleKeyPressed);
    }

    /**
     * Xử lý sự kiện nhấn phím
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Xử lý đăng nhập
     */
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validation
        if (!validateInput(username, password)) {
            return;
        }

        // Disable button để tránh spam click
        loginButton.setDisable(true);

        // Thực hiện đăng nhập
        User user = authService.login(username, password);

        if (user != null) {
            // Đăng nhập thành công
            System.out.println("✓ Login successful: " + user.getFullName());

            // Chuyển sang dashboard tương ứng
            redirectToDashboard(user);

        } else {
            // Đăng nhập thất bại
            showError("Tên đăng nhập hoặc mật khẩu không đúng!");
            loginButton.setDisable(false);

            // Clear password field
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    /**
     * Validate input
     */
    private boolean validateInput(String username, String password) {
        // Check empty
        if (ValidationUtil.isEmpty(username)) {
            showError("Vui lòng nhập tên đăng nhập!");
            usernameField.requestFocus();
            return false;
        }

        if (ValidationUtil.isEmpty(password)) {
            showError("Vui lòng nhập mật khẩu!");
            passwordField.requestFocus();
            return false;
        }

        // Check minimum length
        if (username.trim().length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự!");
            usernameField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Chuyển hướng đến dashboard phù hợp
     */
    private void redirectToDashboard(User user) {
        if ("MANAGER".equals(user.getRole())) {
            // Chuyển sang Manager Dashboard
            sceneManager.switchScene(
                    "/com/coffeeshop/views/manager-dashboard.fxml",
                    "Coffee Shop - Manager Dashboard",
                    1200,
                    700
            );
        } else {
            // Chuyển sang Employee Dashboard
            sceneManager.switchScene(
                    "/com/coffeeshop/views/employee-dashboard.fxml",
                    "Coffee Shop - Employee Dashboard",
                    1000,
                    600
            );
        }
    }

    /**
     * Hiển thị lỗi
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Thêm hiệu ứng shake (optional)
        shakeNode(errorLabel);
    }

    /**
     * Ẩn lỗi
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Hiệu ứng shake cho node (optional)
     */
    private void shakeNode(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(100), node
        );
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    /**
     * Xử lý hover button (optional - thêm hiệu ứng)
     */
    @FXML
    public void handleLoginButtonHover() {
        loginButton.setStyle(
                "-fx-background-color: #2980B9; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
    }

    /**
     * Xử lý mouse exit button
     */
    @FXML
    public void handleLoginButtonExit() {
        loginButton.setStyle(
                "-fx-background-color: #3498DB; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12; " +
                        "-fx-background-radius: 5;"
        );
    }
}
