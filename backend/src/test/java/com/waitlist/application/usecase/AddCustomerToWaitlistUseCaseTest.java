package com.waitlist.application.usecase;

import com.waitlist.application.dto.AddCustomerToWaitlistRequest;
import com.waitlist.application.dto.AddCustomerToWaitlistResponse;
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
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("AddCustomerToWaitlistUseCase Tests")
class AddCustomerToWaitlistUseCaseTest {

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private AddCustomerToWaitlistUseCase useCase;

    private Business testBusiness;
    private Customer testCustomer;
    private AddCustomerToWaitlistRequest testRequest;
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

        testRequest = new AddCustomerToWaitlistRequest(customerId, 4);
    }

    @Test
    @DisplayName("Should successfully add customer to waitlist")
    void shouldSuccessfullyAddCustomerToWaitlist() {
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
        AddCustomerToWaitlistResponse response = useCase.execute(testRequest, businessId);

        // Assert
        assertNotNull(response);
        assertEquals(savedEntry.getId(), response.getWaitlistEntryId());
        assertEquals(businessId, response.getBusinessId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals("Test Restaurant", response.getBusinessName());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("+1987654321", response.getCustomerPhone());
        assertEquals(4, response.getPartySize());
        assertEquals(3, response.getPosition());
        assertEquals(180, response.getEstimatedWaitTime());
        assertEquals(WaitlistStatus.WAITING, response.getStatus());
        assertTrue(response.isSmsNotificationSent());

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
    @DisplayName("Should throw IllegalArgumentException when business not found")
    void shouldThrowExceptionWhenBusinessNotFound() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(testRequest, businessId);
        });

        assertEquals("Business not found or inactive", exception.getMessage());

        verify(businessRepository).findById(businessId);
        verify(customerRepository, never()).findById(any());
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when business is inactive")
    void shouldThrowExceptionWhenBusinessInactive() {
        // Arrange
        testBusiness.setIsActive(false);
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(testRequest, businessId);
        });

        assertEquals("Business not found or inactive", exception.getMessage());

        verify(businessRepository).findById(businessId);
        verify(customerRepository, never()).findById(any());
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(testRequest, businessId);
        });

        assertEquals("Customer not found", exception.getMessage());

        verify(businessRepository).findById(businessId);
        verify(customerRepository).findById(customerId);
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when customer already on waitlist")
    void shouldThrowExceptionWhenCustomerAlreadyOnWaitlist() {
        // Arrange
        WaitlistEntry existingEntry = new WaitlistEntry(testBusiness, testCustomer, 2, 1);
        existingEntry.setId(UUID.randomUUID());

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.of(existingEntry));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            useCase.execute(testRequest, businessId);
        });

        assertEquals("Customer is already on the waitlist", exception.getMessage());

        verify(businessRepository).findById(businessId);
        verify(customerRepository).findById(customerId);
        verify(waitlistEntryRepository).findActiveEntryByCustomer(businessId, customerId);
        verify(waitlistEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should assign position 1 when waitlist is empty")
    void shouldAssignPosition1WhenWaitlistEmpty() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.empty()); // No existing entries

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 4, 1);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 1); // 1 * 60 = 60 minutes

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(true);

        // Act
        AddCustomerToWaitlistResponse response = useCase.execute(testRequest, businessId);

        // Assert
        assertEquals(1, response.getPosition());
        assertEquals(60, response.getEstimatedWaitTime());

        verify(waitlistEntryRepository).findMaxPositionForBusiness(businessId);
        verify(waitlistEntryRepository).save(any(WaitlistEntry.class));
        verify(smsService).sendWaitlistNotification(
                "+1987654321",
                "Test Restaurant",
                60,
                1);
    }

    @Test
    @DisplayName("Should handle SMS notification failure gracefully")
    void shouldHandleSmsNotificationFailureGracefully() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.of(0));

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 4, 1);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 1);

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(false); // SMS fails

        // Act
        AddCustomerToWaitlistResponse response = useCase.execute(testRequest, businessId);

        // Assert
        assertNotNull(response);
        assertEquals(WaitlistStatus.WAITING, response.getStatus());
        assertFalse(response.isSmsNotificationSent());

        verify(smsService).sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should handle SMS service exception gracefully")
    void shouldHandleSmsServiceExceptionGracefully() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.of(0));

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 4, 1);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 1);

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("SMS service unavailable"));

        // Act
        AddCustomerToWaitlistResponse response = useCase.execute(testRequest, businessId);

        // Assert
        assertNotNull(response);
        assertEquals(WaitlistStatus.WAITING, response.getStatus());
        assertFalse(response.isSmsNotificationSent());

        verify(smsService).sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should calculate correct estimated wait time")
    void shouldCalculateCorrectEstimatedWaitTime() {
        // Arrange
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(waitlistEntryRepository.findActiveEntryByCustomer(businessId, customerId))
                .thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionForBusiness(businessId))
                .thenReturn(Optional.of(4)); // Position 5

        WaitlistEntry savedEntry = new WaitlistEntry(testBusiness, testCustomer, 4, 5);
        savedEntry.setId(UUID.randomUUID());
        savedEntry.calculateEstimatedWaitTime(60, 5); // 5 * 60 = 300 minutes

        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenReturn(savedEntry);
        when(smsService.sendWaitlistNotification(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(true);

        // Act
        AddCustomerToWaitlistResponse response = useCase.execute(testRequest, businessId);

        // Assert
        assertEquals(5, response.getPosition());
        assertEquals(300, response.getEstimatedWaitTime());

        verify(smsService).sendWaitlistNotification(
                "+1987654321",
                "Test Restaurant",
                300,
                5);
    }
}

