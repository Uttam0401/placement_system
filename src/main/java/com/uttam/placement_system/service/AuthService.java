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

    @Autowired private StudentRepository studentRepo;
    @Autowired private CompanyRepository companyRepo;
    @Autowired private TpoRepository     tpoRepo;
    @Autowired private PasswordEncoder   passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.college.email.domain}")
    private String collegeDomain;

    // ===== REGISTER STUDENT =====
    public Map<String, Object> registerStudent(Map<String, Object> body) {
        String email = (String) body.get("email");

        if (!email.endsWith(collegeDomain))
            throw new RuntimeException("Student email must end with " + collegeDomain);
        if (studentRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Student student = Student.builder()
                .name((String) body.get("name"))
                .email(email)
                .password(passwordEncoder.encode((String) body.get("password")))
                .phone((String) body.get("phone"))
                .branch((String) body.get("branch"))
                .cgpa(body.get("cgpa") != null ? Double.parseDouble(body.get("cgpa").toString()) : null)
                .graduationYear(body.get("graduationYear") != null ? Integer.parseInt(body.get("graduationYear").toString()) : null)
                .skills((String) body.get("skills"))
                .linkedinUrl((String) body.get("linkedinUrl"))
                .approvalStatus(Student.ApprovalStatus.PENDING)
                .build();

        studentRepo.save(student);
        return Map.of("message", "Registration successful! Awaiting TPO approval.");
    }

    // ===== REGISTER COMPANY =====
    public Map<String, Object> registerCompany(Map<String, Object> body) {
        String email = (String) body.get("email");
        if (companyRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Company company = Company.builder()
                .name((String) body.get("name"))
                .email(email)
                .password(passwordEncoder.encode((String) body.get("password")))
                .industry((String) body.get("industry"))
                .website((String) body.get("website"))
                .description((String) body.get("description"))
                .hrName((String) body.get("hrName"))
                .phone((String) body.get("phone"))
                .location((String) body.get("location"))
                .approvalStatus(Company.ApprovalStatus.PENDING)
                .build();

        companyRepo.save(company);
        return Map.of("message", "Company registered! Awaiting TPO approval.");
    }

    // ===== REGISTER TPO =====
    public Map<String, Object> registerTpo(Map<String, Object> body) {
        String email = (String) body.get("email");
        if (tpoRepo.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        Tpo tpo = Tpo.builder()
                .name((String) body.get("name"))
                .email(email)
                .password(passwordEncoder.encode((String) body.get("password")))
                .phone((String) body.get("phone"))
                .designation((String) body.get("designation"))
                .build();

        tpoRepo.save(tpo);
        return Map.of("message", "TPO account created successfully.");
    }

    // ===== LOGIN =====
    public Map<String, Object> login(Map<String, Object> body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        return switch (role.toUpperCase()) {
            case "STUDENT" -> loginStudent(email, password);
            case "COMPANY" -> loginCompany(email, password);
            case "TPO"     -> loginTpo(email, password);
            default        -> throw new RuntimeException("Invalid role");
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

    private Map<String, Object> buildResponse(String token, String role, Long userId, String name, String email) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("token",  token);
        res.put("role",   role);
        res.put("userId", userId);
        res.put("name",   name);
        res.put("email",  email);
        return res;
    }
}