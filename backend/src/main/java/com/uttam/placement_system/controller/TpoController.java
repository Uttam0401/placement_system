package com.uttam.placement_system.controller;

import com.uttam.placement_system.service.TpoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/tpo")
@CrossOrigin(origins = "*")
public class TpoController {

    @Autowired private TpoService tpoService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        try { return ResponseEntity.ok(tpoService.getAnalytics()); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> analytics() {
        try { return ResponseEntity.ok(tpoService.getAnalytics()); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    // STUDENTS
    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@RequestParam(required = false) String status) {
        try { return ResponseEntity.ok(tpoService.getAllStudents(status)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/students/{id}/approve")
    public ResponseEntity<?> approveStudent(@PathVariable Long id) {
        try { return ResponseEntity.ok(tpoService.approveStudent(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/students/{id}/reject")
    public ResponseEntity<?> rejectStudent(@PathVariable Long id) {
        try { return ResponseEntity.ok(tpoService.rejectStudent(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try { tpoService.deleteStudent(id); return ResponseEntity.ok(Map.of("message", "Student deleted")); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    // COMPANIES
    @GetMapping("/companies")
    public ResponseEntity<?> getCompanies(@RequestParam(required = false) String status) {
        try { return ResponseEntity.ok(tpoService.getAllCompanies(status)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/companies/{id}/approve")
    public ResponseEntity<?> approveCompany(@PathVariable Long id) {
        try { return ResponseEntity.ok(tpoService.approveCompany(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/companies/{id}/reject")
    public ResponseEntity<?> rejectCompany(@PathVariable Long id) {
        try { return ResponseEntity.ok(tpoService.rejectCompany(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        try { tpoService.deleteCompany(id); return ResponseEntity.ok(Map.of("message", "Company deleted")); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    // JOBS
    @GetMapping("/jobs")
    public ResponseEntity<?> getJobs() {
        try { return ResponseEntity.ok(tpoService.getAllJobs()); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/jobs/{id}/close")
    public ResponseEntity<?> closeJob(@PathVariable Long id) {
        try { return ResponseEntity.ok(tpoService.closeJob(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try { tpoService.deleteJob(id); return ResponseEntity.ok(Map.of("message", "Job deleted")); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    // APPLICATIONS
    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        try { return ResponseEntity.ok(tpoService.getAllApplications()); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}