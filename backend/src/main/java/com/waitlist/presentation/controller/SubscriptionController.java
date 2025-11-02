package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.Subscription;
import com.waitlist.domain.entity.SubscriptionStatus;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.SubscriptionRepository;
import com.waitlist.presentation.dto.SubscriptionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get all subscriptions", description = "Retrieve all subscriptions (Platform Admin only)")
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAllWithBusiness();
        List<SubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptionDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get subscription by ID", description = "Retrieve a specific subscription by ID")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@PathVariable UUID id) {
        Optional<Subscription> subscription = subscriptionRepository.findById(id);
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            // Load business relationship
            sub.getBusiness().getName();
            return ResponseEntity.ok(convertToDto(sub));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get subscription by business ID", description = "Retrieve subscription for a specific business")
    public ResponseEntity<SubscriptionDto> getSubscriptionByBusinessId(@PathVariable UUID businessId) {
        Optional<Subscription> subscription = subscriptionRepository.findByBusinessIdWithBusiness(businessId);
        if (subscription.isPresent()) {
            return ResponseEntity.ok(convertToDto(subscription.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get subscriptions by status", description = "Retrieve subscriptions by status")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByStatus(@PathVariable SubscriptionStatus status) {
        List<Subscription> subscriptions = subscriptionRepository.findByStatus(status);
        List<SubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptionDtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional
    @Operation(summary = "Create subscription", description = "Create a new subscription for a business (Platform Admin only)")
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubscriptionDto subscriptionDto) {
        // Check if business exists
        Optional<Business> business = businessRepository.findById(subscriptionDto.getBusinessId());
        if (business.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if business already has a subscription
        Optional<Subscription> existingSubscription = subscriptionRepository.findByBusinessId(subscriptionDto.getBusinessId());
        if (existingSubscription.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Subscription subscription = convertToEntity(subscriptionDto, business.get());
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedSubscription));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional
    @Operation(summary = "Update subscription", description = "Update an existing subscription")
    public ResponseEntity<SubscriptionDto> updateSubscription(@PathVariable UUID id,
                                                              @Valid @RequestBody SubscriptionDto subscriptionDto) {
        Optional<Subscription> existingSubscription = subscriptionRepository.findById(id);
        if (existingSubscription.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Subscription subscription = existingSubscription.get();
        // Load business relationship
        Business business = subscription.getBusiness();
        business.getName();

        // Update subscription fields
        subscription.setPlan(subscriptionDto.getPlan());
        subscription.setStatus(subscriptionDto.getStatus());
        subscription.setStartDate(subscriptionDto.getStartDate());
        subscription.setEndDate(subscriptionDto.getEndDate());
        subscription.setBillingCycleDays(subscriptionDto.getBillingCycleDays());
        subscription.setMonthlyPrice(subscriptionDto.getMonthlyPrice());
        subscription.setAutoRenew(subscriptionDto.getAutoRenew());
        subscription.setTrialEndDate(subscriptionDto.getTrialEndDate());
        subscription.setNotes(subscriptionDto.getNotes());

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return ResponseEntity.ok(convertToDto(savedSubscription));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional
    @Operation(summary = "Activate subscription", description = "Activate a subscription")
    public ResponseEntity<SubscriptionDto> activateSubscription(@PathVariable UUID id) {
        Optional<Subscription> subscription = subscriptionRepository.findById(id);
        if (subscription.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        subscription.get().activate();
        Subscription savedSubscription = subscriptionRepository.save(subscription.get());
        return ResponseEntity.ok(convertToDto(savedSubscription));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional
    @Operation(summary = "Cancel subscription", description = "Cancel a subscription")
    public ResponseEntity<SubscriptionDto> cancelSubscription(@PathVariable UUID id) {
        Optional<Subscription> subscription = subscriptionRepository.findById(id);
        if (subscription.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        subscription.get().cancel();
        Subscription savedSubscription = subscriptionRepository.save(subscription.get());
        return ResponseEntity.ok(convertToDto(savedSubscription));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional
    @Operation(summary = "Suspend subscription", description = "Suspend a subscription")
    public ResponseEntity<SubscriptionDto> suspendSubscription(@PathVariable UUID id) {
        Optional<Subscription> subscription = subscriptionRepository.findById(id);
        if (subscription.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        subscription.get().suspend();
        Subscription savedSubscription = subscriptionRepository.save(subscription.get());
        return ResponseEntity.ok(convertToDto(savedSubscription));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Delete subscription", description = "Delete a subscription")
    public ResponseEntity<Void> deleteSubscription(@PathVariable UUID id) {
        if (subscriptionRepository.existsById(id)) {
            subscriptionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private SubscriptionDto convertToDto(Subscription subscription) {
        Business business = subscription.getBusiness();
        
        SubscriptionDto dto = new SubscriptionDto(
                subscription.getId(),
                business.getId(),
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getBillingCycleDays(),
                subscription.getMonthlyPrice(),
                subscription.getAutoRenew(),
                subscription.getTrialEndDate(),
                subscription.getNotes(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt());

        dto.setBusinessName(business.getName());
        return dto;
    }

    private Subscription convertToEntity(SubscriptionDto subscriptionDto, Business business) {
        Subscription subscription = new Subscription(
                business,
                subscriptionDto.getPlan(),
                subscriptionDto.getStatus(),
                subscriptionDto.getStartDate(),
                subscriptionDto.getEndDate(),
                subscriptionDto.getBillingCycleDays(),
                subscriptionDto.getMonthlyPrice());

        subscription.setAutoRenew(subscriptionDto.getAutoRenew());
        subscription.setTrialEndDate(subscriptionDto.getTrialEndDate());
        subscription.setNotes(subscriptionDto.getNotes());

        return subscription;
    }
}

