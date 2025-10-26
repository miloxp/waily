package com.waitlist.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class AddCustomerToWaitlistRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be positive")
    private Integer partySize;

    // Constructors
    public AddCustomerToWaitlistRequest() {
    }

    public AddCustomerToWaitlistRequest(UUID customerId, Integer partySize) {
        this.customerId = customerId;
        this.partySize = partySize;
    }

    // Getters and Setters
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
}

