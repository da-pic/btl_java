package com.example.ql_shopcoffee.utils;

public class ValidationUtil {

    /**
     * Kiểm tra string có rỗng không
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Kiểm tra string có null hoặc rỗng không
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Validate username
     * - Không rỗng
     * - Độ dài >= 3
     * - Chỉ chứa chữ, số, underscore
     */
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) {
            return false;
        }

        if (username.trim().length() < 3) {
            return false;
        }

        // Chỉ chứa chữ, số, underscore
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Validate password
     * - Không rỗng
     * - Độ dài >= 3
     */
    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) {
            return false;
        }

        return password.length() >= 3;
    }

    /**
     * Validate số (phải là số dương)
     */
    public static boolean isPositiveNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }

        try {
            double number = Double.parseDouble(str);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate số nguyên dương
     */
    public static boolean isPositiveInteger(String str) {
        if (isEmpty(str)) {
            return false;
        }

        try {
            int number = Integer.parseInt(str);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parse string sang double
     * @return giá trị double hoặc 0.0 nếu parse fail
     */
    public static double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Parse string sang int
     * @return giá trị int hoặc 0 nếu parse fail
     */
    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
