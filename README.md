# 🎓 Campus Placement Management System

A full-stack campus placement platform built with **Spring Boot** (Java 17) + **MySQL** backend and a clean **HTML/CSS/Vanilla JS** frontend. Designed by **Uttam**.

---

## 📁 Project Structure

```
placement-system/
├── backend/                     Spring Boot Application
│   ├── src/main/java/com/uttam/placement/
│   │   ├── controller/          REST API Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── StudentController.java
│   │   │   ├── CompanyController.java
│   │   │   ├── TpoController.java
│   │   │   └── JobController.java
│   │   ├── service/             Business Logic
│   │   │   ├── AuthService.java
│   │   │   ├── StudentService.java
│   │   │   ├── CompanyService.java
│   │   │   ├── TpoService.java
│   │   │   └── JobService.java
│   │   ├── repository/          Spring Data JPA
│   │   │   ├── StudentRepository.java
│   │   │   ├── CompanyRepository.java
│   │   │   ├── JobRepository.java
│   │   │   ├── ApplicationRepository.java
│   │   │   └── TpoRepository.java
│   │   ├── model/               JPA Entities
│   │   │   ├── Student.java
│   │   │   ├── Company.java
│   │   │   ├── Job.java
│   │   │   ├── Application.java
│   │   │   └── Tpo.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java  (JWT + Spring Security)
│   │   └── PlacementSystemApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/
│   ├── index.html               Landing page
│   ├── auth/
│   │   ├── login.html
│   │   ├── signup-student.html
│   │   ├── signup-company.html
│   │   └── signup-tpo.html
│   ├── student/
│   │   ├── dashboard.html
│   │   ├── jobs.html
│   │   ├── applications.html
│   │   ├── profile.html
│   │   └── chatbot.html
│   ├── company/
│   │   ├── dashboard.html
│   │   ├── post-job.html
│   │   ├── applicants.html
│   │   └── shortlist.html
│   ├── tpo/
│   │   ├── dashboard.html
│   │   ├── students.html
│   │   ├── companies.html
│   │   ├── jobs.html
│   │   └── reports.html
│   └── assets/
│       ├── css/styles.css
│       └── js/utils.js
│
├── database/
│   └── schema.sql
│
├── README.md
└── .gitignore
```

---

## 🚀 Quick Start

### 1. Database Setup

```bash
mysql -u root -p < database/schema.sql
```

This creates the `placement_system` database with all tables and sample data including:
- Default TPO: `tpo@college.edu` / `admin123`
- 3 approved companies with sample jobs

### 2. Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/placement_system
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run Backend

```bash
cd backend
mvn spring-boot:run
```

API will start at **https://placementsystem-production.up.railway.app**

Health check: `GET https://placementsystem-production.up.railway.app/api/auth/health`

### 4. Run Frontend

Open `frontend/index.html` in a browser, or use Live Server:

```bash
cd frontend
npx serve .
# → http://localhost:3000
```

---

## 🔑 Default Credentials

| Role    | Email                 | Password     |
|---------|-----------------------|--------------|
| TPO     | tpo@college.edu       | admin123     |
| Company | hr@techcorp.com       | company123   |
| Company | campus@infosys.com    | company123   |
| Company | recruit@finserve.com  | company123   |

> Students must register with `@college.edu` email and await TPO approval.

---

## 🎭 Role Features

### 👨‍🎓 Student
| Feature | Description |
|---------|-------------|
| Register | Email must end with `@college.edu` |
| Browse Jobs | Filter by branch, type, keyword |
| Eligible Jobs | Auto-filtered by CGPA + branch |
| Apply | One-click with optional cover letter |
| Track Applications | Real-time status: Applied → Shortlisted → Placed |
| Upload Resume | PDF only, max 5MB |
| AI Chatbot | Placement guidance assistant |
| Profile | Update CGPA, skills, LinkedIn |

### 🏢 Company (HR)
| Feature | Description |
|---------|-------------|
| Post Jobs | Role, CTC, min CGPA, branches, skills, deadline |
| Save as Draft | Publish later |
| View Applicants | Per-job applicant list |
| Filter Applicants | By CGPA and branch |
| Update Status | Shortlist / Reject / Place with feedback |
| Bulk Actions | Shortlist or reject multiple at once |

### 🎯 TPO (Admin)
| Feature | Description |
|---------|-------------|
| Approve/Reject | Student and company registrations |
| All Jobs | View, close, or delete any job |
| All Applications | Track every application |
| Analytics Dashboard | Placement rate, branch-wise stats |
| Reports | Full placed students list with CTC |

---

## 🔌 API Reference

### Auth (Public)
```
POST /api/auth/register/student
POST /api/auth/register/company
POST /api/auth/register/tpo
POST /api/auth/login
GET  /api/auth/health
GET  /api/jobs/public
POST /api/chatbot/ask
```

### Student `[ROLE_STUDENT]`
```
GET  /api/student/dashboard
GET  /api/student/profile
PUT  /api/student/profile
POST /api/student/resume
GET  /api/student/jobs
GET  /api/student/jobs/eligible
GET  /api/student/jobs/{id}/eligibility
POST /api/student/apply
GET  /api/student/applications
```

### Company `[ROLE_COMPANY]`
```
GET    /api/company/dashboard
GET    /api/company/profile
PUT    /api/company/profile
POST   /api/company/jobs
GET    /api/company/jobs
PUT    /api/company/jobs/{id}
DELETE /api/company/jobs/{id}
GET    /api/company/jobs/{id}/applicants?minCgpa=&branch=
PUT    /api/company/applications/{id}/status
```

### TPO `[ROLE_TPO]`
```
GET    /api/tpo/dashboard
GET    /api/tpo/analytics
GET    /api/tpo/students?status=PENDING
PUT    /api/tpo/students/{id}/approve
PUT    /api/tpo/students/{id}/reject
DELETE /api/tpo/students/{id}
GET    /api/tpo/companies?status=PENDING
PUT    /api/tpo/companies/{id}/approve
PUT    /api/tpo/companies/{id}/reject
DELETE /api/tpo/companies/{id}
GET    /api/tpo/jobs
PUT    /api/tpo/jobs/{id}/close
DELETE /api/tpo/jobs/{id}
GET    /api/tpo/applications
```

---

## 🔒 Security

- **JWT** tokens (24h expiry) stored in `localStorage` as `ps_token`
- **BCrypt** password hashing
- **Role-based** access via Spring Security (`ROLE_STUDENT`, `ROLE_COMPANY`, `ROLE_TPO`)
- **College domain validation** — students must register with `@college.edu`
- **Unique constraint** on `(student_id, job_id)` prevents duplicate applications
- **File upload validation** — PDF only, 5MB max

---

## 🛠 Tech Stack

| Layer     | Technology                      |
|-----------|---------------------------------|
| Backend   | Spring Boot 3.2, Java 17        |
| ORM       | Spring Data JPA + Hibernate     |
| Database  | MySQL 8.0                       |
| Auth      | JWT (jjwt 0.11.5) + BCrypt      |
| Frontend  | HTML5, CSS3, Vanilla JS         |
| Charts    | Chart.js 4.x                    |
| Fonts     | Space Grotesk + Sora            |

---

## 📞 Contact

Built by **Uttam** as a full-stack campus placement project.
