package com.waitlist.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_businesses",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "business_id")
    )
    private java.util.Set<Business> businesses = new java.util.HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.BUSINESS_OWNER;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {
    }

    public User(String username, String password, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public User(String username, String password, String email, Business business, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        if (business != null) {
            this.businesses.add(business);
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    @Deprecated
    public Business getBusiness() {
        // For backward compatibility, return first business or null
        return businesses.isEmpty() ? null : businesses.iterator().next();
    }

    @Deprecated
    public void setBusiness(Business business) {
        // For backward compatibility, replace all businesses with this one
        this.businesses.clear();
        if (business != null) {
            this.businesses.add(business);
        }
    }

    public java.util.Set<Business> getBusinesses() {
        return businesses;
    }

    public void setBusinesses(java.util.Set<Business> businesses) {
        this.businesses = businesses != null ? businesses : new java.util.HashSet<>();
    }

    public void addBusiness(Business business) {
        if (business != null) {
            this.businesses.add(business);
        }
    }

    public void removeBusiness(Business business) {
        if (business != null) {
            this.businesses.remove(business);
        }
    }

    public boolean hasBusiness(UUID businessId) {
        return businesses.stream().anyMatch(b -> b.getId().equals(businessId));
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean hasAccessToBusiness(UUID businessId) {
        return isActive && hasBusiness(businessId);
    }
}

