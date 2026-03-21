package com.uttam.placement_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "minimum_cgpa")
    private Double minimumCgpa;

    @Column(name = "required_skills")
    private String requiredSkills;   // comma-separated

    @Column(name = "eligible_branches")
    private String eligibleBranches; // comma-separated: CSE,ECE,IT

    private String ctc;              // e.g. "12 LPA"

    @Column(name = "job_type")
    private String jobType;          // Full-Time, Internship, Part-Time

    private String location;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    private Integer openings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications;

    public enum JobStatus {
        ACTIVE, CLOSED, DRAFT
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}