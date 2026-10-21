package com.ecommerce;

// User entity class representing registered customer
public class User {

    private int userId;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;

    // Parameterized constructor with phone number
    public User(int userId, String fullName, String username, String password, String email, String phoneNumber) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Overloaded constructor without phone number
    public User(int userId, String fullName, String username, String password, String email) {
        this(userId, fullName, username, password, email, "");
    }

    // Verifies the entered password against the stored password hash.
    public boolean checkPassword(String candidatePassword) {
        return PasswordUtil.verifyPassword(candidatePassword, this.password);
    }

    // Getters and Setters

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", fullName=" + fullName + ", username=" + username + ", email=" + email + ", phone=" + phoneNumber + "]";
    }
}