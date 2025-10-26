package com.waitlist.presentation.controller;

import com.waitlist.domain.entity.Customer;
import com.waitlist.domain.service.SmsService;
import com.waitlist.infrastructure.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/sms")
    @Operation(summary = "Send SMS to customer", description = "Send SMS to a customer using Twilio")
    public ResponseEntity<Map<String, Object>> sendSms(@Valid @RequestBody SmsRequest smsRequest,
            Authentication authentication) {
        try {
            // Find customer
            Optional<Customer> customer = customerRepository.findById(smsRequest.getCustomerId());
            if (customer.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Customer customerEntity = customer.get();

            // Send SMS
            boolean success = smsService.sendSms(customerEntity.getPhone(), smsRequest.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("customerId", customerEntity.getId());
            response.put("phoneNumber", customerEntity.getPhone());
            response.put("message", smsRequest.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/status/{id}")
    @Operation(summary = "Check SMS delivery status", description = "Check SMS delivery status (mock implementation)")
    public ResponseEntity<Map<String, Object>> checkSmsStatus(@PathVariable String id) {
        // This is a mock implementation since Twilio webhook integration would be more
        // complex
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", id);
        response.put("status", "delivered"); // Mock status
        response.put("timestamp", System.currentTimeMillis());
        response.put("note",
                "This is a mock implementation. In production, integrate with Twilio webhooks for real status tracking.");

        return ResponseEntity.ok(response);
    }

    // DTO for SMS request
    public static class SmsRequest {
        private UUID customerId;
        private String message;

        public SmsRequest() {
        }

        public SmsRequest(UUID customerId, String message) {
            this.customerId = customerId;
            this.message = message;
        }

        public UUID getCustomerId() {
            return customerId;
        }

        public void setCustomerId(UUID customerId) {
            this.customerId = customerId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

