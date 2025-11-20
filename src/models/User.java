package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class User {

    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String role;

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";

    public User(int userId, String username, String email, String password, String role, boolean isAlreadyHashed) {
        this.userId = validateUserId(userId);
        this.username = requireNonEmpty("username", username);
        this.email = validateEmail(email);
        this.role = validateRole(role);

        if (isAlreadyHashed) {
            this.passwordHash = password; 
        } else {
            this.passwordHash = hashPassword(requireNonEmpty("password", password));
        }
    }

    private int validateUserId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("userId must be a positive integer.");
        }
        return id;
    }

    private String validateEmail(String email) {
        email = requireNonEmpty("email", email);
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.com$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        return email;
    }

    private String validateRole(String role) {
        role = requireNonEmpty("role", role).toUpperCase();
        if (!role.equals(ROLE_STUDENT) && !role.equals(ROLE_INSTRUCTOR)) {
            throw new IllegalArgumentException("Role must be STUDENT or INSTRUCTOR.");
        }
        return role;
    }

    private String requireNonEmpty(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        return value.trim();
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }
}