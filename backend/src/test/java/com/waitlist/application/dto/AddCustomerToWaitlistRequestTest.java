package com.waitlist.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AddCustomerToWaitlistRequest Validation Tests")
class AddCustomerToWaitlistRequestTest {

    private Validator validator;
    private UUID validCustomerId;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validCustomerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void shouldPassValidationWithValidData() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(validCustomerId, 4);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when customer ID is null")
    void shouldFailValidationWhenCustomerIdIsNull() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(null, 4);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Customer ID is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when party size is null")
    void shouldFailValidationWhenPartySizeIsNull() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(validCustomerId, null);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Party size is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when party size is zero")
    void shouldFailValidationWhenPartySizeIsZero() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(validCustomerId, 0);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Party size must be positive", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when party size is negative")
    void shouldFailValidationWhenPartySizeIsNegative() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(validCustomerId, -1);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Party size must be positive", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation with multiple violations")
    void shouldFailValidationWithMultipleViolations() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(null, -1);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());

        // Check that both validation messages are present
        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(messages.contains("Customer ID is required"));
        assertTrue(messages.contains("Party size must be positive"));
    }

    @Test
    @DisplayName("Should pass validation with minimum valid party size")
    void shouldPassValidationWithMinimumValidPartySize() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(validCustomerId, 1);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation with large party size")
    void shouldPassValidationWithLargePartySize() {
        // Arrange
        AddCustomerToWaitlistRequest request = new AddCustomerToWaitlistRequest(validCustomerId, 20);

        // Act
        Set<ConstraintViolation<AddCustomerToWaitlistRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }
}

