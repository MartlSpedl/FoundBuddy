package com.example.foundbuddybackend.model;

public class User {

    private String id;
    private String username;
    private String email;
    private String password;
    private String profileImage;
    
    // Email Verification Felder
    private boolean emailVerified = false;
    private String verificationToken;

    public User() {}

    public User(String username, String email, String password, String profileImage) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.emailVerified = false;
    }

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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
}