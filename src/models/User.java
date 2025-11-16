package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class User {

    private int userId;            // must be positive integer
    private String username;
    private String email;          // must match string@string.com
    private String passwordHash;   // hashed using SHA-256
    private String role;           // "STUDENT" or "INSTRUCTOR"

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";

    public User(int userId,
                String username,
                String email,
                String rawPassword,
                String role) {

        this.userId = validateUserId(userId);
        this.username = requireNonEmpty("username", username);
        this.email = validateEmail(email);
        this.passwordHash = hashPassword(requireNonEmpty("password", rawPassword));
        this.role = validateRole(role);
    }

    // ===============================
    //        VALIDATION METHODS
    // ===============================

    private int validateUserId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("userId must be a positive integer.");
        }
        return id;
    }

    private String validateEmail(String email) {
        email = requireNonEmpty("email", email);

        // must match string@string.com
        // example: abc@xyz.com
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.com$")) {
            throw new IllegalArgumentException(
                "Invalid email format. Email must be of form string@string.com"
            );
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

    // ===============================
    //       PASSWORD HASHING
    // ===============================

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(rawPassword.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    // ===============================
    //            GETTERS
    // ===============================

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = requireNonEmpty("username", username);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Hash the new raw password
    public void setRawPassword(String rawPassword) {
        this.passwordHash = hashPassword(requireNonEmpty("password", rawPassword));
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = validateRole(role);
    }
}
