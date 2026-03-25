package com.uttam.placement_system.service;

import com.uttam.placement_system.model.Student;
import com.uttam.placement_system.model.Company;
import com.uttam.placement_system.model.Tpo;
import com.uttam.placement_system.repository.StudentRepository;
import com.uttam.placement_system.repository.CompanyRepository;
import com.uttam.placement_system.repository.TpoRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Service
public class AuthService {

    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private CompanyRepository companyRepo;
    @Autowired
    private TpoRepository tpoRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OtpService otpService;
    @Autowired
    private EmailService emailService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.college.email.domain}")
    private String collegeDomain;

    // ===== REGISTER STUDENT =====
    public Map<String, Object> registerStudent(Map<String, Object> body) {
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");
        String name = (String) body.get("name");

        if (!email.endsWith(collegeDomain))
            throw new RuntimeException("Student email must end with " + collegeDomain);
        if (studentRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Student student = Student.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode((String) body.get("password")))
                .phone(phone)
                .branch((String) body.get("branch"))
                .cgpa(body.get("cgpa") != null ? Double.parseDouble(body.get("cgpa").toString()) : null)
                .graduationYear(body.get("graduationYear") != null ? Integer.parseInt(body.get("graduationYear").toString()) : null)
                .skills((String) body.get("skills"))
                .linkedinUrl((String) body.get("linkedinUrl"))
                .approvalStatus(Student.ApprovalStatus.PENDING)
                .build();

        studentRepo.save(student);

        otpService.sendRegistrationOtp(email, phone, name);
        emailService.sendWelcome(email, name, "Student");

        return Map.of(
                "message", "Registration successful! Please verify your email with the OTP sent.",
                "requiresOtp", true,
                "email", email
        );
    }

    // ===== REGISTER COMPANY =====
    public Map<String, Object> registerCompany(Map<String, Object> body) {
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");
        String name = (String) body.get("name");

        if (companyRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Company company = Company.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode((String) body.get("password")))
                .industry((String) body.get("industry"))
                .website((String) body.get("website"))
                .description((String) body.get("description"))
                .hrName((String) body.get("hrName"))
                .phone(phone)
                .location((String) body.get("location"))
                .approvalStatus(Company.ApprovalStatus.PENDING)
                .build();

        companyRepo.save(company);

        otpService.sendRegistrationOtp(email, phone, name);
        emailService.sendWelcome(email, name, "Company");

        return Map.of(
                "message", "Company registered! Please verify your email with the OTP sent.",
                "requiresOtp", true,
                "email", email
        );
    }

    // ===== REGISTER TPO =====
    public Map<String, Object> registerTpo(Map<String, Object> body) {
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");
        String name = (String) body.get("name");

        if (tpoRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Tpo tpo = Tpo.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode((String) body.get("password")))
                .phone(phone)
                .designation((String) body.get("designation"))
                .build();

        tpoRepo.save(tpo);

        otpService.sendRegistrationOtp(email, phone, name);
        emailService.sendWelcome(email, name, "TPO");

        return Map.of(
                "message", "TPO account created! Please verify your email with the OTP sent.",
                "requiresOtp", true,
                "email", email
        );
    }

    // ===== VERIFY REGISTRATION OTP =====
    public Map<String, Object> verifyRegistrationOtp(String email, String otp) {
        otpService.verifyOtp(email, otp, "REGISTRATION");
        return Map.of("message", "Email verified successfully! Awaiting TPO approval.");
    }

    // ===== RESEND OTP =====
    public Map<String, Object> resendOtp(String email, String role) {
        String name = "User";
        String phone = null;

        switch (role.toUpperCase()) {
            case "STUDENT" -> {
                Student s = studentRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Student not found"));
                name = s.getName();
                phone = s.getPhone();
            }
            case "COMPANY" -> {
                Company c = companyRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Company not found"));
                name = c.getName();
                phone = c.getPhone();
            }
            case "TPO" -> {
                Tpo t = tpoRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("TPO not found"));
                name = t.getName();
                phone = t.getPhone();
            }
        }

        otpService.sendRegistrationOtp(email, phone, name);
        return Map.of("message", "OTP resent successfully.");
    }

    // ===== LOGIN =====
    public Map<String, Object> login(Map<String, Object> body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        return switch (role.toUpperCase()) {
            case "STUDENT" -> loginStudent(email, password);
            case "COMPANY" -> loginCompany(email, password);
            case "TPO" -> loginTpo(email, password);
            default -> throw new RuntimeException("Invalid role");
        };
    }

    private Map<String, Object> loginStudent(String email, String password) {
        Student student = studentRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, student.getPassword()))
            throw new RuntimeException("Invalid email or password");

        if (student.getApprovalStatus() == Student.ApprovalStatus.PENDING)
            throw new RuntimeException("Your account is pending TPO approval");
        if (student.getApprovalStatus() == Student.ApprovalStatus.REJECTED)
            throw new RuntimeException("Your account has been rejected by TPO");

        String token = generateToken(email, "STUDENT", student.getId());
        return buildResponse(token, "STUDENT", student.getId(), student.getName(), email);
    }

    private Map<String, Object> loginCompany(String email, String password) {
        Company company = companyRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, company.getPassword()))
            throw new RuntimeException("Invalid email or password");

        if (company.getApprovalStatus() == Company.ApprovalStatus.PENDING)
            throw new RuntimeException("Your company is pending TPO approval");
        if (company.getApprovalStatus() == Company.ApprovalStatus.REJECTED)
            throw new RuntimeException("Your company registration has been rejected");

        String token = generateToken(email, "COMPANY", company.getId());
        return buildResponse(token, "COMPANY", company.getId(), company.getName(), email);
    }

    private Map<String, Object> loginTpo(String email, String password) {
        Tpo tpo = tpoRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, tpo.getPassword()))
            throw new RuntimeException("Invalid email or password");

        String token = generateToken(email, "TPO", tpo.getId());
        return buildResponse(token, "TPO", tpo.getId(), tpo.getName(), email);
    }

    // ===== FORGOT PASSWORD =====
    public Map<String, Object> forgotPassword(Map<String, Object> body) {
        String email = (String) body.get("email");
        String role = (String) body.get("role");

        boolean exists = switch (role != null ? role : "") {
            case "STUDENT" -> studentRepo.existsByEmail(email);
            case "COMPANY" -> companyRepo.existsByEmail(email);
            case "TPO" -> tpoRepo.existsByEmail(email);
            default -> studentRepo.existsByEmail(email) || companyRepo.existsByEmail(email);
        };

        // Always return success to prevent email enumeration
        if (exists) {
            String name = switch (role != null ? role : "") {
                case "COMPANY" -> companyRepo.findByEmail(email).map(c -> c.getName()).orElse("User");
                case "TPO" -> tpoRepo.findByEmail(email).map(t -> t.getName()).orElse("User");
                default -> studentRepo.findByEmail(email).map(s -> s.getName()).orElse("User");
            };
            otpService.sendPasswordResetOtp(email, null, name);
        }

        return Map.of("message", "If the email exists, an OTP has been sent.");
    }

    // ===== VERIFY RESET OTP =====
    public Map<String, Object> verifyResetOtp(Map<String, Object> body) {
        String email = (String) body.get("email");
        String otp = (String) body.get("otp");
        boolean valid = otpService.verifyOtp(email, otp, "PASSWORD_RESET");
        if (!valid) throw new RuntimeException("Invalid or expired OTP");
        String resetToken = generateResetToken(email);
        return Map.of("message", "OTP verified", "resetToken", resetToken);
    }

    // ===== RESET PASSWORD =====
    public Map<String, Object> resetPassword(Map<String, Object> body) {
        String email = (String) body.get("email");
        String role = (String) body.get("role");
        String resetToken = (String) body.get("resetToken");
        String newPassword = (String) body.get("newPassword");

        if (!validateResetToken(email, resetToken))
            throw new RuntimeException("Invalid or expired reset session. Please start over.");

        String encoded = passwordEncoder.encode(newPassword);
        switch (role != null ? role : "") {
            case "COMPANY" -> companyRepo.findByEmail(email).ifPresent(c -> {
                c.setPassword(encoded);
                companyRepo.save(c);
            });
            case "TPO" -> tpoRepo.findByEmail(email).ifPresent(t -> {
                t.setPassword(encoded);
                tpoRepo.save(t);
            });
            default -> studentRepo.findByEmail(email).ifPresent(s -> {
                s.setPassword(encoded);
                studentRepo.save(s);
            });
        }

        return Map.of("message", "Password reset successfully.");
    }

    // ===== GENERATE TOKEN =====
    private String generateToken(String email, String role, Long userId) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===== GENERATE RESET TOKEN =====
    private String generateResetToken(String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email)
                .claim("purpose", "password_reset")
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===== VALIDATE RESET TOKEN =====
    private boolean validateResetToken(String email, String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            return email.equals(claims.getSubject()) && "password_reset".equals(claims.get("purpose"));
        } catch (Exception e) {
            return false;
        }
    }

    // ===== BUILD RESPONSE =====
    private Map<String, Object> buildResponse(String token, String role, Long userId, String name, String email) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("token", token);
        res.put("role", role);
        res.put("userId", userId);
        res.put("name", name);
        res.put("email", email);
        return res;
    }
}