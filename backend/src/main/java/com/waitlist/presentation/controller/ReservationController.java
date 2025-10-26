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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
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
    @Operation(summary = "Get all reservations", description = "Retrieve all reservations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER')")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAllWithBusinessAndCustomer();
        List<ReservationDto> reservationDtos = reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservationDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieve a specific reservation by ID")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable UUID id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
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
    @Operation(summary = "Create reservation", description = "Create a new reservation")
    public ResponseEntity<ReservationDto> createReservation(@Valid @RequestBody ReservationDto reservationDto) {
        // Check if business exists and is active
        Optional<Business> business = businessRepository.findById(reservationDto.getBusinessId());
        if (business.isEmpty() || !business.get().getIsActive()) {
            return ResponseEntity.badRequest().build();
        }

        // Check if customer exists
        Optional<Customer> customer = customerRepository.findById(reservationDto.getCustomerId());
        if (customer.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Check for conflicting reservations
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                reservationDto.getBusinessId(),
                reservationDto.getReservationDate(),
                reservationDto.getReservationTime());

        if (!conflicts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Reservation reservation = convertToEntity(reservationDto, business.get(), customer.get());
        Reservation savedReservation = reservationRepository.save(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedReservation));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER')")
    @Operation(summary = "Confirm reservation", description = "Confirm a pending reservation")
    public ResponseEntity<ReservationDto> confirmReservation(@PathVariable UUID id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            reservation.get().confirm();
            Reservation savedReservation = reservationRepository.save(reservation.get());
            return ResponseEntity.ok(convertToDto(savedReservation));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancel a reservation")
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable UUID id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent() && reservation.get().canBeCancelled()) {
            reservation.get().cancel();
            Reservation savedReservation = reservationRepository.save(reservation.get());
            return ResponseEntity.ok(convertToDto(savedReservation));
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS_OWNER')")
    @Operation(summary = "Complete reservation", description = "Mark a reservation as completed")
    public ResponseEntity<ReservationDto> completeReservation(@PathVariable UUID id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            reservation.get().complete();
            Reservation savedReservation = reservationRepository.save(reservation.get());
            return ResponseEntity.ok(convertToDto(savedReservation));
        }
        return ResponseEntity.notFound().build();
    }

    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto(
                reservation.getId(),
                reservation.getBusiness().getId(),
                reservation.getCustomer().getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getPartySize(),
                reservation.getStatus(),
                reservation.getSpecialRequests(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt());

        // Add display fields
        dto.setBusinessName(reservation.getBusiness().getName());
        dto.setCustomerName(reservation.getCustomer().getName());
        dto.setCustomerPhone(reservation.getCustomer().getPhone());

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
