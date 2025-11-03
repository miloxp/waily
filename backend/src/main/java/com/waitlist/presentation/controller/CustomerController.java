package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Business;
import com.waitlist.domain.entity.Customer;
import com.waitlist.domain.entity.User;
import com.waitlist.domain.entity.UserRole;
import com.waitlist.infrastructure.repository.BusinessRepository;
import com.waitlist.infrastructure.repository.CustomerRepository;
import com.waitlist.infrastructure.security.CustomUserDetailsService;
import com.waitlist.presentation.dto.CustomerDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Operation(summary = "Get all customers", description = "Retrieve all customers (Platform Admin sees all, business users see their business's customers)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CustomerDto>> getAllCustomers(Authentication authentication) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();
            UserRole currentUserRole = currentUser.getRole();

            List<Customer> customers;
            
            if (currentUserRole == UserRole.PLATFORM_ADMIN) {
                // PLATFORM_ADMIN sees all customers
                customers = customerRepository.findAllWithBusinesses();
            } else {
                // Business users see only customers associated with their businesses
                java.util.Set<Business> userBusinesses = currentUser.getBusinesses();
                
                if (userBusinesses.isEmpty()) {
                    customers = new java.util.ArrayList<>();
                } else {
                    java.util.List<UUID> businessIds = userBusinesses.stream()
                            .map(Business::getId)
                            .collect(Collectors.toList());
                    
                    customers = customerRepository.findCustomersByBusinessIds(businessIds);
                }
            }
            
            List<CustomerDto> customerDtos = customers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customerDtos);
        } catch (Exception e) {
            System.err.println("Error in getAllCustomers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by ID")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable UUID id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return ResponseEntity.ok(convertToDto(customer.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get customer by phone", description = "Retrieve a customer by phone number")
    public ResponseEntity<CustomerDto> getCustomerByPhone(@PathVariable String phone) {
        Optional<Customer> customer = customerRepository.findByPhone(phone);
        if (customer.isPresent()) {
            return ResponseEntity.ok(convertToDto(customer.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by name, phone, or email")
    public ResponseEntity<List<CustomerDto>> searchCustomers(@RequestParam String searchTerm) {
        List<Customer> customers = customerRepository.searchCustomers(searchTerm);
        List<CustomerDto> customerDtos = customers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDtos);
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Operation(summary = "Create customer", description = "Create a new customer and associate with user's business")
    @Transactional
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CustomerDto customerDto,
            Authentication authentication) {
        try {
            // Get current user's business
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();
            java.util.Set<Business> userBusinesses = currentUser.getBusinesses();

            if (userBusinesses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Check if customer with phone already exists
            Optional<Customer> existingCustomerOpt = customerRepository.findByPhone(customerDto.getPhone());
            Customer customer;
            
            if (existingCustomerOpt.isPresent()) {
                // Customer exists, just add business association
                customer = existingCustomerOpt.get();
                
                // Add all user's businesses to the customer if not already associated
                for (Business business : userBusinesses) {
                    if (!customer.hasBusiness(business.getId())) {
                        customer.addBusiness(business);
                    }
                }
            } else {
                // Create new customer
                customer = convertToEntity(customerDto);
                
                // Associate customer with all user's businesses
                for (Business business : userBusinesses) {
                    customer.addBusiness(business);
                }
            }

            Customer savedCustomer = customerRepository.save(customer);
            customerRepository.flush(); // Ensure relationships are persisted
            
            // Reload customer with businesses
            Customer customerWithBusinesses = customerRepository.findAllWithBusinesses().stream()
                    .filter(c -> c.getId().equals(savedCustomer.getId()))
                    .findFirst()
                    .orElse(savedCustomer);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(customerWithBusinesses));
        } catch (Exception e) {
            System.err.println("Error in createCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update an existing customer")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable UUID id,
            @Valid @RequestBody CustomerDto customerDto) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Customer customer = existingCustomer.get();
        customer.updateContactInfo(customerDto.getName(), customerDto.getEmail());

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(convertToDto(savedCustomer));
    }

    @PostMapping("/find-or-create")
    @PreAuthorize("hasRole('PLATFORM_ADMIN') or hasRole('BUSINESS_OWNER') or hasRole('BUSINESS_STAFF')")
    @Operation(summary = "Find or create customer", description = "Find existing customer by phone or create new one and associate with user's business")
    @Transactional
    public ResponseEntity<CustomerDto> findOrCreateCustomer(
            @Valid @RequestBody CustomerDto customerDto,
            Authentication authentication) {
        try {
            // Get current user's business
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();
            java.util.Set<Business> userBusinesses = currentUser.getBusinesses();

            if (userBusinesses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Optional<Customer> existingCustomerOpt = customerRepository.findByPhone(customerDto.getPhone());

            Customer customer;
            if (existingCustomerOpt.isPresent()) {
                // Update existing customer if new information is provided
                customer = existingCustomerOpt.get();
                if (customerDto.getName() != null && !customerDto.getName().trim().isEmpty()) {
                    customer.setName(customerDto.getName());
                }
                if (customerDto.getEmail() != null && !customerDto.getEmail().trim().isEmpty()) {
                    customer.setEmail(customerDto.getEmail());
                }
                
                // Add business association if not already present
                for (Business business : userBusinesses) {
                    if (!customer.hasBusiness(business.getId())) {
                        customer.addBusiness(business);
                    }
                }
            } else {
                // Create new customer
                customer = convertToEntity(customerDto);
                
                // Associate customer with all user's businesses
                for (Business business : userBusinesses) {
                    customer.addBusiness(business);
                }
            }
            
            Customer savedCustomer = customerRepository.save(customer);
            customerRepository.flush();
            
            // Reload customer with businesses
            Customer customerWithBusinesses = customerRepository.findAllWithBusinesses().stream()
                    .filter(c -> c.getId().equals(savedCustomer.getId()))
                    .findFirst()
                    .orElse(savedCustomer);
            
            return ResponseEntity.ok(convertToDto(customerWithBusinesses));
        } catch (Exception e) {
            System.err.println("Error in findOrCreateCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private CustomerDto convertToDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getPhone(),
                customer.getName(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }

    private Customer convertToEntity(CustomerDto customerDto) {
        return new Customer(
                customerDto.getPhone(),
                customerDto.getName(),
                customerDto.getEmail());
    }
}

