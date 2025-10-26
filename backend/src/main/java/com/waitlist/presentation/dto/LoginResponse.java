package com.waitlist.presentation.dto;

public class LoginResponse {

    private String token;
    private String type;
    private String username;
    private String roles;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(String token, String type, String username, String roles) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.roles = roles;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}

