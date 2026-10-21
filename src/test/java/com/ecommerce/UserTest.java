package com.ecommerce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("User constructor with phone number initializes all fields")
    void testConstructorWithPhone() {
        String hashedPassword = PasswordUtil.hashPassword("pass1234");
        User user = new User(1, "John Doe", "johndoe", hashedPassword, "john@example.com", "9876543210");

        assertEquals(1, user.getUserId());
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals(hashedPassword, user.getPassword());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("9876543210", user.getPhoneNumber());
    }

    @Test
    @DisplayName("User constructor without phone number defaults phone number to empty string")
    void testConstructorWithoutPhone() {
        String hashedPassword = PasswordUtil.hashPassword("pass1234");
        User user = new User(2, "Jane Doe", "janedoe", hashedPassword, "jane@example.com");

        assertEquals(2, user.getUserId());
        assertEquals("Jane Doe", user.getFullName());
        assertEquals("janedoe", user.getUsername());
        assertEquals(hashedPassword, user.getPassword());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("", user.getPhoneNumber());
    }

    @Test
    @DisplayName("checkPassword accurately verifies valid and invalid candidate passwords")
    void testCheckPassword() {
        String plainPassword = "SecureUserPass!1";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User user = new User(3, "Bob Smith", "bob", hashedPassword, "bob@test.com", "9123456780");

        assertTrue(user.checkPassword(plainPassword));
        assertFalse(user.checkPassword("WrongPassword"));
        assertFalse(user.checkPassword(""));
        assertFalse(user.checkPassword(null));
    }

    @Test
    @DisplayName("User getters and setters update fields correctly")
    void testSettersAndGetters() {
        User user = new User(10, "Initial", "initial", "pass", "init@test.com");

        user.setUserId(20);
        user.setFullName("Updated Name");
        user.setUsername("updateduser");
        user.setPassword("newpass");
        user.setEmail("new@test.com");
        user.setPhoneNumber("9998887776");

        assertEquals(20, user.getUserId());
        assertEquals("Updated Name", user.getFullName());
        assertEquals("updateduser", user.getUsername());
        assertEquals("newpass", user.getPassword());
        assertEquals("new@test.com", user.getEmail());
        assertEquals("9998887776", user.getPhoneNumber());
    }

    @Test
    @DisplayName("toString contains relevant user details")
    void testToString() {
        User user = new User(5, "Charlie Brown", "charlie", "hash", "charlie@peanuts.com", "9876543210");
        String stringVal = user.toString();

        assertTrue(stringVal.contains("userId=5"));
        assertTrue(stringVal.contains("fullName=Charlie Brown"));
        assertTrue(stringVal.contains("username=charlie"));
        assertTrue(stringVal.contains("email=charlie@peanuts.com"));
        assertTrue(stringVal.contains("phone=9876543210"));
    }
}
