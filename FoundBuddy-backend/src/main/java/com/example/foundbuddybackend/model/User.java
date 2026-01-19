package com.example.foundbuddybackend.model;

public class User {

    private String id;
    private String username;
    private String email;
    private String password;

    private boolean emailVerified;
    private String verificationToken;

    // Passwort Reset
    private String passwordResetToken;
    private Long passwordResetRequestedAt;

    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public Long getPasswordResetRequestedAt() {
        return passwordResetRequestedAt;
    }

    public void setPasswordResetRequestedAt(Long passwordResetRequestedAt) {
        this.passwordResetRequestedAt = passwordResetRequestedAt;
    }
}
