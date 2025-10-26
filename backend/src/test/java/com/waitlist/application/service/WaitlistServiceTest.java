package com.waitlist.application.service;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import com.waitlist.domain.entity.Customer;
import com.waitlist.domain.entity.WaitlistEntry;
import com.waitlist.domain.entity.WaitlistStatus;
import com.waitlist.domain.service.SmsService;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.CustomerRepository;
import com.waitlist.infrastructure.repository.WaitlistEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private WaitlistService waitlistService;

    private Business testBusiness;
    private Customer testCustomer;
    private UUID businessId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        testBusiness = new Business(
                "Test Restaurant",
                BusinessType.RESTAURANT,
                "123 Main St",
                "+1234567890",
                "test@restaurant.com",
                50,
                60);
        testBusiness.setId(businessId);

        testCustomer = new Customer(
                "+1987654321",
                "John Doe",
                "john@example.com");
        testCustomer.setId(customerId);
    }

    @Test
    void addCustomerToWaitlist_Success() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.of(2)); // Current max position is 2, so new position will be 3

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 4, 3);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 3); // 3 * 60 = 180 minutes

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(true);

        // Act
        WaitlistEntry result = waitlistService.addCustomerToWaitlist(businessId, customerId, 4);

        // Assert
        assertNotNull(result);
        assertEquals(testBusiness, result.getBusiness());
        assertEquals(testCustomer, result.getCustomer());
        assertEquals(4, result.getPartySize());
        assertEquals(3, result.getPosition());
        assertEquals(WaitlistStatus.WAITING, result.getStatus());
        assertEquals(180, result.getEstimatedWaitTime());

        // Verify interactions
        verify(businessRepository).findById(businessId);
        verify(customerRepository).findById(customerId);
        verify(waitlistEntryRepository).findActiveEntryByCustomer(businessId, customerId);
        verify(waitlistEntryRepository).findMaxPositionForBusiness(businessId);
        verify(waitlistEntryRepository).save(any(WaitlistEntry.class));
        verify(smsService).sendWaitlistNotification(
                "+1987654321",
                "Test Restaurant",
                180,
                3);
    }

    @Test
    void addCustomerToWaitlist_BusinessNotFound() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            waitlistService.addCustomerToWaitlist(businessId, customerId, 4);
        });

        verify(businessRepository).findById(businessId);
        verify(customerRepository, never()).findById(any());
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    void addCustomerToWaitlist_CustomerNotFound() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            waitlistService.addCustomerToWaitlist(businessId, customerId, 4);
        });

        verify(businessRepository).findById(businessId);
        verify(customerRepository).findById(customerId);
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    void addCustomerToWaitlist_CustomerAlreadyOnWaitlist() {
        // Arrange
        WaitlistEntry existingEntry = new WaitlistEntry(testBusiness, testCustomer, 2, 1);
        existingEntry.setId(UUID.randomUUID());

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.of(existingEntry));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            waitlistService.addCustomerToWaitlist(businessId, customerId, 4);
        });

        verify(businessRepository).findById(businessId);
        verify(customerRepository).findById(customerId);
        verify(waitlistEntryRepository).findActiveEntryByCustomer(businessId, customerId);
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    void addCustomerToWaitlist_FirstCustomerInWaitlist() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.empty()); // No existing entries

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 2, 1);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 1); // 1 * 60 = 60 minutes

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(true);

        // Act
        WaitlistEntry result = waitlistService.addCustomerToWaitlist(businessId, customerId, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPosition());
        assertEquals(60, result.getEstimatedWaitTime());

        verify(waitlistEntryRepository).findMaxPositionForBusiness(businessId);
        verify(waitlistEntryRepository).save(any(WaitlistEntry.class));
        verify(smsService).sendWaitlistNotification(
                "+1987654321",
                "Test Restaurant",
                60,
                1);
    }

    @Test
    void addCustomerToWaitlist_SmsNotificationFails() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.of(0));

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 2, 1);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 1);

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(false); // SMS fails

        // Act
        WaitlistEntry result = waitlistService.addCustomerToWaitlist(businessId, customerId, 2);

        // Assert
        assertNotNull(result);
        // Customer should still be added to waitlist even if SMS fails
        assertEquals(WaitlistStatus.WAITING, result.getStatus());

        verify(smsService).sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt());
    }
}

