package com.example.student_management_system;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService {

    private static AuthService instance;

    // Fallback demo users for offline/dev mode: username -> [hashedPassword, displayName, role]
    private final Map<String, String[]> userStore = new HashMap<>();

    private AuthService() {
        seedDemoUsers();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    private void seedDemoUsers() {
        addDemoUser("student",  "student123",  "Demo Student",  User.Role.STUDENT);
        addDemoUser("teacher",  "teacher123",  "Prof. Rahman",  User.Role.TEACHER);
        addDemoUser("admin",    "admin123",    "System Admin",  User.Role.ADMIN);
    }

    private void addDemoUser(String username, String plainPassword, String displayName, User.Role role) {
        userStore.put(normalizeUsername(username),
                new String[]{hashPassword(plainPassword), displayName, role.name()});
    }

    public Optional<User> authenticate(String username, String plainPassword) {
        if (username == null || plainPassword == null) return Optional.empty();

        String cleanUsername = normalizeUsername(username);
        String cleanPassword = plainPassword.trim();

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) return Optional.empty();

        // 1) Try live database first
        Optional<User> dbUser = authenticateFromDatabase(cleanUsername, cleanPassword);
        if (dbUser.isPresent()) return dbUser;

        // 2) Fall back to demo users (useful when DB is not configured)
        return authenticateFromMemory(cleanUsername, cleanPassword);
    }

    private Optional<User> authenticateFromDatabase(String username, String plainPassword) {
        String sql = """
                SELECT id, username, full_name, password, role, status
                FROM users
                WHERE LOWER(username) = ?
                """;

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return Optional.empty();

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();

                    int    id             = rs.getInt("id");
                    String dbUsername     = rs.getString("username");
                    String fullName       = rs.getString("full_name");
                    String storedPassword = rs.getString("password");
                    String roleText       = rs.getString("role");
                    String status         = rs.getString("status");

                    if (storedPassword == null || storedPassword.isBlank()) return Optional.empty();

                    // Check account is active
                    if (status != null && !"ACTIVE".equalsIgnoreCase(status.trim())) {
                        return Optional.empty();
                    }

                    // Accept both hashed and plain-text passwords (graceful migration)
                    boolean matched = storedPassword.equalsIgnoreCase(hashPassword(plainPassword))
                                   || storedPassword.equals(plainPassword);

                    if (!matched) return Optional.empty();

                    User.Role role = parseRole(roleText);
                    return Optional.of(new User(id, dbUsername, fullName, role));
                }
            }

        } catch (Exception e) {
            System.out.println("DB auth failed (falling back to demo): " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<User> authenticateFromMemory(String username, String plainPassword) {
        String[] stored = userStore.get(username);
        if (stored == null) return Optional.empty();
        if (!stored[0].equals(hashPassword(plainPassword))) return Optional.empty();
        User.Role role = User.Role.valueOf(stored[2]);
        return Optional.of(new User(0, username, stored[1], role));
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private User.Role parseRole(String roleText) {
        if (roleText == null || roleText.isBlank()) return User.Role.STUDENT;
        switch (roleText.trim().toUpperCase()) {
            case "ADMIN":   return User.Role.ADMIN;
            case "TEACHER": return User.Role.TEACHER;
            default:        return User.Role.STUDENT;
        }
    }

    private String hashPassword(String plain) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
