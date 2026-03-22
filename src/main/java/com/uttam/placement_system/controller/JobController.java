package com.uttam.placement_system.controller;

import com.uttam.placement_system.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired private JobService jobService;

    // Public job listings (no auth required)
    @GetMapping("/api/jobs/public")
    public ResponseEntity<?> publicJobs() {
        try { return ResponseEntity.ok(jobService.getPublicJobs()); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    // Chatbot (no auth required)
    @PostMapping("/api/chatbot/ask")
    public ResponseEntity<?> chatbot(@RequestBody Map<String, String> body) {
        try { return ResponseEntity.ok(jobService.chatbot(body.getOrDefault("question", ""))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}