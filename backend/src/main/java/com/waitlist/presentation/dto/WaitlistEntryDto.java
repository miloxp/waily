package com.waitlist.presentation.dto;

import com.waitlist.domain.entity.WaitlistStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

public class WaitlistEntryDto {

    private UUID id;

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @Positive(message = "Party size must be positive")
    private Integer partySize;

    private Integer estimatedWaitTime; // in minutes

    @Positive(message = "Position must be positive")
    private Integer position;

    private WaitlistStatus status;

    private LocalDateTime notifiedAt;
    private LocalDateTime seatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for display
    private String businessName;
    private String customerName;
    private String customerPhone;

    // Constructors
    public WaitlistEntryDto() {
    }

    public WaitlistEntryDto(UUID id, UUID businessId, UUID customerId, Integer partySize,
            Integer estimatedWaitTime, Integer position, WaitlistStatus status,
            LocalDateTime notifiedAt, LocalDateTime seatedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.businessId = businessId;
        this.customerId = customerId;
        this.partySize = partySize;
        this.estimatedWaitTime = estimatedWaitTime;
        this.position = position;
        this.status = status;
        this.notifiedAt = notifiedAt;
        this.seatedAt = seatedAt;
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

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public WaitlistStatus getStatus() {
        return status;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public LocalDateTime getSeatedAt() {
        return seatedAt;
    }

    public void setSeatedAt(LocalDateTime seatedAt) {
        this.seatedAt = seatedAt;
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}

