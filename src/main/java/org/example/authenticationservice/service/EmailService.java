package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String email, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText(
                        "You requested to reset your password.\n\n" +
                        "Click the link below to reset your password:\n" +
                        resetLink +
                        "\n\n" +
                        "This link will expire in 15 minutes.");
        mailSender.send(message);
    }
}