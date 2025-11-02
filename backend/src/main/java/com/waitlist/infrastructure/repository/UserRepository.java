package com.waitlist.infrastructure.repository;

import com.waitlist.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndIsActiveTrue(String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.businesses WHERE u.username = :username AND u.isActive = true")
    Optional<User> findByUsernameAndIsActiveTrueWithBusiness(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u JOIN u.businesses b WHERE b.id = :businessId AND u.isActive = true")
    java.util.List<User> findByBusinessIdAndIsActiveTrue(@Param("businessId") UUID businessId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.businesses b WHERE b.id = :businessId AND u.username = :username AND u.isActive = true")
    Optional<User> findByBusinessIdAndUsernameAndIsActiveTrue(@Param("businessId") UUID businessId,
            @Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.businesses")
    java.util.List<User> findAllWithBusinesses();
}

