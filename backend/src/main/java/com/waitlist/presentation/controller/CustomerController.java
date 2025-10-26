package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Customer;
import com.waitlist.infrastructure.repository.CustomerRepository;
import com.waitlist.presentation.dto.CustomerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Get all customers", description = "Retrieve all customers")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDto> customerDtos = customers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDtos);
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
    @Operation(summary = "Create customer", description = "Create a new customer")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        // Check if customer with phone already exists
        if (customerRepository.existsByPhone(customerDto.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Customer customer = convertToEntity(customerDto);
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedCustomer));
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
    @Operation(summary = "Find or create customer", description = "Find existing customer by phone or create new one")
    public ResponseEntity<CustomerDto> findOrCreateCustomer(@Valid @RequestBody CustomerDto customerDto) {
        Optional<Customer> existingCustomer = customerRepository.findByPhone(customerDto.getPhone());

        if (existingCustomer.isPresent()) {
            // Update existing customer if new information is provided
            Customer customer = existingCustomer.get();
            if (customerDto.getName() != null && !customerDto.getName().trim().isEmpty()) {
                customer.setName(customerDto.getName());
            }
            if (customerDto.getEmail() != null && !customerDto.getEmail().trim().isEmpty()) {
                customer.setEmail(customerDto.getEmail());
            }
            Customer savedCustomer = customerRepository.save(customer);
            return ResponseEntity.ok(convertToDto(savedCustomer));
        } else {
            // Create new customer
            Customer customer = convertToEntity(customerDto);
            Customer savedCustomer = customerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedCustomer));
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

