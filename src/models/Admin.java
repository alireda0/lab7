package models;


import models.User;

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