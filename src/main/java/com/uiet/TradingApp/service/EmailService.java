package com.uiet.TradingApp.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Service
public class EmailService {
  @Autowired
  JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String from;

  public void sendVerificationEmail(String email, String verificationToken) {
    String subject = "Email Verification";
    String path = "/public/verification";
    String message = "Click the button below to verify your email";
    sendEmail(email, subject, message, path, verificationToken);
  }

  public void sendEmail(String email, String subject, String message,
      String path, String verificationToken) {
    try {
      String actionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
          .path(path)
          .queryParam("token", verificationToken)
          .toUriString();
      String content = String.format(
          """
              <div style="font-family: Arial, sans-serif; max-width: 600px; background-color: #f2f2f2; padding: 20px; border-radius: 5px;">
                <h2 style="color: #333;">%s</h2>
                <p style="color: #666;">%s</p>
                <a href="%s"
                   style="display: inline-block; background-color: #007BFF; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                   Verify Email
                </a>
              </div>
              """,
          subject, message, actionUrl);
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
      mimeMessageHelper.setTo(email);
      mimeMessageHelper.setSubject(subject);
      mimeMessageHelper.setText(content, true);
      mailSender.send(mimeMessage);
      log.info("INFO: Email sent to {} with subject {}", email, subject);
    } catch (Exception e) {
      log.error("ERROR: Failed to send email {}", email, e);
    }
  }
}
