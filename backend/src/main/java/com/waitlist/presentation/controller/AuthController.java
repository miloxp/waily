package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.BusinessType;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.UserRole;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.UserRepository;
import com.waitlist.infrastructure.security.CustomUserDetailsService;
import com.waitlist.infrastructure.security.JwtUtil;
import com.waitlist.presentation.dto.BusinessDto;
import com.waitlist.presentation.dto.LoginRequest;
import com.waitlist.presentation.dto.LoginResponse;
import com.waitlist.presentation.dto.RegisterRequest;
import com.waitlist.presentation.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", userDetails.getAuthorities());
            String tokenWithClaims = jwtUtil.generateToken(userDetails.getUsername(), claims);

            LoginResponse response = new LoginResponse(
                    tokenWithClaims,
                    "Bearer",
                    userDetails.getUsername(),
                    userDetails.getAuthorities().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register business", description = "Register a new business account")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username or email already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Create business
            Business business = new Business(
                    registerRequest.getBusinessName(),
                    BusinessType.valueOf(registerRequest.getBusinessType()),
                    registerRequest.getBusinessAddress(),
                    registerRequest.getBusinessPhone(),
                    registerRequest.getBusinessEmail(),
                    registerRequest.getCapacity() != null ? registerRequest.getCapacity() : 50,
                    registerRequest.getAverageServiceTime() != null ? registerRequest.getAverageServiceTime() : 60);

            Business savedBusiness = businessRepository.save(business);

            // Create user
            User user = new User(
                    registerRequest.getUsername(),
                    passwordEncoder.encode(registerRequest.getPassword()),
                    registerRequest.getEmail(),
                    savedBusiness,
                    UserRole.BUSINESS_OWNER);

            User savedUser = userRepository.save(user);

            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", savedUser.getId());
            claims.put("businessId", savedBusiness.getId());
            claims.put("role", savedUser.getRole().name());

            String token = jwtUtil.generateToken(savedUser.getUsername(), claims);

            LoginResponse response = new LoginResponse(
                    token,
                    "Bearer",
                    savedUser.getUsername(),
                    savedUser.getRole().name());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get the authenticated user's profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        try {
            logger.debug("Getting profile for authentication: {}", authentication);

            if (authentication == null || authentication.getPrincipal() == null) {
                logger.error("Authentication or principal is null");
                return ResponseEntity.badRequest().build();
            }

            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();

            User user = userPrincipal.getUser();
            logger.debug("Found user: {}", user.getUsername());

            UserProfileDto profile = new UserProfileDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    convertBusinessToDto(user.getBusiness()),
                    user.getIsActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt());

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            logger.error("Error getting user profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    private BusinessDto convertBusinessToDto(Business business) {
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

    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            boolean isValid = jwtUtil.validateToken(jwt);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (isValid) {
                String username = jwtUtil.extractUsername(jwt);
                response.put("username", username);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Invalid token");
            return ResponseEntity.ok(response);
        }
    }
}
