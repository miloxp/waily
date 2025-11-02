package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.Customer;
import com.waitlist.domain.entity.Reservation;
import com.waitlist.domain.entity.ReservationStatus;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.CustomerRepository;
import com.waitlist.infrastructure.repository.ReservationRepository;
import com.waitlist.presentation.dto.ReservationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.waitlist.infrastructure.security.CustomUserDetailsService;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.UserRole;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation management endpoints")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    @Operation(summary = "Get all reservations", description = "Retrieve all reservations (Platform Admin sees all, business users see their business's reservations)")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReservationDto>> getAllReservations(Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();
            UserRole currentUserRole = currentUser.getRole();

            List<Reservation> reservations;
            
            if (currentUserRole == UserRole.PLATFORM_ADMIN) {
                // PLATFORM_ADMIN sees all reservations
                reservations = reservationRepository.findAllWithBusinessAndCustomer();
            } else {
                // Business users see only reservations for their businesses
                java.util.Set<Business> ownedBusinesses = currentUser.getBusinesses();
                
                if (ownedBusinesses.isEmpty()) {
                    reservations = new java.util.ArrayList<>();
                } else {
                    // Collect business IDs
                    java.util.List<UUID> businessIds = ownedBusinesses.stream()
                            .map(Business::getId)
                            .collect(Collectors.toList());
                    
                    // Get active reservations (PENDING and CONFIRMED) for all their businesses in one query
                    java.util.List<ReservationStatus> activeStatuses = java.util.Arrays.asList(
                            ReservationStatus.PENDING,
                            ReservationStatus.CONFIRMED
                    );
                    reservations = reservationRepository.findByBusinessIdsAndStatuses(businessIds, activeStatuses);
                }
            }

            List<ReservationDto> reservationDtos = reservations.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservationDtos);
        } catch (Exception e) {
            System.err.println("Error in getAllReservations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieve a specific reservation by ID")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable UUID id) {
        Optional<Reservation> reservation = reservationRepository.findByIdWithBusinessAndCustomer(id);
        if (reservation.isPresent()) {
            return ResponseEntity.ok(convertToDto(reservation.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "Get reservations by business", description = "Retrieve reservations for a specific business")
    public ResponseEntity<List<ReservationDto>> getReservationsByBusiness(@PathVariable UUID businessId,
            @RequestParam(required = false) LocalDate date) {
        List<Reservation> reservations;
        if (date != null) {
            reservations = reservationRepository.findByBusinessIdAndReservationDate(businessId, date);
        } else {
            reservations = reservationRepository.findByBusinessIdAndStatus(businessId, ReservationStatus.PENDING);
        }

        List<ReservationDto> reservationDtos = reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservationDtos);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get reservations by customer", description = "Retrieve reservations for a specific customer")
    public ResponseEntity<List<ReservationDto>> getReservationsByCustomer(@PathVariable UUID customerId) {
        List<Reservation> reservations = reservationRepository.findByCustomerId(customerId);
        List<ReservationDto> reservationDtos = reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservationDtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Operation(summary = "Create reservation", description = "Create a new reservation")
    @Transactional
    public ResponseEntity<ReservationDto> createReservation(
            @Valid @RequestBody ReservationDto reservationDto,
            Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();
            UserRole currentUserRole = currentUser.getRole();
            
            UUID businessId = reservationDto.getBusinessId();
            
            // Check if business exists and is active
            Optional<Business> business = businessRepository.findById(businessId);
            if (business.isEmpty() || !business.get().getIsActive()) {
                return ResponseEntity.badRequest().build();
            }

            // Check if user has access to this business (unless PLATFORM_ADMIN)
            if (currentUserRole != UserRole.PLATFORM_ADMIN && !currentUser.hasBusiness(businessId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if customer exists
            Optional<Customer> customer = customerRepository.findById(reservationDto.getCustomerId());
            if (customer.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Check for conflicting reservations
            List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                    businessId,
                    reservationDto.getReservationDate(),
                    reservationDto.getReservationTime());

            if (!conflicts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Reservation reservation = convertToEntity(reservationDto, business.get(), customer.get());
            Reservation savedReservation = reservationRepository.save(reservation);
            
            // Reload with relationships for DTO conversion
            Reservation reservationForDto = reservationRepository.findByIdWithBusinessAndCustomer(savedReservation.getId())
                    .orElse(savedReservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(reservationForDto));
        } catch (Exception e) {
            System.err.println("Error in createReservation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    @Transactional
    @Operation(summary = "Confirm reservation", description = "Confirm a pending reservation")
    public ResponseEntity<ReservationDto> confirmReservation(@PathVariable UUID id) {
        try {
            Optional<Reservation> reservationOpt = reservationRepository.findByIdWithBusinessAndCustomer(id);
            if (reservationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Reservation reservation = reservationOpt.get();
            
            // Check if reservation can be confirmed (must be PENDING)
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            // Access relationships before save to ensure they're loaded
            Business business = reservation.getBusiness();
            Customer customer = reservation.getCustomer();
            
            if (business == null || customer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Trigger lazy loading if needed
            business.getName();
            customer.getName();
            customer.getPhone();
            
            reservation.confirm();
            Reservation savedReservation = reservationRepository.save(reservation);
            
            return ResponseEntity.ok(convertToDto(savedReservation));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancel a reservation")
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable UUID id) {
        Optional<Reservation> reservation = reservationRepository.findByIdWithBusinessAndCustomer(id);
        if (reservation.isPresent() && reservation.get().canBeCancelled()) {
            reservation.get().cancel();
            reservationRepository.save(reservation.get());
            // Refetch to ensure relationships are loaded
            Optional<Reservation> savedReservation = reservationRepository.findByIdWithBusinessAndCustomer(id);
            if (savedReservation.isPresent()) {
                return ResponseEntity.ok(convertToDto(savedReservation.get()));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    @Transactional
    @Operation(summary = "Complete reservation", description = "Mark a reservation as completed")
    public ResponseEntity<ReservationDto> completeReservation(@PathVariable UUID id) {
        try {
            Optional<Reservation> reservationOpt = reservationRepository.findByIdWithBusinessAndCustomer(id);
            if (reservationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Reservation reservation = reservationOpt.get();
            
            // Check if reservation can be completed (must be CONFIRMED)
            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            // Access relationships before save to ensure they're loaded
            Business business = reservation.getBusiness();
            Customer customer = reservation.getCustomer();
            
            if (business == null || customer == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Trigger lazy loading if needed
            business.getName();
            customer.getName();
            customer.getPhone();
            
            reservation.complete();
            Reservation savedReservation = reservationRepository.save(reservation);
            
            return ResponseEntity.ok(convertToDto(savedReservation));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ReservationDto convertToDto(Reservation reservation) {
        Business business = reservation.getBusiness();
        Customer customer = reservation.getCustomer();
        
        if (business == null || customer == null) {
            throw new IllegalStateException("Reservation must have business and customer loaded");
        }
        
        ReservationDto dto = new ReservationDto(
                reservation.getId(),
                business.getId(),
                customer.getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getPartySize(),
                reservation.getStatus(),
                reservation.getSpecialRequests(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt());

        // Add display fields
        dto.setBusinessName(business.getName());
        dto.setCustomerName(customer.getName());
        dto.setCustomerPhone(customer.getPhone());

        return dto;
    }

    private Reservation convertToEntity(ReservationDto reservationDto, Business business, Customer customer) {
        return new Reservation(
                business,
                customer,
                reservationDto.getReservationDate(),
                reservationDto.getReservationTime(),
                reservationDto.getPartySize(),
                reservationDto.getSpecialRequests());
    }
}
