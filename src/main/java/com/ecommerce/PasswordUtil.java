package com.ecommerce;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.Base64;

public class PasswordUtil {

    private static final int ITERATIONS = 120_000;

    // Custom method: password ka salted hash banana hai.
    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = createHash(password, salt);

        // Salt aur hash ko database mein store karne ke liye jodte hain.
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    // Custom method: login ke waqt entered password verify karta hai.
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split(":", -1);

        if (parts.length != 2) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = createHash(password, salt);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Custom helper method: Java ki cryptography API se password hash karta hai.
    private static byte[] createHash(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, 256);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }
}