package com.waitlist.presentation.dto;

import com.waitlist.domain.entity.SubscriptionPlan;
import com.waitlist.domain.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SubscriptionDto {

    private UUID id;

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    private String businessName; // Display field

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;

    @NotNull(message = "Subscription status is required")
    private SubscriptionStatus status;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Billing cycle days is required")
    @Positive(message = "Billing cycle days must be positive")
    private Integer billingCycleDays;

    @NotNull(message = "Monthly price is required")
    @Positive(message = "Monthly price must be positive")
    private Double monthlyPrice;

    @NotNull(message = "Auto renew is required")
    private Boolean autoRenew;

    private LocalDate trialEndDate;

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public SubscriptionDto() {
    }

    public SubscriptionDto(UUID id, UUID businessId, SubscriptionPlan plan, SubscriptionStatus status,
                          LocalDate startDate, LocalDate endDate, Integer billingCycleDays,
                          Double monthlyPrice, Boolean autoRenew, LocalDate trialEndDate,
                          String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.businessId = businessId;
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billingCycleDays = billingCycleDays;
        this.monthlyPrice = monthlyPrice;
        this.autoRenew = autoRenew;
        this.trialEndDate = trialEndDate;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getBillingCycleDays() {
        return billingCycleDays;
    }

    public void setBillingCycleDays(Integer billingCycleDays) {
        this.billingCycleDays = billingCycleDays;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Boolean getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public LocalDate getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(LocalDate trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}

