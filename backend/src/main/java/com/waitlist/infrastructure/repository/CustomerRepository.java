package com.waitlist.infrastructure.repository;

import com.waitlist.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "c.phone LIKE CONCAT('%', :searchTerm, '%') OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);

    boolean existsByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE c.phone IN :phones")
    List<Customer> findByPhones(@Param("phones") List<String> phones);

    @Query("SELECT DISTINCT c FROM Customer c " +
           "JOIN Reservation r ON r.customer.id = c.id " +
           "WHERE r.business.id = :businessId")
    List<Customer> findCustomersWithReservationsByBusiness(@Param("businessId") UUID businessId);

    @Query("SELECT DISTINCT c FROM Customer c " +
           "JOIN WaitlistEntry w ON w.customer.id = c.id " +
           "WHERE w.business.id = :businessId")
    List<Customer> findCustomersWithWaitlistEntriesByBusiness(@Param("businessId") UUID businessId);

    @Query("SELECT DISTINCT c FROM Customer c " +
           "WHERE c.id IN (SELECT DISTINCT r.customer.id FROM Reservation r WHERE r.business.id = :businessId) " +
           "OR c.id IN (SELECT DISTINCT w.customer.id FROM WaitlistEntry w WHERE w.business.id = :businessId)")
    List<Customer> findCustomersByBusiness(@Param("businessId") UUID businessId);

    @Query("SELECT DISTINCT c FROM Customer c " +
           "WHERE c.id IN (SELECT DISTINCT r.customer.id FROM Reservation r WHERE r.business.id IN :businessIds) " +
           "OR c.id IN (SELECT DISTINCT w.customer.id FROM WaitlistEntry w WHERE w.business.id IN :businessIds)")
    List<Customer> findCustomersByBusinesses(@Param("businessIds") java.util.List<UUID> businessIds);
}

