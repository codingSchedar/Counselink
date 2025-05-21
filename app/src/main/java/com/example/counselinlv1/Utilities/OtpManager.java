package com.example.counselinlv1.Utilities;

public class OtpManager {

    // Generate OTP code
    public static String generateOtp() {
        return String.valueOf((int) (Math.random() * 9000) + 1000); // Generates a random 4-digit OTP
    }

    // Send OTP via email using the existing EmailSender class
    public static void sendOtp(String recipientEmail, EmailSender.EmailCallback callback) {
        String otpCode = generateOtp(); // Generate OTP
        EmailSender.sendOtpEmail(recipientEmail, "Your OTP Code", "Your OTP code is: " + otpCode, new EmailSender.EmailCallback() {
            @Override
            public void onSuccess(String message) {
                callback.onSuccess(otpCode); // Return the OTP code on success
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e); // Propagate the failure
            }
        });
    }
}

