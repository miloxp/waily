package com.waitlist.infrastructure.repository;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByIsActiveTrue();

    List<Business> findByTypeAndIsActiveTrue(BusinessType type);

    Optional<Business> findByNameAndIsActiveTrue(String name);

    @Query("SELECT b FROM Business b WHERE b.isActive = true AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Business> searchActiveBusinesses(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(b) FROM Business b WHERE b.isActive = true")
    long countActiveBusinesses();

    boolean existsByNameAndIsActiveTrue(String name);
}

