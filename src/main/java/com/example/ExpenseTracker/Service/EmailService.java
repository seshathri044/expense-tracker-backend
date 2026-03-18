package com.example.ExpenseTracker.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${SPRING_MAIL_FROM}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            log.info("Attempting to send welcome email to: {}", toEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Expense Tracker");
            helper.setTo(toEmail);
            helper.setReplyTo(fromEmail);
            helper.setSubject("🎉 Welcome to Expense Tracker, " + name + "!");
            helper.setText(buildWelcomeEmailHtml(name), true);

            mailSender.send(mimeMessage);
            log.info("Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email: " + e.getMessage());
        }
    }

    public void sendResetOtpEmail(String toEmail, String otp) {
        try {
            log.info("Attempting to send reset OTP email to: {}", toEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Expense Tracker");
            helper.setTo(toEmail);
            helper.setReplyTo(fromEmail);
            helper.setSubject("🔐 Password Reset OTP - Expense Tracker");
            helper.setText(buildResetOtpEmailHtml(otp), true);

            mailSender.send(mimeMessage);
            log.info("Reset OTP email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send reset OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send reset OTP email: " + e.getMessage());
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Expense Tracker");
            helper.setTo(toEmail);
            helper.setSubject("✅ Verify Your Account - Expense Tracker");
            helper.setText(buildOtpEmailHtml(otp), true);

            mailSender.send(mimeMessage);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // =====================================================================
    // HTML TEMPLATES
    // =====================================================================

    private String buildWelcomeEmailHtml(String name) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Welcome to Expense Tracker</title></head>" +
            "<body style='margin:0;padding:0;background-color:#f0f4f8;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f0f4f8;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>" +

            // Header
            "<tr><td style='background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);border-radius:16px 16px 0 0;padding:40px 40px 30px;text-align:center;'>" +
            "<div style='font-size:48px;margin-bottom:10px;'>💰</div>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:700;letter-spacing:-0.5px;'>Expense Tracker</h1>" +
            "<p style='color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:15px;'>Smart Finance Management</p>" +
            "</td></tr>" +

            // Body
            "<tr><td style='background:#ffffff;padding:40px;'>" +
            "<h2 style='color:#1a202c;font-size:24px;margin:0 0 16px;'>Welcome aboard, " + name + "! 🎉</h2>" +
            "<p style='color:#4a5568;font-size:16px;line-height:1.6;margin:0 0 24px;'>We're thrilled to have you join Expense Tracker. You're now one step closer to taking full control of your finances.</p>" +

            // Feature cards
            "<table width='100%' cellpadding='0' cellspacing='0' style='margin:24px 0;'>" +
            "<tr>" +
            "<td width='33%' style='padding:4px;'>" +
            "<div style='background:#f7f3ff;border-radius:12px;padding:20px 16px;text-align:center;'>" +
            "<div style='font-size:28px;margin-bottom:8px;'>📊</div>" +
            "<p style='color:#553c9a;font-size:13px;font-weight:600;margin:0;'>Track Expenses</p>" +
            "</div></td>" +
            "<td width='33%' style='padding:4px;'>" +
            "<div style='background:#f0fff4;border-radius:12px;padding:20px 16px;text-align:center;'>" +
            "<div style='font-size:28px;margin-bottom:8px;'>🎯</div>" +
            "<p style='color:#276749;font-size:13px;font-weight:600;margin:0;'>Set Budgets</p>" +
            "</div></td>" +
            "<td width='33%' style='padding:4px;'>" +
            "<div style='background:#ebf8ff;border-radius:12px;padding:20px 16px;text-align:center;'>" +
            "<div style='font-size:28px;margin-bottom:8px;'>📈</div>" +
            "<p style='color:#2b6cb0;font-size:13px;font-weight:600;margin:0;'>View Reports</p>" +
            "</div></td>" +
            "</tr></table>" +

            "<p style='color:#4a5568;font-size:15px;line-height:1.6;margin:24px 0;'>Your account is almost ready! Please verify your email address to get started. Check your inbox for the OTP verification email.</p>" +

            "<div style='background:#f7f3ff;border-left:4px solid #667eea;border-radius:0 8px 8px 0;padding:16px 20px;margin:24px 0;'>" +
            "<p style='color:#553c9a;font-size:14px;margin:0;'>💡 <strong>Next Step:</strong> Enter the OTP sent to your email to verify your account and start tracking your expenses.</p>" +
            "</div>" +
            "</td></tr>" +

            // Footer
            "<tr><td style='background:#f7fafc;border-radius:0 0 16px 16px;padding:24px 40px;text-align:center;border-top:1px solid #e2e8f0;'>" +
            "<p style='color:#718096;font-size:13px;margin:0 0 8px;'>Need help? Contact us anytime.</p>" +
            "<p style='color:#a0aec0;font-size:12px;margin:0;'>© 2026 Expense Tracker by Seshathri · This is an automated email, please do not reply.</p>" +
            "</td></tr>" +

            "</table></td></tr></table></body></html>";
    }

    private String buildOtpEmailHtml(String otp) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Verify Your Account</title></head>" +
            "<body style='margin:0;padding:0;background-color:#f0f4f8;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f0f4f8;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>" +

            // Header
            "<tr><td style='background:linear-gradient(135deg,#11998e 0%,#38ef7d 100%);border-radius:16px 16px 0 0;padding:40px 40px 30px;text-align:center;'>" +
            "<div style='font-size:48px;margin-bottom:10px;'>✅</div>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:700;'>Verify Your Account</h1>" +
            "<p style='color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:15px;'>Expense Tracker</p>" +
            "</td></tr>" +

            // Body
            "<tr><td style='background:#ffffff;padding:40px;text-align:center;'>" +
            "<p style='color:#4a5568;font-size:16px;line-height:1.6;margin:0 0 32px;'>Use the OTP below to verify your account. This code expires in <strong>10 minutes</strong>.</p>" +

            // OTP Box
            "<div style='background:linear-gradient(135deg,#f7f3ff,#ebf8ff);border:2px dashed #667eea;border-radius:16px;padding:32px;margin:24px 0;display:inline-block;width:100%;box-sizing:border-box;'>" +
            "<p style='color:#718096;font-size:14px;margin:0 0 12px;text-transform:uppercase;letter-spacing:2px;font-weight:600;'>Your Verification Code</p>" +
            "<div style='font-size:48px;font-weight:800;letter-spacing:12px;color:#667eea;font-family:monospace;'>" + otp + "</div>" +
            "<p style='color:#a0aec0;font-size:13px;margin:12px 0 0;'>⏱ Valid for 10 minutes only</p>" +
            "</div>" +

            "<div style='background:#fff5f5;border-left:4px solid #fc8181;border-radius:0 8px 8px 0;padding:16px 20px;margin:24px 0;text-align:left;'>" +
            "<p style='color:#c53030;font-size:14px;margin:0;'>⚠️ <strong>Security Notice:</strong> Never share this OTP with anyone. Expense Tracker will never ask for your OTP.</p>" +
            "</div>" +
            "<p style='color:#a0aec0;font-size:14px;margin:24px 0 0;'>If you didn't create an account, you can safely ignore this email.</p>" +
            "</td></tr>" +

            // Footer
            "<tr><td style='background:#f7fafc;border-radius:0 0 16px 16px;padding:24px 40px;text-align:center;border-top:1px solid #e2e8f0;'>" +
            "<p style='color:#a0aec0;font-size:12px;margin:0;'>© 2026 Expense Tracker by Seshathri · This is an automated email, please do not reply.</p>" +
            "</td></tr>" +

            "</table></td></tr></table></body></html>";
    }

    private String buildResetOtpEmailHtml(String otp) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Password Reset OTP</title></head>" +
            "<body style='margin:0;padding:0;background-color:#f0f4f8;font-family:Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f0f4f8;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>" +

            // Header
            "<tr><td style='background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);border-radius:16px 16px 0 0;padding:40px 40px 30px;text-align:center;'>" +
            "<div style='font-size:48px;margin-bottom:10px;'>🔐</div>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:700;'>Password Reset</h1>" +
            "<p style='color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:15px;'>Expense Tracker</p>" +
            "</td></tr>" +

            // Body
            "<tr><td style='background:#ffffff;padding:40px;text-align:center;'>" +
            "<p style='color:#4a5568;font-size:16px;line-height:1.6;margin:0 0 32px;'>We received a request to reset your password. Use the OTP below. This code expires in <strong>10 minutes</strong>.</p>" +

            // OTP Box
            "<div style='background:linear-gradient(135deg,#fff5f7,#fff0f6);border:2px dashed #f5576c;border-radius:16px;padding:32px;margin:24px 0;display:inline-block;width:100%;box-sizing:border-box;'>" +
            "<p style='color:#718096;font-size:14px;margin:0 0 12px;text-transform:uppercase;letter-spacing:2px;font-weight:600;'>Password Reset Code</p>" +
            "<div style='font-size:48px;font-weight:800;letter-spacing:12px;color:#f5576c;font-family:monospace;'>" + otp + "</div>" +
            "<p style='color:#a0aec0;font-size:13px;margin:12px 0 0;'>⏱ Valid for 10 minutes only</p>" +
            "</div>" +

            "<div style='background:#fff5f5;border-left:4px solid #fc8181;border-radius:0 8px 8px 0;padding:16px 20px;margin:24px 0;text-align:left;'>" +
            "<p style='color:#c53030;font-size:14px;margin:0;'>⚠️ <strong>Didn't request this?</strong> Ignore this email — your password will remain unchanged. If you're concerned, contact support immediately.</p>" +
            "</div>" +
            "</td></tr>" +

            // Footer
            "<tr><td style='background:#f7fafc;border-radius:0 0 16px 16px;padding:24px 40px;text-align:center;border-top:1px solid #e2e8f0;'>" +
            "<p style='color:#a0aec0;font-size:12px;margin:0;'>© 2026 Expense Tracker by Seshathri · This is an automated email, please do not reply.</p>" +
            "</td></tr>" +

            "</table></td></tr></table></body></html>";
    }
}