package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
    }

    @RepeatedTest(10)
    @DisplayName("generateOtp returns a 4-digit number between 1000 and 9999")
    void testGenerateOtp() {
        String otp = otpService.generateOtp();

        assertNotNull(otp);
        assertEquals(4, otp.length());
        assertTrue(otp.matches("\\d{4}"));

        int numericOtp = Integer.parseInt(otp);
        assertTrue(numericOtp >= 1000 && numericOtp <= 9999);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "1234567890", " 9876543210 "})
    @DisplayName("validatePhoneNumber returns true for valid 10-digit phone numbers")
    void testValidatePhoneNumberValid(String phone) {
        assertTrue(otpService.validatePhoneNumber(phone));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "12345678901", "abcdefghij", "98765-4321", "98765 4321"})
    @DisplayName("validatePhoneNumber returns false for invalid phone numbers or formats")
    void testValidatePhoneNumberInvalid(String phone) {
        assertFalse(otpService.validatePhoneNumber(phone));
    }

    @Test
    @DisplayName("verifyOtp returns true when entered OTP matches actual OTP")
    void testVerifyOtpSuccess() {
        assertTrue(otpService.verifyOtp("4321", "4321"));
        assertTrue(otpService.verifyOtp(" 4321 ", "4321")); // trimmed match
    }

    @Test
    @DisplayName("verifyOtp returns false when OTP does not match")
    void testVerifyOtpFailure() {
        assertFalse(otpService.verifyOtp("1234", "4321"));
        assertFalse(otpService.verifyOtp("0000", "4321"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("verifyOtp returns false when entered OTP is null or empty")
    void testVerifyOtpNullOrEmpty(String enteredOtp) {
        assertFalse(otpService.verifyOtp(enteredOtp, "5678"));
    }

    @Test
    @DisplayName("sendOtp executes SMS consumer simulation without throwing exception")
    void testSendOtp() {
        assertDoesNotThrow(() -> otpService.sendOtp("9876543210", "1234"));
    }
}
