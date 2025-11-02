package com.waitlist.domain.entity;

public enum SubscriptionStatus {
    ACTIVE,         // Subscription is active and paid
    TRIAL,          // Trial period
    EXPIRED,        // Subscription has expired
    CANCELLED,      // Subscription was cancelled
    SUSPENDED       // Subscription is suspended (payment issue)
}

