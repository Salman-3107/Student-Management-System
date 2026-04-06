package com.example.student_management_system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=Asia/Dhaka";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "MySQL@123";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = getValue("db.url", "SMS_DB_URL", DEFAULT_URL);
        String user = getValue("db.user", "SMS_DB_USER", DEFAULT_USER);
        String password = getValue("db.password", "SMS_DB_PASSWORD", DEFAULT_PASSWORD);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String getValue(String propertyKey, String envKey, String defaultValue) {
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }
}