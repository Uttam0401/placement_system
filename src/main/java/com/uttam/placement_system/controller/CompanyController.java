package com.uttam.placement_system.controller;

import com.uttam.placement_system.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@CrossOrigin(origins = "*")
public class CompanyController {

    @Autowired private CompanyService companyService;

    private Long userId(Authentication auth) { return (Long) auth.getCredentials(); }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication auth) {
        try { return ResponseEntity.ok(companyService.getDashboardStats(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        try { return ResponseEntity.ok(companyService.getProfile(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody Map<String, Object> body) {
        try { return ResponseEntity.ok(companyService.updateProfile(userId(auth), body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/jobs")
    public ResponseEntity<?> postJob(Authentication auth, @RequestBody Map<String, Object> body) {
        try { return ResponseEntity.ok(companyService.postJob(userId(auth), body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getMyJobs(Authentication auth) {
        try { return ResponseEntity.ok(companyService.getMyJobs(userId(auth))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/jobs/{jobId}")
    public ResponseEntity<?> updateJob(Authentication auth, @PathVariable Long jobId,
                                       @RequestBody Map<String, Object> body) {
        try { return ResponseEntity.ok(companyService.updateJob(userId(auth), jobId, body)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> deleteJob(Authentication auth, @PathVariable Long jobId) {
        try { companyService.deleteJob(userId(auth), jobId);
            return ResponseEntity.ok(Map.of("message", "Job deleted")); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/jobs/{jobId}/applicants")
    public ResponseEntity<?> getApplicants(Authentication auth, @PathVariable Long jobId,
                                           @RequestParam(required = false) Double minCgpa,
                                           @RequestParam(required = false) String branch) {
        try {
            if (minCgpa != null || (branch != null && !branch.isBlank()))
                return ResponseEntity.ok(companyService.filterApplicants(userId(auth), jobId, minCgpa, branch));
            return ResponseEntity.ok(companyService.getApplicants(userId(auth), jobId));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/applications/{appId}/status")
    public ResponseEntity<?> updateApplicationStatus(Authentication auth, @PathVariable Long appId,
                                                     @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(companyService.updateApplicationStatus(
                    userId(auth), appId, (String) body.get("status"), (String) body.get("feedback")));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}