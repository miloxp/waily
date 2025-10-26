package com.waitlist.presentation.controller;

import com.waitlist.application.dto.AddCustomerToWaitlistRequest;
import com.waitlist.application.dto.AddCustomerToWaitlistResponse;
import com.waitlist.application.usecase.AddCustomerToWaitlistUseCase;
import com.waitlist.domain.entity.WaitlistStatus;
import com.waitlist.infrastructure.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WaitlistController.class)
@DisplayName("WaitlistController Tests")
class WaitlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddCustomerToWaitlistUseCase addCustomerToWaitlistUseCase;

    private AddCustomerToWaitlistRequest testRequest;
    private AddCustomerToWaitlistResponse testResponse;
    private UUID businessId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        testRequest = new AddCustomerToWaitlistRequest(customerId, 4);

        testResponse = new AddCustomerToWaitlistResponse(
                UUID.randomUUID(),
                businessId,
                customerId,
                "Test Restaurant",
                "John Doe",
                "+1987654321",
                4,
                1,
                60,
                WaitlistStatus.WAITING,
                LocalDateTime.now(),
                true);
    }

    @Test
    @DisplayName("Should successfully add customer to waitlist")
    @WithMockUser
    void shouldSuccessfullyAddCustomerToWaitlist() throws Exception {
        // Arrange
        when(addCustomerToWaitlistUseCase.execute(any(AddCustomerToWaitlistRequest.class), eq(businessId)))
                .thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest))
                .with(authentication(createMockAuthentication(businessId))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.waitlistEntryId").value(testResponse.getWaitlistEntryId().toString()))
                .andExpect(jsonPath("$.businessId").value(businessId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.businessName").value("Test Restaurant"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.customerPhone").value("+1987654321"))
                .andExpect(jsonPath("$.partySize").value(4))
                .andExpect(jsonPath("$.position").value(1))
                .andExpect(jsonPath("$.estimatedWaitTime").value(60))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.smsNotificationSent").value(true));
    }

    @Test
    @DisplayName("Should return 400 when request validation fails")
    @WithMockUser
    void shouldReturn400WhenRequestValidationFails() throws Exception {
        // Arrange - Invalid request with null customer ID
        AddCustomerToWaitlistRequest invalidRequest = new AddCustomerToWaitlistRequest(null, 4);

        // Act & Assert
        mockMvc.perform(post("/api/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(authentication(createMockAuthentication(businessId))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when business not found")
    @WithMockUser
    void shouldReturn400WhenBusinessNotFound() throws Exception {
        // Arrange
        when(addCustomerToWaitlistUseCase.execute(any(AddCustomerToWaitlistRequest.class), eq(businessId)))
                .thenThrow(new IllegalArgumentException("Business not found or inactive"));

        // Act & Assert
        mockMvc.perform(post("/api/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest))
                .with(authentication(createMockAuthentication(businessId))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when customer already on waitlist")
    @WithMockUser
    void shouldReturn409WhenCustomerAlreadyOnWaitlist() throws Exception {
        // Arrange
        when(addCustomerToWaitlistUseCase.execute(any(AddCustomerToWaitlistRequest.class), eq(businessId)))
                .thenThrow(new IllegalStateException("Customer is already on the waitlist"));

        // Act & Assert
        mockMvc.perform(post("/api/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest))
                .with(authentication(createMockAuthentication(businessId))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 500 when unexpected error occurs")
    @WithMockUser
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        // Arrange
        when(addCustomerToWaitlistUseCase.execute(any(AddCustomerToWaitlistRequest.class), eq(businessId)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/api/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest))
                .with(authentication(createMockAuthentication(businessId))))
                .andExpect(status().isInternalServerError());
    }

    private org.springframework.security.core.Authentication createMockAuthentication(UUID businessId) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = mock(
                CustomUserDetailsService.CustomUserPrincipal.class);
        when(userPrincipal.getBusinessId()).thenReturn(businessId);

        org.springframework.security.core.Authentication authentication = mock(
                org.springframework.security.core.Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        return authentication;
    }
}
