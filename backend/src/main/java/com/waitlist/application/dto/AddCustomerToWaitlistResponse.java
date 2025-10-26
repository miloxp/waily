package com.waitlist.application.dto;

import com.waitlist.domain.entity.WaitlistStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AddCustomerToWaitlistResponse {

    private UUID waitlistEntryId;
    private UUID businessId;
    private UUID customerId;
    private String businessName;
    private String customerName;
    private String customerPhone;
    private Integer partySize;
    private Integer position;
    private Integer estimatedWaitTime;
    private WaitlistStatus status;
    private LocalDateTime createdAt;
    private boolean smsNotificationSent;

    // Constructors
    public AddCustomerToWaitlistResponse() {
    }

    public AddCustomerToWaitlistResponse(UUID waitlistEntryId, UUID businessId, UUID customerId,
            String businessName, String customerName, String customerPhone,
            Integer partySize, Integer position, Integer estimatedWaitTime,
            WaitlistStatus status, LocalDateTime createdAt, boolean smsNotificationSent) {
        this.waitlistEntryId = waitlistEntryId;
        this.businessId = businessId;
        this.customerId = customerId;
        this.businessName = businessName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.partySize = partySize;
        this.position = position;
        this.estimatedWaitTime = estimatedWaitTime;
        this.status = status;
        this.createdAt = createdAt;
        this.smsNotificationSent = smsNotificationSent;
    }

    // Getters and Setters
    public UUID getWaitlistEntryId() {
        return waitlistEntryId;
    }

    public void setWaitlistEntryId(UUID waitlistEntryId) {
        this.waitlistEntryId = waitlistEntryId;
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

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }

    public WaitlistStatus getStatus() {
        return status;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSmsNotificationSent() {
        return smsNotificationSent;
    }

    public void setSmsNotificationSent(boolean smsNotificationSent) {
        this.smsNotificationSent = smsNotificationSent;
    }
}

