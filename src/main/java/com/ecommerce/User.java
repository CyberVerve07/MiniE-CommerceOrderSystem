package com.ecommerce;

/**
 * User class represents a registered customer in the e-commerce system.
 * It stores customer authentication details and contact information.
 */
public class User {

    private int userId;
    private String fullName;
    private String username;
    private String password;
    private String email;

    /**
     * Parameterized constructor to create a new user profile.
     *
     * @param userId   Unique identifier for the user
     * @param fullName Full legal or display name of the customer
     * @param username Unique login username
     * @param password Password for account authentication
     * @param email    Customer contact email address
     */
    public User(int userId, String fullName, String username, String password, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
    }
    // End of User constructor - initializes user credentials and profile.

    /**
     * Verifies if the provided password matches the user's password.
     *
     * @param candidatePassword The password entered by the user
     * @return true if credentials match, false otherwise
     */
    public boolean checkPassword(String candidatePassword) {
        if (candidatePassword == null || this.password == null) {
            return false;
        }
        return this.password.equals(candidatePassword);
    }
    // End of checkPassword - validates password for authentication.

    // --- Standard Getters and Setters ---

    public int getUserId() {
        return userId;
    }
    // End of getUserId getter.

    public void setUserId(int userId) {
        this.userId = userId;
    }
    // End of setUserId setter.

    public String getFullName() {
        return fullName;
    }
    // End of getFullName getter.

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    // End of setFullName setter.

    public String getUsername() {
        return username;
    }
    // End of getUsername getter.

    public void setUsername(String username) {
        this.username = username;
    }
    // End of setUsername setter.

    public String getPassword() {
        return password;
    }
    // End of getPassword getter.

    public void setPassword(String password) {
        this.password = password;
    }
    // End of setPassword setter.

    public String getEmail() {
        return email;
    }
    // End of getEmail getter.

    public void setEmail(String email) {
        this.email = email;
    }
    // End of setEmail setter.

    /**
     * Formats user details for quick overview (omits password for safety).
     */
    @Override
    public String toString() {
        return String.format("User [ID: %d, Name: %s, Username: %s, Email: %s]",
                userId, fullName, username, email);
    }
    // End of toString - safe representation of user data without sensitive password.
}