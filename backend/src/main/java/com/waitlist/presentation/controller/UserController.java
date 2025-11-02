package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.UserRole;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.UserRepository;
import com.waitlist.presentation.dto.CreateUserRequest;
import com.waitlist.presentation.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.waitlist.infrastructure.security.CustomUserDetailsService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    @Operation(summary = "Get all users", description = "Retrieve all users (Platform Admin sees all, Business Owner sees their staff)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserDto>> getAllUsers(Authentication authentication) {
        try {
            // Get current user
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();
            UserRole currentUserRole = currentUser.getRole();

            // Fetch users based on role
            List<User> users;
            if (currentUserRole == UserRole.PLATFORM_ADMIN) {
                // PLATFORM_ADMIN sees all users
                users = userRepository.findAllWithBusinesses();
            } else if (currentUserRole == UserRole.BUSINESS_OWNER) {
                // BUSINESS_OWNER sees only their BUSINESS_STAFF
                java.util.Set<Business> ownedBusinesses = currentUser.getBusinesses();
                users = new java.util.ArrayList<>();
                for (Business ownedBusiness : ownedBusinesses) {
                    List<User> staffUsers = userRepository.findByBusinessIdAndIsActiveTrue(ownedBusiness.getId());
                    for (User staffUser : staffUsers) {
                        // Load businesses relationship
                        staffUser.getBusinesses().size();
                        // Only include BUSINESS_STAFF
                        if (staffUser.getRole() == UserRole.BUSINESS_STAFF) {
                            users.add(staffUser);
                        }
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Remove duplicates that might occur from LEFT JOIN FETCH
            java.util.Map<UUID, User> uniqueUsersMap = new java.util.LinkedHashMap<>();
            for (User user : users) {
                if (!uniqueUsersMap.containsKey(user.getId())) {
                    uniqueUsersMap.put(user.getId(), user);
                    // Force initialization of businesses within transaction
                    java.util.Set<Business> businesses = user.getBusinesses();
                    if (businesses != null) {
                        int size = businesses.size(); // Trigger Hibernate to load the collection
                        System.out.println("User " + user.getUsername() + " has " + size + " businesses");
                        // Initialize each business
                        for (Business business : businesses) {
                            business.getId(); // Ensure business is fully loaded
                            business.getName(); // Ensure name is loaded
                            System.out.println("  - Business: " + business.getName() + " (ID: " + business.getId() + ")");
                        }
                    } else {
                        System.out.println("User " + user.getUsername() + " has null businesses");
                    }
                }
            }
            
            // Convert to DTOs
            List<UserDto> userDtos = uniqueUsersMap.values().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            System.out.println("Returning " + userDtos.size() + " users");
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            System.err.println("Error in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
    @Transactional
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User userEntity = user.get();
            // Load businesses relationship
            userEntity.getBusinesses().size();
            return ResponseEntity.ok(convertToDto(userEntity));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Get users by business ID", description = "Retrieve all users for a specific business")
    @Transactional
    public ResponseEntity<List<UserDto>> getUsersByBusiness(@PathVariable UUID businessId) {
        List<User> users = userRepository.findByBusinessIdAndIsActiveTrue(businessId);
        // Load businesses relationship for each user
        users.forEach(u -> u.getBusinesses().size());
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER')")
    @Transactional
    @Operation(summary = "Create user", description = "Create a new user (Platform Admin or Business Owner)")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest,
                                               Authentication authentication) {
        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        User currentUser = userPrincipal.getUser();
        UserRole currentUserRole = currentUser.getRole();

        // Role-based validation
        if (currentUserRole == UserRole.PLATFORM_ADMIN) {
            // PLATFORM_ADMIN can only create BUSINESS_OWNER
            if (createUserRequest.getRole() != UserRole.BUSINESS_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null); // Or create an error DTO
            }
        } else if (currentUserRole == UserRole.BUSINESS_OWNER) {
            // BUSINESS_OWNER can only create BUSINESS_STAFF
            if (createUserRequest.getRole() != UserRole.BUSINESS_STAFF) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
            }

            // Check limit: BUSINESS_OWNER can only create max 3 BUSINESS_STAFF
            // Count existing BUSINESS_STAFF users for businesses owned by current user
            int existingStaffCount = 0;
            for (Business ownedBusiness : currentUser.getBusinesses()) {
                List<User> staffUsers = userRepository.findByBusinessIdAndIsActiveTrue(ownedBusiness.getId());
                existingStaffCount += (int) staffUsers.stream()
                    .filter(u -> u.getRole() == UserRole.BUSINESS_STAFF)
                    .count();
            }

            if (existingStaffCount >= 3) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Max limit reached
            }

            // BUSINESS_OWNER can only assign their own businesses
            for (UUID businessId : createUserRequest.getBusinessIds()) {
                if (!currentUser.hasBusiness(businessId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null); // Trying to assign a business they don't own
                }
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Check if username already exists
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Check if email already exists
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Validate that all businesses exist
        for (UUID businessId : createUserRequest.getBusinessIds()) {
            Optional<Business> business = businessRepository.findById(businessId);
            if (business.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        // Create user
        User user = new User(
                createUserRequest.getUsername(),
                passwordEncoder.encode(createUserRequest.getPassword()),
                createUserRequest.getEmail(),
                createUserRequest.getRole());

        user.setIsActive(createUserRequest.getIsActive() != null ? createUserRequest.getIsActive() : true);

        // Assign businesses
        for (UUID businessId : createUserRequest.getBusinessIds()) {
            Business business = businessRepository.findById(businessId).get();
            user.addBusiness(business);
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id,
                                               @Valid @RequestBody CreateUserRequest updateUserRequest) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existingUser.get();
        
        // Load businesses relationship
        user.getBusinesses().size();

        // Check username uniqueness (if changed)
        if (!user.getUsername().equals(updateUserRequest.getUsername()) &&
            userRepository.existsByUsername(updateUserRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Check email uniqueness (if changed)
        if (!user.getEmail().equals(updateUserRequest.getEmail()) &&
            userRepository.existsByEmail(updateUserRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Validate that all businesses exist
        for (UUID businessId : updateUserRequest.getBusinessIds()) {
            Optional<Business> business = businessRepository.findById(businessId);
            if (business.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        // Update user fields
        user.setUsername(updateUserRequest.getUsername());
        user.setEmail(updateUserRequest.getEmail());
        user.setRole(updateUserRequest.getRole());
        user.setIsActive(updateUserRequest.getIsActive() != null ? updateUserRequest.getIsActive() : user.getIsActive());

        // Update password if provided
        if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }

        // Update businesses
        user.getBusinesses().clear();
        for (UUID businessId : updateUserRequest.getBusinessIds()) {
            Business business = businessRepository.findById(businessId).get();
            user.addBusiness(business);
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDto(savedUser));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Activate user", description = "Activate a user account")
    public ResponseEntity<UserDto> activateUser(@PathVariable UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        user.get().activate();
        User savedUser = userRepository.save(user.get());
        return ResponseEntity.ok(convertToDto(savedUser));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    public ResponseEntity<UserDto> deactivateUser(@PathVariable UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        user.get().deactivate();
        User savedUser = userRepository.save(user.get());
        return ResponseEntity.ok(convertToDto(savedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user account")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private UserDto convertToDto(User user) {
        try {
            java.util.Set<Business> businesses = user.getBusinesses();
            
            System.out.println("Converting user " + user.getUsername() + " to DTO");
            
            // Force initialization of businesses collection
            if (businesses != null) {
                int size = businesses.size(); // Trigger loading
                System.out.println("  Businesses collection size: " + size);
                
                // Access each business to ensure full loading
                for (Business business : businesses) {
                    UUID businessId = business.getId(); // Ensure business is loaded
                    String businessName = business.getName(); // Ensure name is loaded
                    System.out.println("    Business ID: " + businessId + ", Name: " + businessName);
                }
            } else {
                System.out.println("  Businesses collection is null");
            }
            
            List<UUID> businessIds = businesses != null && !businesses.isEmpty()
                    ? businesses.stream()
                            .map(Business::getId)
                            .collect(Collectors.toList())
                    : new java.util.ArrayList<>();

            List<String> businessNames = businesses != null && !businesses.isEmpty()
                    ? businesses.stream()
                            .map(Business::getName)
                            .filter(name -> name != null && !name.isEmpty()) // Filter out null/empty names
                            .collect(Collectors.toList())
                    : new java.util.ArrayList<>();

            System.out.println("  Extracted " + businessIds.size() + " businessIds and " + businessNames.size() + " businessNames");

            UserDto dto = new UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    businessIds,
                    user.getIsActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt());

            dto.setBusinessNames(businessNames);
            
            // Debug logging
            if (businessNames.isEmpty() && !businessIds.isEmpty()) {
                System.err.println("Warning: User " + user.getUsername() + " has businessIds but no businessNames");
            }
            
            System.out.println("  Final DTO - businessIds: " + dto.getBusinessIds() + ", businessNames: " + dto.getBusinessNames());
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error converting user to DTO: " + e.getMessage());
            e.printStackTrace();
            // Return DTO with empty businesses list
            UserDto dto = new UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    new java.util.ArrayList<>(),
                    user.getIsActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt());
            dto.setBusinessNames(new java.util.ArrayList<>());
            return dto;
        }
    }
}

