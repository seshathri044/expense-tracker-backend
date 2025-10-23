package com.example.ExpenseTracker.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            log.info("Attempting to send welcome email to: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setReplyTo(fromEmail);
            message.setSubject("Welcome to Expense Tracker!");
            message.setText(buildWelcomeEmailText(name));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email: " + e.getMessage());
        }
    }

    public void sendResetOtpEmail(String toEmail, String otp) {
        try {
            log.info("Attempting to send reset OTP email to: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setReplyTo(fromEmail);
            message.setSubject("Password Reset OTP - Expense Tracker");
            message.setText(buildResetOtpEmailText(otp));

            mailSender.send(message);
            log.info("Reset OTP email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send reset OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send reset OTP email: " + e.getMessage());
        }
    }

    private String buildWelcomeEmailText(String name) {
        return String.format(
                "Hello %s,\n\n" +
                        "Welcome to Expense Tracker – your smart expense manager powered by Vyugam Solutions!\n\n" +
                        "We’re excited to have you onboard. From now on, you can easily track your expenses, manage your budget, and take full control of your finances with confidence.\n\n" +
                        "If you have any questions or need assistance, our team is here to help you anytime.\n\n" +
                        "Best regards,\n" +
                        "Team Vyugam Solutions\n\n" +
                        "------------------------------------------------------\n" +
                        "VYUGAM SOLUTIONS\n" +
                        "Vyugam is one of the best software solutions in Hosur, providing cutting-edge technology for modern businesses.\n\n" +
                        "📍 Address: 2nd Floor, 647/1A, KVM Towers, Bangalore Road, Hosur, Tamil Nadu – 635109\n" +
                        "📞 Phone: +91-9159585656 | +91-8489403864\n" +
                        "📧 Email: info@vyugamsolutions.com\n\n" +
                        "---\n" +
                        "This is an automated email. Please do not reply to this email.",
                name
        );
    }

    private String buildResetOtpEmailText(String otp) {
        return String.format(
                "Hello,\n\n" +
                        "You have requested to reset your password for Expense Tracker.\n\n" +
                        "Your OTP for password reset is: %s\n\n" +
                        "This OTP will expire in 10 minutes for security reasons.\n\n" +
                        "If you did not request this password reset, please ignore this email and your password will remain unchanged.\n\n" +
                        "Best regards,\n" +
                        "Vyugam Solutions Team\n\n" +
                        "---\n" +
                        "This is an automated email. Please do not reply to this email.",
                otp
        );
    }
    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Account verification OTP");
        message.setText(
                "Hello,\n\n" +
                        "Thank you for signing up with Expense Tracker!\n\n" +
                        "Your OTP for account verification is: " + otp + "\n\n" +
                        "This OTP will expire in 10 minutes for security reasons.\n\n" +
                        "Please enter this code to complete your account verification and get started with managing your expenses.\n\n" +
                        "If you did not create an account with us, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Vyugam Solutions Team\n\n" +
                        "---\n" +
                        "This is an automated email. Please do not reply to this email."
        );
        mailSender.send(message);
    }
}