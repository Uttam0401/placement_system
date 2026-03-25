package com.uttam.placement_system.controller;

import com.uttam.placement_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Campus Placement API"));
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.registerStudent(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/company")
    public ResponseEntity<?> registerCompany(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.registerCompany(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/tpo")
    public ResponseEntity<?> registerTpo(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.registerTpo(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== NEW: Verify OTP after registration =====
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            String otp = (String) body.get("otp");
            return ResponseEntity.ok(authService.verifyRegistrationOtp(email, otp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== NEW: Resend OTP =====
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            String role = (String) body.get("role");
            return ResponseEntity.ok(authService.resendOtp(email, role));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.login(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== FORGOT PASSWORD =====
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.forgotPassword(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.verifyResetOtp(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(authService.resetPassword(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}