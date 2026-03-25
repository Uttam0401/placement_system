package com.uttam.placement_system.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Campus Placement System}")
    private String appName;

    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Email send failed to " + to + ": " + e.getMessage());
        }
    }

    // ===== OTP EMAIL =====
    public void sendOtp(String to, String name, String otp, String purpose) {
        String subject = appName + " — Your OTP for " + purpose;
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:#4f46e5;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">🎓 %s</h2>
              </div>
              <div style="padding:32px">
                <p style="font-size:16px">Hi <strong>%s</strong>,</p>
                <p>Your OTP for <strong>%s</strong> is:</p>
                <div style="text-align:center;margin:28px 0">
                  <span style="font-size:40px;font-weight:bold;letter-spacing:12px;color:#4f46e5;background:#f0f0ff;padding:16px 32px;border-radius:8px">%s</span>
                </div>
                <p style="color:#888;font-size:13px">⏱ Valid for <strong>10 minutes</strong>. Do not share it.</p>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(appName, name, purpose, otp, appName);
        sendHtml(to, subject, html);
    }

    // ===== WELCOME EMAIL =====
    public void sendWelcome(String to, String name, String role) {
        String subject = "Welcome to " + appName + "! 🎉";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:#4f46e5;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">🎓 %s</h2>
              </div>
              <div style="padding:32px">
                <p style="font-size:18px">Hi <strong>%s</strong>, welcome aboard! 🎉</p>
                <p>Your <strong>%s</strong> account has been created. It is under review by the TPO and you'll be notified once approved.</p>
                <div style="background:#f0f0ff;border-left:4px solid #4f46e5;padding:14px 18px;border-radius:4px;margin-top:20px">
                  <p style="margin:0;color:#4f46e5">💡 Keep your profile complete for better job matches!</p>
                </div>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(appName, name, role, appName);
        sendHtml(to, subject, html);
    }

    // ===== ACCOUNT APPROVED =====
    public void sendAccountApproved(String to, String name, String role) {
        String subject = "✅ Your " + appName + " account is approved!";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:#16a34a;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">✅ Account Approved</h2>
              </div>
              <div style="padding:32px">
                <p>Hi <strong>%s</strong>,</p>
                <p>Your <strong>%s</strong> account on <strong>%s</strong> has been <span style="color:#16a34a;font-weight:bold">approved</span>. You can now log in!</p>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(name, role, appName, appName);
        sendHtml(to, subject, html);
    }

    // ===== ACCOUNT REJECTED =====
    public void sendAccountRejected(String to, String name, String role) {
        String subject = "❌ " + appName + " — Account Registration Update";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:#dc2626;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">Account Registration Update</h2>
              </div>
              <div style="padding:32px">
                <p>Hi <strong>%s</strong>,</p>
                <p>Your <strong>%s</strong> account on <strong>%s</strong> has been <span style="color:#dc2626;font-weight:bold">rejected</span> by the TPO. Please contact TPO for clarification.</p>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(name, role, appName, appName);
        sendHtml(to, subject, html);
    }

    // ===== APPLICATION STATUS CHANGE =====
    public void sendApplicationStatus(String to, String studentName,
                                      String jobRole, String companyName, String status) {
        String emoji = switch (status.toUpperCase()) {
            case "SHORTLISTED"  -> "🌟";
            case "PLACED"       -> "🏆";
            case "REJECTED"     -> "😔";
            case "UNDER_REVIEW" -> "🔍";
            default             -> "📋";
        };
        String color = switch (status.toUpperCase()) {
            case "SHORTLISTED"  -> "#f59e0b";
            case "PLACED"       -> "#16a34a";
            case "REJECTED"     -> "#dc2626";
            default             -> "#4f46e5";
        };
        String message = switch (status.toUpperCase()) {
            case "SHORTLISTED"  -> "Congratulations! You have been <strong>shortlisted</strong> for the next round.";
            case "PLACED"       -> "🎉 You have been <strong>placed</strong> at " + companyName + "! Congratulations!";
            case "REJECTED"     -> "Thank you for your interest. Unfortunately you were not selected this time. Keep applying!";
            case "UNDER_REVIEW" -> "Your application is currently <strong>under review</strong>.";
            default             -> "Status updated to <strong>" + status + "</strong>.";
        };
        String subject = emoji + " Application Update — " + jobRole + " at " + companyName;
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:%s;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">%s Application Update</h2>
              </div>
              <div style="padding:32px">
                <p>Hi <strong>%s</strong>,</p>
                <p>%s</p>
                <div style="background:#f9f9f9;border-radius:8px;padding:16px;margin:20px 0">
                  <p style="margin:4px 0"><strong>🏢 Company:</strong> %s</p>
                  <p style="margin:4px 0"><strong>💼 Role:</strong> %s</p>
                  <p style="margin:4px 0"><strong>📊 Status:</strong> <span style="color:%s;font-weight:bold">%s</span></p>
                </div>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(color, emoji, studentName, message, companyName, jobRole, color, status, appName);
        sendHtml(to, subject, html);
    }

    // ===== NEW JOB ALERT =====
    public void sendNewJobAlert(String to, String studentName,
                                String jobRole, String companyName,
                                String ctc, String deadline) {
        String subject = "🆕 New Job: " + jobRole + " at " + companyName;
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:#4f46e5;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">🆕 New Job Opportunity</h2>
              </div>
              <div style="padding:32px">
                <p>Hi <strong>%s</strong>,</p>
                <p>A new job matching your profile has been posted!</p>
                <div style="background:#f0f0ff;border-radius:8px;padding:16px;margin:20px 0">
                  <p style="margin:4px 0"><strong>🏢 Company:</strong> %s</p>
                  <p style="margin:4px 0"><strong>💼 Role:</strong> %s</p>
                  <p style="margin:4px 0"><strong>💰 CTC:</strong> %s</p>
                  <p style="margin:4px 0"><strong>📅 Deadline:</strong> %s</p>
                </div>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(studentName, companyName, jobRole,
                ctc != null ? ctc : "Not disclosed",
                deadline != null ? deadline : "Open", appName);
        sendHtml(to, subject, html);
    }

    // ===== PASSWORD RESET OTP EMAIL =====
    public void sendPasswordResetOtp(String to, String name, String otp) {
        String subject = appName + " — Password Reset OTP";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:10px;overflow:hidden">
              <div style="background:#0d1b2a;padding:24px;text-align:center">
                <h2 style="color:#fff;margin:0">🔑 Password Reset</h2>
              </div>
              <div style="padding:32px">
                <p style="font-size:16px">Hi <strong>%s</strong>,</p>
                <p>You requested a password reset. Use the OTP below to proceed:</p>
                <div style="text-align:center;margin:28px 0">
                  <span style="font-size:40px;font-weight:bold;letter-spacing:12px;color:#0d1b2a;background:#f1f5f9;padding:16px 32px;border-radius:8px">%s</span>
                </div>
                <p style="color:#666;font-size:14px">This OTP is valid for <strong>10 minutes</strong>.</p>
                <p style="color:#666;font-size:14px">If you did not request this, please ignore this email. Your password will remain unchanged.</p>
              </div>
              <div style="background:#f9f9f9;padding:16px;text-align:center;color:#aaa;font-size:12px">&copy; %s</div>
            </div>
            """.formatted(name, otp, appName);
        sendHtml(to, subject, html);
    }
}
