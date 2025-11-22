package models;

/**
 * Admin class represents an administrator user with elevated permissions.
 * Admins can approve/reject courses, view analytics, and manage the platform.
 * 
 * Used in Lab 8 for course approval workflow and platform management.
 */
public class Admin extends User {
    
    public static final String ROLE_ADMIN = "ADMIN";
    
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default constructor
     * Note: Requires basic user information since User has no empty constructor
     */
    public Admin() {
        super(0, "", "", "", ROLE_ADMIN, true);
    }
    
    public Admin(int userId, String username, String email, 
                 String passwordHash, boolean isActive) {
        super(userId, username, email, passwordHash, ROLE_ADMIN, isActive);
    }
    
 
    
    
   
    // ==================== UTILITY METHODS ====================
    
    @Override
    public String toString() {
        return "Admin{" +
                "userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}