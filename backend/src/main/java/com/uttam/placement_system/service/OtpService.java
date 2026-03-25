package com.uttam.placement_system.service;

import com.uttam.placement_system.model.OtpVerification;
import com.uttam.placement_system.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired private OtpRepository otpRepo;
    @Autowired private EmailService  emailService;
    @Autowired private SmsService    smsService;

    private final SecureRandom random = new SecureRandom();

    // In-memory cache for last OTP per email (for password reset flow)
    private final ConcurrentHashMap<String, String> lastOtpCache = new ConcurrentHashMap<>();

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    // ===== REGISTRATION OTP =====
    public void sendRegistrationOtp(String email, String phone, String name) {
        String otp = generateAndSave(email, "REGISTRATION");
        emailService.sendOtp(email, name, otp, "Registration");
        if (phone != null && !phone.isBlank()) {
            smsService.sendOtp(phone, otp, "Registration");
        }
    }

    // ===== PASSWORD RESET OTP =====
    public void sendPasswordResetOtp(String email, String phone, String name) {
        String otp = generateAndSave(email, "PASSWORD_RESET");
        lastOtpCache.put(email, otp);
        emailService.sendPasswordResetOtp(email, name, otp);
        if (phone != null && !phone.isBlank()) {
            smsService.sendOtp(phone, otp, "Password Reset");
        }
    }

    // Get last generated OTP for an email (used to pass to email service if needed)
    public String getLastOtp(String email) {
        return lastOtpCache.getOrDefault(email, "");
    }

    private String generateAndSave(String email, String type) {
        String otp = generateOtp();
        otpRepo.deleteByIdentifierAndOtpType(email, type);
        OtpVerification record = OtpVerification.builder()
                .identifier(email)
                .otpType(type)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();
        otpRepo.save(record);
        return otp;
    }

    // ===== VERIFY OTP (with explicit type) =====
    public boolean verifyOtp(String email, String otpInput, String otpType) {
        OtpVerification record = otpRepo
                .findTopByIdentifierAndOtpTypeAndVerifiedFalseOrderByCreatedAtDesc(email, otpType)
                .orElseThrow(() -> new RuntimeException("OTP not found or already used. Please request a new one."));

        if (record.isExpired())
            throw new RuntimeException("OTP has expired. Please request a new one.");

        if (!record.getOtp().equals(otpInput.trim()))
            throw new RuntimeException("Invalid OTP. Please try again.");

        record.setVerified(true);
        otpRepo.save(record);
        lastOtpCache.remove(email);
        return true;
    }

    // ===== VERIFY OTP (auto-detects type: REGISTRATION or PASSWORD_RESET) =====
    public boolean verifyOtp(String email, String otpInput) {
        // Try PASSWORD_RESET first (for forgot-password flow), then REGISTRATION
        for (String type : new String[]{"PASSWORD_RESET", "REGISTRATION"}) {
            try {
                return verifyOtp(email, otpInput, type);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Invalid OTP")) throw e;
                // Not found for this type, try next
            }
        }
        throw new RuntimeException("OTP not found. Please request a new one.");
    }

    // ===== RESEND OTP =====
    public void resendOtp(String email, String phone, String name, String otpType) {
        if ("PASSWORD_RESET".equals(otpType)) {
            sendPasswordResetOtp(email, phone, name);
        } else {
            sendRegistrationOtp(email, phone, name);
        }
    }

    // ===== CLEANUP EXPIRED OTPs every hour =====
    @Scheduled(fixedRate = 3_600_000)
    public void cleanupExpired() {
        otpRepo.deleteExpired(LocalDateTime.now());
    }
}
