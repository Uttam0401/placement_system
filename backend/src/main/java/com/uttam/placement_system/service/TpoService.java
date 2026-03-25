package com.uttam.placement_system.service;

import com.uttam.placement_system.model.*;
import com.uttam.placement_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TpoService {

    @Autowired private StudentRepository     studentRepo;
    @Autowired private CompanyRepository     companyRepo;
    @Autowired private JobRepository         jobRepo;
    @Autowired private ApplicationRepository appRepo;
    @Autowired private TpoRepository         tpoRepo;
    @Autowired private EmailService          emailService;

    // ===== STUDENTS =====
    public List<Student> getAllStudents(String status) {
        if (status != null && !status.isBlank())
            return studentRepo.findByApprovalStatus(Student.ApprovalStatus.valueOf(status.toUpperCase()));
        return studentRepo.findAll();
    }

    public Student approveStudent(Long id) {
        Student s = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        s.setApprovalStatus(Student.ApprovalStatus.APPROVED);
        studentRepo.save(s);
        // Notify student
        emailService.sendAccountApproved(s.getEmail(), s.getName(), "Student");
        return s;
    }

    public Student rejectStudent(Long id) {
        Student s = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        s.setApprovalStatus(Student.ApprovalStatus.REJECTED);
        studentRepo.save(s);
        // Notify student
        emailService.sendAccountRejected(s.getEmail(), s.getName(), "Student");
        return s;
    }

    public void deleteStudent(Long id) {
        if (!studentRepo.existsById(id)) throw new RuntimeException("Student not found");
        studentRepo.deleteById(id);
    }

    // ===== COMPANIES =====
    public List<Company> getAllCompanies(String status) {
        if (status != null && !status.isBlank())
            return companyRepo.findByApprovalStatus(Company.ApprovalStatus.valueOf(status.toUpperCase()));
        return companyRepo.findAll();
    }

    public Company approveCompany(Long id) {
        Company c = companyRepo.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));
        c.setApprovalStatus(Company.ApprovalStatus.APPROVED);
        companyRepo.save(c);
        // Notify company
        emailService.sendAccountApproved(c.getEmail(), c.getName(), "Company");
        return c;
    }

    public Company rejectCompany(Long id) {
        Company c = companyRepo.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));
        c.setApprovalStatus(Company.ApprovalStatus.REJECTED);
        companyRepo.save(c);
        // Notify company
        emailService.sendAccountRejected(c.getEmail(), c.getName(), "Company");
        return c;
    }

    public void deleteCompany(Long id) {
        if (!companyRepo.existsById(id)) throw new RuntimeException("Company not found");
        companyRepo.deleteById(id);
    }

    // ===== JOBS =====
    public List<Job> getAllJobs() {
        return jobRepo.findAll();
    }

    public Job closeJob(Long id) {
        Job job = jobRepo.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(Job.JobStatus.CLOSED);
        return jobRepo.save(job);
    }

    public void deleteJob(Long id) {
        if (!jobRepo.existsById(id)) throw new RuntimeException("Job not found");
        jobRepo.deleteById(id);
    }

    // ===== APPLICATIONS =====
    public List<Application> getAllApplications() {
        return appRepo.findAll();
    }

    // ===== ANALYTICS =====
    public Map<String, Object> getAnalytics() {
        long totalStudents     = studentRepo.count();
        long pendingStudents   = studentRepo.countPending();
        long approvedStudents  = studentRepo.findByApprovalStatus(Student.ApprovalStatus.APPROVED).size();
        long totalCompanies    = companyRepo.count();
        long pendingCompanies  = companyRepo.countByApprovalStatus(Company.ApprovalStatus.PENDING);
        long approvedCompanies = companyRepo.countByApprovalStatus(Company.ApprovalStatus.APPROVED);
        long activeJobs        = jobRepo.countByStatus(Job.JobStatus.ACTIVE);
        long totalJobs         = jobRepo.count();
        long totalApplications = appRepo.count();
        long placedStudents    = appRepo.countPlacedStudents();

        Map<String, Long> appsByStatus = new LinkedHashMap<>();
        for (Object[] row : appRepo.countByStatusGrouped())
            appsByStatus.put(row[0].toString(), (Long) row[1]);

        Map<String, Long> placedByBranch = new LinkedHashMap<>();
        for (Object[] row : appRepo.countPlacedByBranch()) {
            String branch = row[0] != null ? row[0].toString() : "Unknown";
            placedByBranch.put(branch, (Long) row[1]);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalStudents",        totalStudents);
        result.put("pendingStudents",      pendingStudents);
        result.put("approvedStudents",     approvedStudents);
        result.put("totalCompanies",       totalCompanies);
        result.put("pendingCompanies",     pendingCompanies);
        result.put("approvedCompanies",    approvedCompanies);
        result.put("activeJobs",           activeJobs);
        result.put("totalJobs",            totalJobs);
        result.put("totalApplications",    totalApplications);
        result.put("placedStudents",       placedStudents);
        result.put("applicationsByStatus", appsByStatus);
        result.put("placedByBranch",       placedByBranch);
        return result;
    }
}