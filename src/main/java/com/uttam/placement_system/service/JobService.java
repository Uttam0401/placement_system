package com.uttam.placement_system.service;

import com.uttam.placement_system.model.Job;
import com.uttam.placement_system.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JobService {

    @Autowired private JobRepository jobRepo;

    public List<Job> getPublicJobs() {
        return jobRepo.findByStatus(Job.JobStatus.ACTIVE);
    }

    // ===== RULE-BASED CHATBOT =====
    public Map<String, String> chatbot(String question) {
        String q = question.toLowerCase().trim();
        String answer = getBotAnswer(q);
        return Map.of("answer", answer, "question", question);
    }

    private String getBotAnswer(String q) {
        if (matches(q, "eligib", "qualify", "criteria", "requirement"))
            return "Eligibility is based on:\n• **CGPA** – Each job has a minimum CGPA requirement\n• **Branch** – Only listed branches are eligible\n• **Skills** – Matching skills improve your chances\n\nGo to **Eligible Jobs** to see jobs matching your profile!";

        if (matches(q, "apply", "submit", "application"))
            return "To apply for a job:\n1. Go to **Jobs** page\n2. Click a job card to view details\n3. Click **Apply Now**\n4. Add an optional cover letter\n5. Track status under **My Applications**";

        if (matches(q, "status", "shortlist", "track", "result"))
            return "Your application statuses:\n• **Applied** – Submitted successfully\n• **Under Review** – Company is reviewing\n• **Shortlisted** – You are selected for next round 🎉\n• **Rejected** – Not selected this time\n• **Placed** – Congratulations! Job confirmed 🏆";

        if (matches(q, "resume", "cv", "upload", "pdf"))
            return "To upload your resume:\n1. Go to **My Profile**\n2. Click **Upload Resume**\n3. Choose a PDF file (max 5MB)\n4. Your resume is shared with companies on apply";

        if (matches(q, "cgpa", "gpa", "marks", "score", "grade"))
            return "CGPA is critical for placements!\n• Top companies require **7.5+** CGPA\n• Most companies accept **6.5+**\n• Update your CGPA in **My Profile**\n• Use **Eligible Jobs** to filter by your CGPA";

        if (matches(q, "job", "opening", "opportunit", "position", "vacancy"))
            return "Browse all available jobs under **Jobs** tab.\nUse **Eligible Jobs** to see only the jobs you qualify for based on your CGPA and branch.";

        if (matches(q, "profile", "update", "edit", "detail"))
            return "Update your profile:\n1. Go to **My Profile**\n2. Edit Name, CGPA, Branch, Skills, LinkedIn\n3. Upload your resume\n4. Click **Save Changes**\n\nA complete profile = better job matches!";

        if (matches(q, "approv", "pending", "reject", "register", "account", "verify"))
            return "After registering, your account needs **TPO approval**.\n• TPO reviews all student registrations\n• Login is enabled once approved\n• This usually takes 1–2 business days\n• Contact your TPO if stuck in pending";

        if (matches(q, "skill", "technolog", "language", "tool"))
            return "Add skills to your profile for better matches!\n• Use comma-separated format: Java, Python, SQL\n• Popular skills: Java, Python, SQL, React, ML, AWS\n• Companies filter candidates by required skills";

        if (matches(q, "company", "recruiter", "hr", "employer"))
            return "Companies registered on this portal:\n• Post job openings for campus recruitment\n• Review and shortlist student applications\n• Are approved by TPO before posting jobs\n\nApplied students can see company details on each job card.";

        if (matches(q, "hello", "hi", "hey", "namaste", "good morning", "good afternoon"))
            return "Hello! 👋 I'm your Campus Placement Assistant!\n\nI can help you with:\n• **Eligibility** – What jobs you qualify for\n• **Apply** – How to apply for jobs\n• **Status** – Understanding application statuses\n• **Resume** – How to upload your CV\n• **Profile** – Tips to improve your profile\n\nWhat would you like to know?";

        if (matches(q, "thank", "thanks", "great", "awesome", "helpful"))
            return "You're welcome! 😊 Best of luck with your placements!\n\n• Keep your profile updated\n• Apply to multiple relevant jobs\n• Check back for new openings\n\nYou've got this! 🚀";

        if (matches(q, "help", "what can", "support", "assist"))
            return "I can help you with:\n\n💼 **Jobs** – Browse available openings\n✅ **Eligibility** – Check if you qualify\n📋 **Apply** – Application steps\n📊 **Status** – Track your applications\n👤 **Profile** – Update your details\n📄 **Resume** – Upload your CV\n🎓 **CGPA** – How it affects eligibility\n\nJust type your question!";

        if (matches(q, "tpo", "placement officer", "admin", "coordinator"))
            return "The **TPO (Training & Placement Officer)** manages:\n• Approving/rejecting student and company registrations\n• Monitoring all job postings\n• Tracking placement statistics\n• Generating placement reports\n\nContact your TPO directly for account issues.";

        if (matches(q, "internship", "intern"))
            return "Internship opportunities are listed under **Jobs** with type = **Internship**.\nUse the type filter on the Jobs page to find internships specifically.";

        return "I'm not sure I understood that. Here's what I can help with:\n\n• Type **'jobs'** – Available positions\n• Type **'eligibility'** – Eligibility criteria\n• Type **'apply'** – Application process\n• Type **'status'** – Application statuses\n• Type **'profile'** – Profile management\n• Type **'resume'** – Resume upload\n• Type **'help'** – Full topic list\n\nOr contact your TPO for specific queries!";
    }

    private boolean matches(String q, String... keywords) {
        for (String kw : keywords) if (q.contains(kw)) return true;
        return false;
    }
}