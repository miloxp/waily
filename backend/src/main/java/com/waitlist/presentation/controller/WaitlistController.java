package com.waitlist.presentation.controller;

import com.waitlist.application.dto.AddCustomerToWaitlistRequest;
import com.waitlist.application.dto.AddCustomerToWaitlistResponse;
import com.waitlist.application.usecase.AddCustomerToWaitlistUseCase;
import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.Customer;
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

    @GetMapping("/{id}")
    @Operation(summary = "Get waitlist entry by ID", description = "Retrieve a specific waitlist entry by ID")
    public ResponseEntity<WaitlistEntryDto> getWaitlistEntryById(@PathVariable UUID id) {
        Optional<WaitlistEntry> entry = waitlistEntryRepository.findById(id);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER')")
    @Operation(summary = "Notify customer", description = "Notify customer that their table is ready")
    public ResponseEntity<WaitlistEntryDto> notifyCustomer(@PathVariable UUID id) {
        Optional<WaitlistEntry> entry = waitlistEntryRepository.findById(id);
        if (entry.isPresent() && entry.get().canBeNotified()) {
            entry.get().notifyCustomer();
            WaitlistEntry savedEntry = waitlistEntryRepository.save(entry.get());

            // Send SMS notification
            smsService.sendTableReadyNotification(
                    entry.get().getCustomer().getPhone(),
                    entry.get().getBusiness().getName(),
                    entry.get().getBusiness().getPhone());

            return ResponseEntity.ok(convertToDto(savedEntry));
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/seat")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER')")
    @Operation(summary = "Seat customer", description = "Mark customer as seated")
    public ResponseEntity<WaitlistEntryDto> seatCustomer(@PathVariable UUID id) {
        Optional<WaitlistEntry> entry = waitlistEntryRepository.findById(id);
        if (entry.isPresent() && entry.get().canBeSeated()) {
            entry.get().seatCustomer();
            WaitlistEntry savedEntry = waitlistEntryRepository.save(entry.get());

            // Update positions of remaining customers
            waitlistEntryRepository.updatePositionsAfterRemoval(
                    entry.get().getBusiness().getId(),
                    entry.get().getPosition());

            return ResponseEntity.ok(convertToDto(savedEntry));
        }
        return ResponseEntity.badRequest().build();
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

    private WaitlistEntryDto convertToDto(WaitlistEntry entry) {
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
