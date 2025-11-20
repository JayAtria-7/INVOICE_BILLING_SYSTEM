package com.yourcompany.invoicesystem.util;

import com.yourcompany.invoicesystem.model.User;

/**
 * Session manager to track the currently logged-in user
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            Logger.info("User logged in: " + user.getUsername() + " (Role: " + user.getRole() + ")");
        } else {
            Logger.info("User logged out");
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }
    
    public boolean isManager() {
        return currentUser != null && 
               (currentUser.getRole() == User.Role.MANAGER || currentUser.getRole() == User.Role.ADMIN);
    }
    
    public void logout() {
        setCurrentUser(null);
    }
}
