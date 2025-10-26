package com.waitlist.infrastructure.repository;

import com.waitlist.domain.entity.Reservation;
import com.waitlist.domain.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

        List<Reservation> findByBusinessIdAndReservationDate(UUID businessId, LocalDate date);

        List<Reservation> findByBusinessIdAndReservationDateAndStatus(
                        UUID businessId, LocalDate date, ReservationStatus status);

        List<Reservation> findByCustomerId(UUID customerId);

        List<Reservation> findByBusinessIdAndStatus(UUID businessId, ReservationStatus status);

        @Query("SELECT r FROM Reservation r WHERE r.business.id = :businessId AND " +
                        "r.reservationDate = :date AND r.reservationTime = :time AND " +
                        "r.status IN ('PENDING', 'CONFIRMED')")
        List<Reservation> findConflictingReservations(
                        @Param("businessId") UUID businessId,
                        @Param("date") LocalDate date,
                        @Param("time") LocalTime time);

        @Query("SELECT r FROM Reservation r WHERE r.business.id = :businessId AND " +
                        "r.reservationDate = :date AND r.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY r.reservationTime")
        List<Reservation> findActiveReservationsForDate(
                        @Param("businessId") UUID businessId,
                        @Param("date") LocalDate date);

        @Query("SELECT COUNT(r) FROM Reservation r WHERE r.business.id = :businessId AND " +
                        "r.reservationDate = :date AND r.status IN ('PENDING', 'CONFIRMED')")
        long countActiveReservationsForDate(@Param("businessId") UUID businessId, @Param("date") LocalDate date);

        @Query("SELECT r FROM Reservation r WHERE r.business.id = :businessId AND " +
                        "r.reservationDate >= :startDate AND r.reservationDate <= :endDate " +
                        "ORDER BY r.reservationDate, r.reservationTime")
        List<Reservation> findReservationsInDateRange(
                        @Param("businessId") UUID businessId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT r FROM Reservation r JOIN FETCH r.business JOIN FETCH r.customer")
        List<Reservation> findAllWithBusinessAndCustomer();
}
