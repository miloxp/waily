package com.waitlist.presentation.dto;

import com.waitlist.domain.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "At least one business must be selected")
    private List<UUID> businessIds;

    private Boolean isActive = true;

    // Constructors
    public CreateUserRequest() {
    }

    public CreateUserRequest(String username, String password, String email, UserRole role, List<UUID> businessIds) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.businessIds = businessIds;
        this.isActive = true;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<UUID> getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(List<UUID> businessIds) {
        this.businessIds = businessIds;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

