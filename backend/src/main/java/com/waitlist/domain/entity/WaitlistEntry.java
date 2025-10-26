package com.waitlist.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "waitlist_entries")
@EntityListeners(AuditingEntityListener.class)
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime; // in minutes

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "seated_at")
    private LocalDateTime seatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public WaitlistEntry() {
    }

    public WaitlistEntry(Business business, Customer customer, Integer partySize, Integer position) {
        this.business = business;
        this.customer = customer;
        this.partySize = partySize;
        this.position = position;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    // Business methods
    public void notifyCustomer() {
        if (this.status == WaitlistStatus.WAITING) {
            this.status = WaitlistStatus.NOTIFIED;
            this.notifiedAt = LocalDateTime.now();
        }
    }

    public void seatCustomer() {
        if (this.status == WaitlistStatus.NOTIFIED) {
            this.status = WaitlistStatus.SEATED;
            this.seatedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        if (this.status != WaitlistStatus.SEATED) {
            this.status = WaitlistStatus.CANCELLED;
        }
    }

    public boolean isActive() {
        return this.status == WaitlistStatus.WAITING || this.status == WaitlistStatus.NOTIFIED;
    }

    public boolean canBeNotified() {
        return this.status == WaitlistStatus.WAITING;
    }

    public boolean canBeSeated() {
        return this.status == WaitlistStatus.NOTIFIED;
    }

    public void updatePosition(Integer newPosition) {
        this.position = newPosition;
    }

    public void calculateEstimatedWaitTime(Integer averageServiceTime, Integer currentPosition) {
        if (averageServiceTime != null && currentPosition != null) {
            this.estimatedWaitTime = currentPosition * averageServiceTime;
        }
    }
}

