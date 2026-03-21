-- ============================================================
-- Campus Placement Management System
-- Database: placement_system  |  MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS placement_system;
USE placement_system;

-- ============================================================
-- TPO ADMINS
-- ============================================================
CREATE TABLE IF NOT EXISTS tpo_admins (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    designation VARCHAR(100),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- STUDENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS students (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE COMMENT 'Must end with @college.edu',
    password         VARCHAR(255) NOT NULL,
    phone            VARCHAR(20),
    branch           VARCHAR(50)  COMMENT 'CSE, ECE, IT, ME, CE, EEE',
    cgpa             DECIMAL(4,2) CHECK (cgpa BETWEEN 0 AND 10),
    graduation_year  INT,
    skills           TEXT         COMMENT 'Comma-separated: Java, Python, SQL',
    resume_url       VARCHAR(500),
    linkedin_url     VARCHAR(500),
    approval_status  ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email  (email),
    INDEX idx_status (approval_status),
    INDEX idx_cgpa   (cgpa),
    INDEX idx_branch (branch)
);

-- ============================================================
-- COMPANIES
-- ============================================================
CREATE TABLE IF NOT EXISTS companies (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    industry         VARCHAR(100),
    website          VARCHAR(300),
    description      TEXT,
    hr_name          VARCHAR(100),
    phone            VARCHAR(20),
    location         VARCHAR(200),
    approval_status  ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email  (email),
    INDEX idx_status (approval_status)
);

-- ============================================================
-- JOBS
-- ============================================================
CREATE TABLE IF NOT EXISTS jobs (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id           BIGINT NOT NULL,
    role                 VARCHAR(150) NOT NULL,
    description          TEXT,
    minimum_cgpa         DECIMAL(4,2),
    required_skills      TEXT         COMMENT 'Comma-separated',
    eligible_branches    VARCHAR(300) COMMENT 'Comma-separated: CSE,ECE,IT',
    ctc                  VARCHAR(50)  COMMENT 'e.g. 12 LPA',
    job_type             VARCHAR(50)  COMMENT 'Full-Time | Internship | Part-Time',
    location             VARCHAR(200),
    application_deadline DATE,
    openings             INT DEFAULT 1,
    status               ENUM('ACTIVE','CLOSED','DRAFT') DEFAULT 'ACTIVE',
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_company (company_id),
    INDEX idx_status  (status)
);

-- ============================================================
-- APPLICATIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS applications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    job_id       BIGINT NOT NULL,
    status       ENUM('APPLIED','UNDER_REVIEW','SHORTLISTED','REJECTED','PLACED') DEFAULT 'APPLIED',
    cover_letter TEXT,
    feedback     TEXT,
    applied_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uq_student_job (student_id, job_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id)     REFERENCES jobs(id)     ON DELETE CASCADE,

    INDEX idx_student (student_id),
    INDEX idx_job     (job_id),
    INDEX idx_status  (status)
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default TPO  (password: admin123)
INSERT IGNORE INTO tpo_admins (name, email, password, phone, designation) VALUES
('Dr. Uttam Kumar',
 'tpo@college.edu',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyQnLb9Ba',
 '9876543210',
 'Training & Placement Officer');

-- Sample Companies  (password: company123)
INSERT IGNORE INTO companies (name, email, password, industry, website, hr_name, phone, location, approval_status) VALUES
('TechCorp Solutions',
 'hr@techcorp.com',
 '$2a$10$8K1p/a0dR1xqM2LDsSpbsOsVFtjbScNyqdNT5NxCEkq4LpHFGKS.e',
 'Information Technology', 'https://techcorp.com',
 'Priya Sharma', '9123456789', 'Bangalore', 'APPROVED'),

('Infosys Limited',
 'campus@infosys.com',
 '$2a$10$8K1p/a0dR1xqM2LDsSpbsOsVFtjbScNyqdNT5NxCEkq4LpHFGKS.e',
 'IT Services', 'https://infosys.com',
 'Amit Patel', '9234567890', 'Pune', 'APPROVED'),

('FinServe Analytics',
 'recruit@finserve.com',
 '$2a$10$8K1p/a0dR1xqM2LDsSpbsOsVFtjbScNyqdNT5NxCEkq4LpHFGKS.e',
 'Finance & Analytics', 'https://finserve.com',
 'Neha Gupta', '9345678901', 'Mumbai', 'APPROVED');

-- Sample Jobs
INSERT IGNORE INTO jobs
  (company_id, role, description, minimum_cgpa, required_skills, eligible_branches, ctc, job_type, location, openings, status)
SELECT c.id,
  'Software Engineer',
  'Build scalable backend services using Java and Spring Boot. Work with modern cloud infrastructure.',
  7.5, 'Java, Spring Boot, SQL, REST APIs', 'CSE,IT,ECE',
  '12 LPA', 'Full-Time', 'Bangalore', 10, 'ACTIVE'
FROM companies c WHERE c.email='hr@techcorp.com' LIMIT 1;

INSERT IGNORE INTO jobs
  (company_id, role, description, minimum_cgpa, required_skills, eligible_branches, ctc, job_type, location, openings, status)
SELECT c.id,
  'Frontend Developer',
  'Create beautiful and performant web interfaces using React and modern JS.',
  7.0, 'React, JavaScript, HTML, CSS', 'CSE,IT',
  '10 LPA', 'Full-Time', 'Bangalore', 5, 'ACTIVE'
FROM companies c WHERE c.email='hr@techcorp.com' LIMIT 1;

INSERT IGNORE INTO jobs
  (company_id, role, description, minimum_cgpa, required_skills, eligible_branches, ctc, job_type, location, openings, status)
SELECT c.id,
  'Systems Engineer',
  'Join Infosys as a Systems Engineer. Training provided. Excellent growth opportunities.',
  6.0, 'Java, C++, Problem Solving', 'CSE,ECE,ME,EEE,IT',
  '6.5 LPA', 'Full-Time', 'PAN India', 50, 'ACTIVE'
FROM companies c WHERE c.email='campus@infosys.com' LIMIT 1;

INSERT IGNORE INTO jobs
  (company_id, role, description, minimum_cgpa, required_skills, eligible_branches, ctc, job_type, location, openings, status)
SELECT c.id,
  'Data Analyst Intern',
  'Analyze financial datasets to derive business insights. Python and SQL skills essential.',
  6.5, 'Python, SQL, Excel, Statistics', 'CSE,IT,ECE,EEE',
  '40000/month', 'Internship', 'Mumbai', 8, 'ACTIVE'
FROM companies c WHERE c.email='recruit@finserve.com' LIMIT 1;

-- ============================================================
-- USEFUL REPORTING QUERIES (for reference)
-- ============================================================

-- Placement statistics
-- SELECT COUNT(DISTINCT a.student_id) AS placed,
--        ROUND(COUNT(DISTINCT a.student_id)*100.0/(SELECT COUNT(*) FROM students WHERE approval_status='APPROVED'),1) AS placement_pct
-- FROM applications a WHERE a.status = 'PLACED';

-- Branch-wise placements
-- SELECT s.branch, COUNT(*) AS placed
-- FROM applications a JOIN students s ON a.student_id = s.id
-- WHERE a.status = 'PLACED' GROUP BY s.branch ORDER BY placed DESC;

-- Top companies by applications
-- SELECT c.name, COUNT(a.id) AS applicants
-- FROM applications a JOIN jobs j ON a.job_id = j.id JOIN companies c ON j.company_id = c.id
-- GROUP BY c.name ORDER BY applicants DESC;
