package com.jobportal.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

//    @Value("${company.name}")
//    private String companyName;
//
//    @Value("${company.website}")
//    private String companyWebsite;
    private String loadEmailTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("email-template.html");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error loading email template", e);
        }
    }

    /**
     * Sends an email to the applicant about their application status.
     */
    public void sendStatusEmail(String toEmail, String name, String status) {
        try {
            // Load and format email template
            String emailContent = loadEmailTemplate()
                    .replace("{NAME}", name)
                    .replace("{STATUS}", status)
                    .replace("{STATUS_CLASS}", status.equalsIgnoreCase("Accepted") ? "" : "rejected");

            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Application Status Update");
            helper.setText(emailContent, true); // Enable HTML formatting

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email to: " + toEmail);
        }
    }
}

