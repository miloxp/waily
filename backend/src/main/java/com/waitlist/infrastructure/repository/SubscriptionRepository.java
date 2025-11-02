package com.waitlist.infrastructure.repository;

import com.waitlist.domain.entity.Subscription;
import com.waitlist.domain.entity.SubscriptionPlan;
import com.waitlist.domain.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByBusinessId(UUID businessId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByPlan(SubscriptionPlan plan);

    @Query("SELECT s FROM Subscription s WHERE s.endDate < :date AND s.status = 'ACTIVE'")
    List<Subscription> findExpiringSubscriptions(@Param("date") LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.status IN ('ACTIVE', 'TRIAL')")
    List<Subscription> findActiveSubscriptions();

    @Query("SELECT s FROM Subscription s JOIN FETCH s.business WHERE s.business.id = :businessId")
    Optional<Subscription> findByBusinessIdWithBusiness(@Param("businessId") UUID businessId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.business")
    List<Subscription> findAllWithBusiness();
}

