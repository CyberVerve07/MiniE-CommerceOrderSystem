package com.ecommerce;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

// OTP service demonstrating Java 8 Consumer and Predicate functional interfaces
public class OtpService {

    private final Random random = new Random();

    // Generate random 4-digit OTP
    // Consumers is a functional interface that take the Input and Perform the
    // Operation or return noting::

    public String generateOtp() {
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }
    // Predicate to validate phone number format (must be 10 digits)
    public boolean validatePhoneNumber(String phone) {
        Predicate<String> phonePredicate = p -> p != null && p.trim().matches("\\d{10}");
        return phonePredicate.test(phone);
    }

    // When user is entert their mobno and it take it and :::
    // Consumer to simulate sending OTP via SMS
    public void sendOtp(String phone, String otp) {
        Consumer<String> smsConsumer = targetPhone -> {
            System.out.println("----------------------------------------");
            System.out.println("[SMS GATEWAY] Sending OTP to: " + targetPhone);
            System.out.println("Your 4-Digit Verification Code is: " + otp);
            System.out.println("----------------------------------------");
        };
        smsConsumer.accept(phone);
    }
    // Predicate to verify if entered OTP matches generated OTP
    // Checks weathe The otp :::

    public boolean verifyOtp(String enteredOtp, String actualOtp) {
        Predicate<String> otpPredicate = input -> input != null && input.trim().equals(actualOtp);
        return otpPredicate.test(enteredOtp);
    }
}
