package com.waitlist.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusinessController.class)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessRepository businessRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllBusinesses_ShouldReturnBusinesses() throws Exception {
        Business business = new Business(
                "Test Restaurant",
                BusinessType.RESTAURANT,
                "123 Main St",
                "+1234567890",
                "test@restaurant.com",
                50,
                60);
        business.setId(UUID.randomUUID());

        when(businessRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(business));

        mockMvc.perform(get("/api/businesses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test Restaurant"))
                .andExpect(jsonPath("$[0].type").value("RESTAURANT"));
    }

    @Test
    void getBusinessById_WhenExists_ShouldReturnBusiness() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business business = new Business(
                "Test Restaurant",
                BusinessType.RESTAURANT,
                "123 Main St",
                "+1234567890",
                "test@restaurant.com",
                50,
                60);
        business.setId(businessId);

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

        mockMvc.perform(get("/api/businesses/{id}", businessId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(businessId.toString()))
                .andExpect(jsonPath("$.name").value("Test Restaurant"));
    }

    @Test
    void getBusinessById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        UUID businessId = UUID.randomUUID();

        when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/businesses/{id}", businessId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_WithValidData_ShouldCreateBusiness() throws Exception {
        Business business = new Business(
                "New Restaurant",
                BusinessType.RESTAURANT,
                "456 Oak St",
                "+1987654321",
                "new@restaurant.com",
                75,
                45);
        UUID businessId = UUID.randomUUID();
        business.setId(businessId);

        when(businessRepository.existsByNameAndIsActiveTrue("New Restaurant")).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        String businessJson = objectMapper.writeValueAsString(business);

        mockMvc.perform(post("/api/businesses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(businessJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Restaurant"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_WithExistingName_ShouldReturnConflict() throws Exception {
        Business business = new Business(
                "Existing Restaurant",
                BusinessType.RESTAURANT,
                "456 Oak St",
                "+1987654321",
                "existing@restaurant.com",
                75,
                45);

        when(businessRepository.existsByNameAndIsActiveTrue("Existing Restaurant")).thenReturn(true);

        String businessJson = objectMapper.writeValueAsString(business);

        mockMvc.perform(post("/api/businesses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(businessJson))
                .andExpect(status().isConflict());
    }
}

