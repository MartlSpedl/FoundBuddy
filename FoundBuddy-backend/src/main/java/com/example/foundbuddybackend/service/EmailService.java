package com.example.foundbuddybackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sendet eine Bestätigungs-Email an den neuen User.
     */
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verificationLink = baseUrl + "/api/users/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("FoundBuddy - Bestätige deine E-Mail-Adresse");
        message.setText(
            "Hallo " + username + ",\n\n" +
            "Willkommen bei FoundBuddy!\n\n" +
            "Bitte bestätige deine E-Mail-Adresse, indem du auf den folgenden Link klickst:\n\n" +
            verificationLink + "\n\n" +
            "Falls du dich nicht bei FoundBuddy registriert hast, kannst du diese E-Mail ignorieren.\n\n" +
            "Viele Grüße,\n" +
            "Dein FoundBuddy Team"
        );

        mailSender.send(message);
    }
}
