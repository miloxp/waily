package com.waitlist.presentation.controller;

import com.waitlist.application.dto.AddCustomerToWaitlistRequest;
import com.waitlist.application.dto.AddCustomerToWaitlistResponse;
import com.waitlist.application.usecase.AddCustomerToWaitlistUseCase;
import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.Customer;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.WaitlistEntry;
import com.waitlist.domain.entity.WaitlistStatus;
import com.waitlist.domain.service.SmsService;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.CustomerRepository;
import com.waitlist.infrastructure.repository.WaitlistEntryRepository;
import com.waitlist.infrastructure.security.CustomUserDetailsService;
import com.waitlist.presentation.dto.WaitlistEntryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/waitlist")
@Tag(name = "Waitlist", description = "Waitlist management endpoints")
public class WaitlistController {

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private AddCustomerToWaitlistUseCase addCustomerToWaitlistUseCase;

    @GetMapping
    @Operation(summary = "List all waitlist entries", description = "List all waitlist entries for the authenticated business")
    public ResponseEntity<List<WaitlistEntryDto>> listWaitlistEntries(Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();

            UUID businessId = userPrincipal.getBusinessId();
            List<WaitlistEntry> entries = waitlistEntryRepository.findActiveWaitlistEntries(businessId);
            List<WaitlistEntryDto> entryDtos = entries.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(entryDtos);
        } catch (Exception e) {
            e.printStackTrace(); // Log the actual error
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "Get waitlist entries for a business", description = "Get all active waitlist entries for a specific business")
    public ResponseEntity<List<WaitlistEntryDto>> getWaitlistByBusiness(
            @PathVariable UUID businessId,
            Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();

            // Check if user is PLATFORM_ADMIN
            boolean isPlatformAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

            // Allow PLATFORM_ADMIN to access any business
            if (isPlatformAdmin) {
                // Verify business exists
                Optional<Business> business = businessRepository.findById(businessId);
                if (business.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }

                List<WaitlistEntry> entries = waitlistEntryRepository.findActiveWaitlistEntries(businessId);
                List<WaitlistEntryDto> entryDtos = entries.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(entryDtos);
            }

            // For business users, check if they belong to the requested business
            User user = userPrincipal.getUser();

            // Check if user has access to this business
            if (!user.hasBusiness(businessId)) {
                System.err.println("Access denied - User " + userPrincipal.getUsername()
                        + " does not have access to business: " + businessId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verify business exists and is active
            Optional<Business> business = businessRepository.findById(businessId);
            if (business.isEmpty() || !business.get().getIsActive()) {
                return ResponseEntity.notFound().build();
            }

            List<WaitlistEntry> entries = waitlistEntryRepository.findActiveWaitlistEntries(businessId);
            List<WaitlistEntryDto> entryDtos = entries.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(entryDtos);
        } catch (Exception e) {
            System.err.println("Error in getWaitlistByBusiness: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/business/{businessId}/stats")
    @Operation(summary = "Get waitlist statistics", description = "Get waitlist statistics for a business")
    public ResponseEntity<Object> getWaitlistStats(@PathVariable UUID businessId) {
        long waitingCount = waitlistEntryRepository.countWaitingEntries(businessId);
        long activeCount = waitlistEntryRepository.countActiveEntries(businessId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("waitingCount", waitingCount);
        stats.put("activeCount", activeCount);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get waitlist entry by ID", description = "Retrieve a specific waitlist entry by ID")
    public ResponseEntity<WaitlistEntryDto> getWaitlistEntryById(@PathVariable UUID id) {
        Optional<WaitlistEntry> entry = waitlistEntryRepository.findByIdWithBusinessAndCustomer(id);
        if (entry.isPresent()) {
            return ResponseEntity.ok(convertToDto(entry.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Add customer to waitlist", description = "Add a customer to the waitlist")
    public ResponseEntity<AddCustomerToWaitlistResponse> addToWaitlist(
            @Valid @RequestBody AddCustomerToWaitlistRequest request,
            Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();

            UUID businessId = userPrincipal.getBusinessId();

            // Execute the use case
            AddCustomerToWaitlistResponse response = addCustomerToWaitlistUseCase.execute(request, businessId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/notify")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Operation(summary = "Notify customer", description = "Notify customer that their table is ready")
    @Transactional
    public ResponseEntity<WaitlistEntryDto> notifyCustomer(@PathVariable UUID id, Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();
            User currentUser = userPrincipal.getUser();

            Optional<WaitlistEntry> entry = waitlistEntryRepository.findByIdWithBusinessAndCustomer(id);
            if (entry.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            WaitlistEntry waitlistEntry = entry.get();
            UUID businessId = waitlistEntry.getBusiness().getId();

            // Check if user has access to this business
            boolean isPlatformAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

            if (!isPlatformAdmin && !currentUser.hasBusiness(businessId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (waitlistEntry.canBeNotified()) {
                // Access entities before saving to ensure they're loaded
                Business business = waitlistEntry.getBusiness();
                Customer customer = waitlistEntry.getCustomer();
                String customerPhone = customer.getPhone();
                String businessName = business.getName();
                String businessPhone = business.getPhone();

                waitlistEntry.notifyCustomer();
                WaitlistEntry savedEntry = waitlistEntryRepository.save(waitlistEntry);

                // Send SMS notification using the loaded values
                smsService.sendTableReadyNotification(customerPhone, businessName, businessPhone);

                // Reload with relationships for DTO conversion
                WaitlistEntry entryForDto = waitlistEntryRepository.findByIdWithBusinessAndCustomer(savedEntry.getId())
                        .orElse(savedEntry);
                return ResponseEntity.ok(convertToDto(entryForDto));
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error in notifyCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/seat")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Operation(summary = "Seat customer", description = "Mark customer as seated")
    @Transactional
    public ResponseEntity<WaitlistEntryDto> seatCustomer(@PathVariable UUID id, Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();
            User currentUser = userPrincipal.getUser();

            Optional<WaitlistEntry> entry = waitlistEntryRepository.findByIdWithBusinessAndCustomer(id);
            if (entry.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            WaitlistEntry waitlistEntry = entry.get();
            UUID businessId = waitlistEntry.getBusiness().getId();

            // Check if user has access to this business
            boolean isPlatformAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));

            if (!isPlatformAdmin && !currentUser.hasBusiness(businessId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (waitlistEntry.canBeSeated()) {
                waitlistEntry.seatCustomer();
                WaitlistEntry savedEntry = waitlistEntryRepository.save(waitlistEntry);

                // Update positions of remaining customers
                waitlistEntryRepository.updatePositionsAfterRemoval(
                        waitlistEntry.getBusiness().getId(),
                        waitlistEntry.getPosition());

                // Reload with relationships for DTO conversion
                WaitlistEntry entryForDto = waitlistEntryRepository.findByIdWithBusinessAndCustomer(savedEntry.getId())
                        .orElse(savedEntry);
                return ResponseEntity.ok(convertToDto(entryForDto));
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error in seatCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status", description = "Update waitlist entry status (waiting, called, seated, canceled)")
    public ResponseEntity<WaitlistEntryDto> updateStatus(@PathVariable UUID id,
            @RequestParam WaitlistStatus status,
            Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();

            Optional<WaitlistEntry> entry = waitlistEntryRepository.findById(id);
            if (entry.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Verify the entry belongs to the authenticated user's business
            if (!entry.get().getBusiness().getId().equals(userPrincipal.getBusinessId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            WaitlistEntry waitlistEntry = entry.get();

            switch (status) {
                case NOTIFIED:
                    if (waitlistEntry.canBeNotified()) {
                        waitlistEntry.notifyCustomer();
                        // Send SMS notification
                        smsService.sendTableReadyNotification(
                                waitlistEntry.getCustomer().getPhone(),
                                waitlistEntry.getBusiness().getName(),
                                waitlistEntry.getBusiness().getPhone());
                    }
                    break;
                case SEATED:
                    if (waitlistEntry.canBeSeated()) {
                        waitlistEntry.seatCustomer();
                        // Update positions of remaining customers
                        waitlistEntryRepository.updatePositionsAfterRemoval(
                                waitlistEntry.getBusiness().getId(),
                                waitlistEntry.getPosition());
                    }
                    break;
                case CANCELLED:
                    if (waitlistEntry.isActive()) {
                        waitlistEntry.cancel();
                        // Update positions of remaining customers
                        waitlistEntryRepository.updatePositionsAfterRemoval(
                                waitlistEntry.getBusiness().getId(),
                                waitlistEntry.getPosition());
                    }
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            WaitlistEntry savedEntry = waitlistEntryRepository.save(waitlistEntry);
            return ResponseEntity.ok(convertToDto(savedEntry));

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove from waitlist", description = "Remove a customer from the waitlist")
    public ResponseEntity<Void> removeFromWaitlist(@PathVariable UUID id, Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();

            Optional<WaitlistEntry> entry = waitlistEntryRepository.findById(id);
            if (entry.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Verify the entry belongs to the authenticated user's business
            if (!entry.get().getBusiness().getId().equals(userPrincipal.getBusinessId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            WaitlistEntry waitlistEntry = entry.get();
            if (waitlistEntry.isActive()) {
                waitlistEntry.cancel();
                waitlistEntryRepository.save(waitlistEntry);

                // Update positions of remaining customers
                waitlistEntryRepository.updatePositionsAfterRemoval(
                        waitlistEntry.getBusiness().getId(),
                        waitlistEntry.getPosition());
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private WaitlistEntryDto convertToDto(WaitlistEntry entry) {
        // Ensure relationships are loaded
        if (entry.getBusiness() == null || entry.getCustomer() == null) {
            throw new IllegalStateException("WaitlistEntry must have business and customer loaded");
        }

        WaitlistEntryDto dto = new WaitlistEntryDto(
                entry.getId(),
                entry.getBusiness().getId(),
                entry.getCustomer().getId(),
                entry.getPartySize(),
                entry.getEstimatedWaitTime(),
                entry.getPosition(),
                entry.getStatus(),
                entry.getNotifiedAt(),
                entry.getSeatedAt(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());

        // Add display fields
        dto.setBusinessName(entry.getBusiness().getName());
        dto.setCustomerName(entry.getCustomer().getName());
        dto.setCustomerPhone(entry.getCustomer().getPhone());

        return dto;
    }
}
