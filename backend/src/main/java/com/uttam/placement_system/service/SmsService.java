package com.uttam.placement_system.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendOtp(String toPhone, String otp, String purpose) {
        // SMS not configured — Email OTP is used instead
        System.out.println("SMS skipped for: " + toPhone);
    }
}