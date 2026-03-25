package com.uttam.placement_system.service;

import com.uttam.placement_system.model.*;
import com.uttam.placement_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class StudentService {

    @Autowired private StudentRepository     studentRepo;
    @Autowired private JobRepository         jobRepo;
    @Autowired private ApplicationRepository appRepo;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ===== PROFILE =====
    public Student getProfile(Long studentId) {
        return studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public Student updateProfile(Long studentId, Map<String, Object> body) {
        Student student = getProfile(studentId);
        if (body.get("name")           != null) student.setName((String) body.get("name"));
        if (body.get("phone")          != null) student.setPhone((String) body.get("phone"));
        if (body.get("branch")         != null) student.setBranch((String) body.get("branch"));
        if (body.get("skills")         != null) student.setSkills((String) body.get("skills"));
        if (body.get("linkedinUrl")    != null) student.setLinkedinUrl((String) body.get("linkedinUrl"));
        if (body.get("cgpa")           != null) student.setCgpa(Double.parseDouble(body.get("cgpa").toString()));
        if (body.get("graduationYear") != null) student.setGraduationYear(Integer.parseInt(body.get("graduationYear").toString()));
        return studentRepo.save(student);
    }

    // ===== RESUME UPLOAD =====
    public String uploadResume(Long studentId, MultipartFile file) throws IOException {
        if (!Objects.requireNonNull(file.getContentType()).equals("application/pdf"))
            throw new RuntimeException("Only PDF files are accepted");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("File size must be under 5MB");

        Path dir = Paths.get(uploadDir, "resumes");
        Files.createDirectories(dir);

        String filename = "student_" + studentId + "_" + System.currentTimeMillis() + ".pdf";
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String resumeUrl = "/uploads/resumes/" + filename;
        Student student = getProfile(studentId);
        student.setResumeUrl(resumeUrl);
        studentRepo.save(student);
        return resumeUrl;
    }

    // ===== JOBS =====
    public List<Job> getAllActiveJobs() {
        return jobRepo.findByStatus(Job.JobStatus.ACTIVE);
    }

    public List<Job> getEligibleJobs(Long studentId) {
        Student student = getProfile(studentId);
        return jobRepo.findEligibleJobs(student.getCgpa(), student.getBranch());
    }

    public Map<String, Object> checkEligibility(Long studentId, Long jobId) {
        Student student = getProfile(studentId);
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));

        List<String> reasons = new ArrayList<>();

        if (job.getMinimumCgpa() != null && student.getCgpa() != null && student.getCgpa() < job.getMinimumCgpa())
            reasons.add("CGPA " + student.getCgpa() + " is below minimum " + job.getMinimumCgpa());
        else if (job.getMinimumCgpa() != null && student.getCgpa() == null)
            reasons.add("CGPA not set in profile");

        if (job.getEligibleBranches() != null && !job.getEligibleBranches().isEmpty()) {
            if (student.getBranch() == null || !job.getEligibleBranches().contains(student.getBranch()))
                reasons.add("Branch " + (student.getBranch() != null ? student.getBranch() : "(not set)")
                        + " is not eligible (allowed: " + job.getEligibleBranches() + ")");
        }

        return Map.of("eligible", reasons.isEmpty(), "reasons", reasons, "job", job);
    }

    // ===== APPLY =====
    public Application applyForJob(Long studentId, Long jobId, String coverLetter) {
        Student student = getProfile(studentId);

        if (student.getApprovalStatus() != Student.ApprovalStatus.APPROVED)
            throw new RuntimeException("Your account must be approved before applying");

        Job job = jobRepo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));

        if (job.getStatus() != Job.JobStatus.ACTIVE)
            throw new RuntimeException("This job is no longer accepting applications");

        if (appRepo.existsByStudentIdAndJobId(studentId, jobId))
            throw new RuntimeException("You have already applied for this job");

        Application application = Application.builder()
                .student(student)
                .job(job)
                .coverLetter(coverLetter)
                .status(Application.ApplicationStatus.APPLIED)
                .build();

        return appRepo.save(application);
    }

    // ===== MY APPLICATIONS =====
    public List<Application> getMyApplications(Long studentId) {
        return appRepo.findByStudentId(studentId);
    }

    // ===== UPLOAD PHOTO =====
    public Map<String, Object> uploadPhoto(Long studentId, MultipartFile photo) throws IOException {
        Student student = getProfile(studentId);

        String ct = photo.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            throw new RuntimeException("Only image files are allowed.");
        if (photo.getSize() > 3 * 1024 * 1024)
            throw new RuntimeException("Image must be under 3MB.");

        String ext = photo.getOriginalFilename() != null
                ? photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf('.'))
                : ".jpg";
        String filename = "photo_" + studentId + "_" + System.currentTimeMillis() + ext;

        Path dir = Paths.get(uploadDir, "photos");
        Files.createDirectories(dir);
        Files.copy(photo.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "/uploads/photos/" + filename;
        student.setPhotoUrl(photoUrl);
        studentRepo.save(student);

        return Map.of("message", "Photo uploaded successfully", "photoUrl", photoUrl);
    }

    // ===== DASHBOARD STATS =====
    public Map<String, Object> getDashboardStats(Long studentId) {
        Student student = getProfile(studentId);
        List<Application> apps = appRepo.findByStudentId(studentId);

        long shortlisted = apps.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.SHORTLISTED).count();
        long placed      = apps.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.PLACED).count();
        long rejected    = apps.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.REJECTED).count();
        List<Job> eligible = jobRepo.findEligibleJobs(student.getCgpa(), student.getBranch());

        return Map.of(
                "studentName",     student.getName(),
                "approvalStatus",  student.getApprovalStatus(),
                "totalJobs",       jobRepo.countByStatus(Job.JobStatus.ACTIVE),
                "eligibleJobs",    eligible.size(),
                "appliedJobs",     apps.size(),
                "shortlisted",     shortlisted,
                "placed",          placed,
                "rejected",        rejected
        );
    }
}