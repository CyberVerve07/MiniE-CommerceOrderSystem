package com.ecommerce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("hashPassword creates a valid salt:hash format with two Base64 encoded parts")
    void testHashPasswordFormat() {
        String password = "SecretPassword123";
        String hashed = PasswordUtil.hashPassword(password);

        assertNotNull(hashed);
        String[] parts = hashed.split(":", -1);
        assertEquals(2, parts.length);
        assertFalse(parts[0].isEmpty());
        assertFalse(parts[1].isEmpty());
    }

    @Test
    @DisplayName("hashPassword generates distinct hashes for the same password due to random salt")
    void testHashPasswordGeneratesUniqueSalts() {
        String password = "Password@2024";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }

    @Test
    @DisplayName("verifyPassword returns true for correct candidate password")
    void testVerifyPasswordSuccess() {
        String password = "MySecurePassword!";
        String hashed = PasswordUtil.hashPassword(password);

        assertTrue(PasswordUtil.verifyPassword(password, hashed));
    }

    @Test
    @DisplayName("verifyPassword returns false for wrong candidate password")
    void testVerifyPasswordFailure() {
        String password = "CorrectPassword";
        String hashed = PasswordUtil.hashPassword(password);

        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hashed));
        assertFalse(PasswordUtil.verifyPassword("correctpassword", hashed)); // case-sensitive
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("verifyPassword returns false when password is null or storedHash is null/empty")
    void testVerifyPasswordNullOrEmpty(String input) {
        String validHash = PasswordUtil.hashPassword("testPassword");
        assertFalse(PasswordUtil.verifyPassword(input, validHash));
        assertFalse(PasswordUtil.verifyPassword("testPassword", input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidHashNoColon", "part1:part2:part3", "notBase64:notBase64!@#", "abc:"})
    @DisplayName("verifyPassword returns false safely for malformed stored hashes")
    void testVerifyPasswordMalformedHash(String malformedHash) {
        assertFalse(PasswordUtil.verifyPassword("password", malformedHash));
    }
}
