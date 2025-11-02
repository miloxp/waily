package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.presentation.dto.BusinessDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.waitlist.infrastructure.security.CustomUserDetailsService;
import com.waitlist.domain.entity.User;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/business")
@Tag(name = "Business", description = "Business management endpoints")
public class BusinessController {

    @Autowired
    private BusinessRepository businessRepository;

    @GetMapping
    @Operation(summary = "List businesses", description = "Retrieve all active businesses")
    public ResponseEntity<List<BusinessDto>> listBusinesses() {
        List<Business> businesses = businessRepository.findByIsActiveTrue();
        List<BusinessDto> businessDtos = businesses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(businessDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business details", description = "Retrieve a specific business by ID")
    public ResponseEntity<BusinessDto> getBusinessDetails(@PathVariable UUID id, Authentication authentication) {
        try {
            Optional<Business> business = businessRepository.findById(id);
            
            if (business.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Business businessEntity = business.get();
            
            // Check if user is PLATFORM_ADMIN (can view any business)
            boolean isPlatformAdmin = false;
            boolean hasAccessToBusiness = false;
            
            if (authentication != null && authentication.getPrincipal() != null) {
                try {
                    isPlatformAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
                    
                    if (!isPlatformAdmin) {
                        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                        User user = userPrincipal.getUser();
                        if (user != null && user.hasBusiness(businessEntity.getId())) {
                            hasAccessToBusiness = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error checking user authorization: " + e.getMessage());
                }
            }

            // Allow access if:
            // 1. Business is active (anyone can view active businesses)
            // 2. User is PLATFORM_ADMIN (can view any business)
            // 3. User belongs to this business (can view their own business even if inactive)
            boolean canAccess = businessEntity.getIsActive() || isPlatformAdmin || hasAccessToBusiness;
            
            if (!canAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(convertToDto(businessEntity));
        } catch (Exception e) {
            System.err.println("Error in getBusinessDetails: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search businesses", description = "Search businesses by name or address")
    public ResponseEntity<List<BusinessDto>> searchBusinesses(@RequestParam String searchTerm) {
        List<Business> businesses = businessRepository.searchActiveBusinesses(searchTerm);
        List<BusinessDto> businessDtos = businesses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(businessDtos);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get businesses by type", description = "Retrieve businesses by business type")
    public ResponseEntity<List<BusinessDto>> getBusinessesByType(@PathVariable BusinessType type) {
        List<Business> businesses = businessRepository.findByTypeAndIsActiveTrue(type);
        List<BusinessDto> businessDtos = businesses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(businessDtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Create business", description = "Create a new business account (Platform Admin only)")
    public ResponseEntity<BusinessDto> createBusiness(@Valid @RequestBody BusinessDto businessDto) {
        if (businessRepository.existsByNameAndIsActiveTrue(businessDto.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Business business = convertToEntity(businessDto);
        Business savedBusiness = businessRepository.save(business);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedBusiness));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update business info", description = "Update an existing business")
    public ResponseEntity<BusinessDto> updateBusinessInfo(@PathVariable UUID id,
            @Valid @RequestBody BusinessDto businessDto) {
        Optional<Business> existingBusiness = businessRepository.findById(id);
        if (existingBusiness.isEmpty() || !existingBusiness.get().getIsActive()) {
            return ResponseEntity.notFound().build();
        }

        Business business = existingBusiness.get();
        business.setName(businessDto.getName());
        business.setType(businessDto.getType());
        business.setAddress(businessDto.getAddress());
        business.setPhone(businessDto.getPhone());
        business.setEmail(businessDto.getEmail());
        business.setCapacity(businessDto.getCapacity());
        business.setAverageServiceTime(businessDto.getAverageServiceTime());

        Business savedBusiness = businessRepository.save(business);
        return ResponseEntity.ok(convertToDto(savedBusiness));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    @Operation(summary = "Delete business", description = "Deactivate a business (Platform Admin or Business Owner)")
    public ResponseEntity<Void> deleteBusiness(@PathVariable UUID id) {
        Optional<Business> business = businessRepository.findById(id);
        if (business.isPresent() && business.get().getIsActive()) {
            business.get().deactivate();
            businessRepository.save(business.get());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private BusinessDto convertToDto(Business business) {
        return new BusinessDto(
                business.getId(),
                business.getName(),
                business.getType(),
                business.getAddress(),
                business.getPhone(),
                business.getEmail(),
                business.getCapacity(),
                business.getAverageServiceTime(),
                business.getIsActive(),
                business.getCreatedAt(),
                business.getUpdatedAt());
    }

    private Business convertToEntity(BusinessDto businessDto) {
        return new Business(
                businessDto.getName(),
                businessDto.getType(),
                businessDto.getAddress(),
                businessDto.getPhone(),
                businessDto.getEmail(),
                businessDto.getCapacity(),
                businessDto.getAverageServiceTime());
    }
}
