package com.uttam.placement_system.service;

import com.uttam.placement_system.model.*;
import com.uttam.placement_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class CompanyService {

    @Autowired private CompanyRepository     companyRepo;
    @Autowired private JobRepository         jobRepo;
    @Autowired private ApplicationRepository appRepo;
    @Autowired private StudentRepository     studentRepo;
    @Autowired private EmailService          emailService;

    // ===== PROFILE =====
    public Company getProfile(Long companyId) {
        return companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public Company updateProfile(Long companyId, Map<String, Object> body) {
        Company c = getProfile(companyId);
        if (body.get("name")        != null) c.setName((String) body.get("name"));
        if (body.get("industry")    != null) c.setIndustry((String) body.get("industry"));
        if (body.get("website")     != null) c.setWebsite((String) body.get("website"));
        if (body.get("description") != null) c.setDescription((String) body.get("description"));
        if (body.get("hrName")      != null) c.setHrName((String) body.get("hrName"));
        if (body.get("phone")       != null) c.setPhone((String) body.get("phone"));
        if (body.get("location")    != null) c.setLocation((String) body.get("location"));
        return companyRepo.save(c);
    }

    // ===== JOBS =====
    public Job postJob(Long companyId, Map<String, Object> body) {
        Company company = getProfile(companyId);
        if (company.getApprovalStatus() != Company.ApprovalStatus.APPROVED)
            throw new RuntimeException("Your company must be approved before posting jobs");

        String deadlineStr = (String) body.get("applicationDeadline");
        LocalDate deadline = (deadlineStr != null && !deadlineStr.isEmpty()) ? LocalDate.parse(deadlineStr) : null;

        Job job = Job.builder()
                .company(company)
                .role((String) body.get("role"))
                .description((String) body.get("description"))
                .minimumCgpa(body.get("minimumCgpa") != null ? Double.parseDouble(body.get("minimumCgpa").toString()) : null)
                .requiredSkills((String) body.get("requiredSkills"))
                .eligibleBranches((String) body.get("eligibleBranches"))
                .ctc((String) body.get("ctc"))
                .jobType((String) body.get("jobType"))
                .location((String) body.get("location"))
                .applicationDeadline(deadline)
                .openings(body.get("openings") != null ? Integer.parseInt(body.get("openings").toString()) : null)
                .status(Job.JobStatus.valueOf((String) body.getOrDefault("status", "ACTIVE")))
                .build();

        Job saved = jobRepo.save(job);

        // Notify all approved students about new job
        notifyStudentsNewJob(saved);

        return saved;
    }

    private void notifyStudentsNewJob(Job job) {
        List<Student> students = studentRepo.findByApprovalStatus(Student.ApprovalStatus.APPROVED);
        String deadlineStr = job.getApplicationDeadline() != null ? job.getApplicationDeadline().toString() : null;
        for (Student student : students) {
            emailService.sendNewJobAlert(
                    student.getEmail(),
                    student.getName(),
                    job.getRole(),
                    job.getCompany().getName(),
                    job.getCtc(),
                    deadlineStr
            );
        }
    }

    public Job updateJob(Long companyId, Long jobId, Map<String, Object> body) {
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getCompany().getId().equals(companyId))
            throw new RuntimeException("You do not own this job");

        if (body.get("role")             != null) job.setRole((String) body.get("role"));
        if (body.get("description")      != null) job.setDescription((String) body.get("description"));
        if (body.get("ctc")              != null) job.setCtc((String) body.get("ctc"));
        if (body.get("jobType")          != null) job.setJobType((String) body.get("jobType"));
        if (body.get("location")         != null) job.setLocation((String) body.get("location"));
        if (body.get("requiredSkills")   != null) job.setRequiredSkills((String) body.get("requiredSkills"));
        if (body.get("eligibleBranches") != null) job.setEligibleBranches((String) body.get("eligibleBranches"));
        if (body.get("minimumCgpa")      != null) job.setMinimumCgpa(Double.parseDouble(body.get("minimumCgpa").toString()));
        if (body.get("openings")         != null) job.setOpenings(Integer.parseInt(body.get("openings").toString()));
        if (body.get("status")           != null) job.setStatus(Job.JobStatus.valueOf((String) body.get("status")));
        if (body.get("applicationDeadline") != null) {
            String d = (String) body.get("applicationDeadline");
            job.setApplicationDeadline(d.isEmpty() ? null : LocalDate.parse(d));
        }
        return jobRepo.save(job);
    }

    public void deleteJob(Long companyId, Long jobId) {
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getCompany().getId().equals(companyId))
            throw new RuntimeException("You do not own this job");
        jobRepo.delete(job);
    }

    public List<Job> getMyJobs(Long companyId) {
        return jobRepo.findByCompanyId(companyId);
    }

    // ===== APPLICANTS =====
    public List<Application> getApplicants(Long companyId, Long jobId) {
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getCompany().getId().equals(companyId))
            throw new RuntimeException("You do not own this job");
        return appRepo.findByJobId(jobId);
    }

    public List<Application> filterApplicants(Long companyId, Long jobId, Double minCgpa, String branch) {
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getCompany().getId().equals(companyId))
            throw new RuntimeException("You do not own this job");
        return appRepo.filterByCompany(companyId, minCgpa, branch != null && branch.isBlank() ? null : branch);
    }

    // ===== APPLICATION STATUS UPDATE + EMAIL NOTIFICATION =====
    public Application updateApplicationStatus(Long companyId, Long appId, String status, String feedback) {
        Application app = appRepo.findById(appId).orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getJob().getCompany().getId().equals(companyId))
            throw new RuntimeException("You do not own this application");

        app.setStatus(Application.ApplicationStatus.valueOf(status));
        if (feedback != null && !feedback.isBlank()) app.setFeedback(feedback);
        Application saved = appRepo.save(app);

        // Notify student of status change
        Student student    = app.getStudent();
        String  companyName = app.getJob().getCompany().getName();
        String  jobRole     = app.getJob().getRole();
        emailService.sendApplicationStatus(student.getEmail(), student.getName(), jobRole, companyName, status);

        return saved;
    }

    // ===== DASHBOARD =====
    public Map<String, Object> getDashboardStats(Long companyId) {
        List<Job> jobs = jobRepo.findByCompanyId(companyId);
        List<Application> apps = appRepo.findByJobCompanyId(companyId);

        long active      = jobs.stream().filter(j -> j.getStatus() == Job.JobStatus.ACTIVE).count();
        long shortlisted = apps.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.SHORTLISTED).count();
        long placed      = apps.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.PLACED).count();

        return Map.of(
                "totalJobs",       jobs.size(),
                "activeJobs",      active,
                "totalApplicants", apps.size(),
                "shortlisted",     shortlisted,
                "placed",          placed
        );
    }
}