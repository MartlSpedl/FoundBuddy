package com.example.foundbuddybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:FoundBuddy}")
    private String senderName;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY ist nicht gesetzt.");
        }
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException("BREVO_SENDER_EMAIL ist nicht gesetzt.");
        }

        String verifyUrl = appBaseUrl + "/api/users/verify?token=" + verificationToken;

        String subject = "FoundBuddy: Bitte E-Mail bestaetigen";
        String html = """
                <div style="font-family: Arial, sans-serif; line-height: 1.5;">
                  <h2>Willkommen bei FoundBuddy%s</h2>
                  <p>Bitte bestaetige deine E-Mail-Adresse, indem du auf den Link klickst:</p>
                  <p><a href="%s">E-Mail bestaetigen</a></p>
                  <p style="color: #666;">Wenn du dich nicht registriert hast, ignoriere diese Mail.</p>
                </div>
                """.formatted(
                (username != null && !username.isBlank()) ? (", " + username) : "",
                verifyUrl
        );

        // Minimales JSON (ohne externe JSON-Libs)
        String json = "{"
                + "\"sender\":{\"name\":\"" + escapeJson(senderName) + "\",\"email\":\"" + escapeJson(senderEmail) + "\"},"
                + "\"to\":[{\"email\":\"" + escapeJson(toEmail) + "\"}],"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"htmlContent\":\"" + escapeJson(html) + "\""
                + "}";

        postToBrevo(json);
    }

    private void postToBrevo(String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.brevo.com/v3/smtp/email");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(12_000);
            conn.setReadTimeout(12_000);
            conn.setDoOutput(true);

            conn.setRequestProperty("api-key", brevoApiKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new RuntimeException("Brevo API Fehler. HTTP " + code);
            }
        } catch (Exception e) {
            throw new RuntimeException("Email Versand fehlgeschlagen: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}
