package com.uttam.placement_system.controller;

import com.uttam.placement_system.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired private StudentService studentService;

    private Long userId(Authentication auth) { return (Long) auth.getCredentials(); }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication auth) {
        try { return ResponseEntity.ok(studentService.getDashboardStats(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        try { return ResponseEntity.ok(studentService.getProfile(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody Map<String, Object> body) {
        try { return ResponseEntity.ok(studentService.updateProfile(userId(auth), body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/resume")
    public ResponseEntity<?> uploadResume(Authentication auth, @RequestParam("file") MultipartFile file) {
        try {
            String url = studentService.uploadResume(userId(auth), file);
            return ResponseEntity.ok(Map.of("resumeUrl", url, "message", "Resume uploaded successfully"));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs() {
        try { return ResponseEntity.ok(studentService.getAllActiveJobs()); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/jobs/eligible")
    public ResponseEntity<?> getEligibleJobs(Authentication auth) {
        try { return ResponseEntity.ok(studentService.getEligibleJobs(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/jobs/{jobId}/eligibility")
    public ResponseEntity<?> checkEligibility(Authentication auth, @PathVariable Long jobId) {
        try { return ResponseEntity.ok(studentService.checkEligibility(userId(auth), jobId)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForJob(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            Long jobId = Long.parseLong(body.get("jobId").toString());
            String coverLetter = (String) body.get("coverLetter");
            return ResponseEntity.ok(studentService.applyForJob(userId(auth), jobId, coverLetter));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getMyApplications(Authentication auth) {
        try { return ResponseEntity.ok(studentService.getMyApplications(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}