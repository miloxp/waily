package com.waitlist.infrastructure.repository;

import com.waitlist.domain.entity.WaitlistEntry;
import com.waitlist.domain.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {

        List<WaitlistEntry> findByBusinessIdAndStatusOrderByPositionAsc(UUID businessId, WaitlistStatus status);

        List<WaitlistEntry> findByBusinessIdOrderByPositionAsc(UUID businessId);

        List<WaitlistEntry> findByCustomerId(UUID customerId);

        @Query("SELECT w FROM WaitlistEntry w JOIN FETCH w.business JOIN FETCH w.customer WHERE w.business.id = :businessId AND "
                        +
                        "w.status IN ('WAITING', 'NOTIFIED') ORDER BY w.position ASC")
        List<WaitlistEntry> findActiveWaitlistEntries(@Param("businessId") UUID businessId);

        @Query("SELECT w FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.status = 'WAITING' ORDER BY w.position ASC")
        List<WaitlistEntry> findWaitingEntries(@Param("businessId") UUID businessId);

        @Query("SELECT w FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.status = 'NOTIFIED' ORDER BY w.position ASC")
        List<WaitlistEntry> findNotifiedEntries(@Param("businessId") UUID businessId);

        @Query("SELECT MAX(w.position) FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.status IN ('WAITING', 'NOTIFIED')")
        Optional<Integer> findMaxPositionForBusiness(@Param("businessId") UUID businessId);

        @Query("SELECT COUNT(w) FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.status = 'WAITING'")
        long countWaitingEntries(@Param("businessId") UUID businessId);

        @Query("SELECT COUNT(w) FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.status IN ('WAITING', 'NOTIFIED')")
        long countActiveEntries(@Param("businessId") UUID businessId);

        @Query("SELECT w FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.customer.id = :customerId AND w.status IN ('WAITING', 'NOTIFIED')")
        Optional<WaitlistEntry> findActiveEntryByCustomer(@Param("businessId") UUID businessId,
                        @Param("customerId") UUID customerId);

        @Modifying
        @Query("UPDATE WaitlistEntry w SET w.position = w.position - 1 WHERE w.business.id = :businessId AND " +
                        "w.position > :removedPosition AND w.status IN ('WAITING', 'NOTIFIED')")
        void updatePositionsAfterRemoval(@Param("businessId") UUID businessId,
                        @Param("removedPosition") Integer removedPosition);

        @Query("SELECT w FROM WaitlistEntry w WHERE w.business.id = :businessId AND " +
                        "w.partySize <= :maxPartySize AND w.status = 'WAITING' " +
                        "ORDER BY w.position ASC")
        List<WaitlistEntry> findWaitingEntriesByPartySize(@Param("businessId") UUID businessId,
                        @Param("maxPartySize") Integer maxPartySize);

        @Query("SELECT w FROM WaitlistEntry w JOIN FETCH w.business JOIN FETCH w.customer WHERE w.id = :id")
        Optional<WaitlistEntry> findByIdWithBusinessAndCustomer(@Param("id") UUID id);
}

