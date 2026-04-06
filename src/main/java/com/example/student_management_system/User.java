package com.example.student_management_system;

public class User {

    public enum Role {
        ADMIN,
        TEACHER,
        STUDENT
    }

    private final int id;
    private final String username;
    private final String fullName;
    private final Role role;

    public User(int id, String username, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    // Compatibility constructor for older code if needed
    public User(String username, String fullName, Role role) {
        this(0, username, fullName, role);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    // Compatibility method for older controllers
    public String getDisplayName() {
        return fullName;
    }

    // Compatibility method for older controllers
    public String getRoleLabel() {
        String text = role.name().toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}