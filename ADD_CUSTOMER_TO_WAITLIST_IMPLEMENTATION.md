# Add Customer to Waitlist - Clean Architecture Implementation

## Overview
This document describes the complete implementation of the "Add Customer to Waitlist" use case following Clean Architecture principles. The implementation includes proper separation of concerns, validation, error handling, and comprehensive testing.

## Architecture Layers

### 1. Domain Layer
- **Entities**: `Business`, `Customer`, `WaitlistEntry`
- **Enums**: `WaitlistStatus`, `BusinessType`
- **Domain Services**: `SmsService` (interface)

### 2. Application Layer
- **Use Case**: `AddCustomerToWaitlistUseCase`
- **DTOs**: 
  - `AddCustomerToWaitlistRequest` (input)
  - `AddCustomerToWaitlistResponse` (output)

### 3. Infrastructure Layer
- **Repositories**: `BusinessRepository`, `CustomerRepository`, `WaitlistEntryRepository`
- **Services**: `TwilioSmsService`, `MockSmsService` (implementations of `SmsService`)

### 4. Presentation Layer
- **Controller**: `WaitlistController` (updated to use the use case)
- **DTOs**: `WaitlistEntryDto` (for other endpoints)

## Implementation Details

### Request DTO (`AddCustomerToWaitlistRequest`)
```java
public class AddCustomerToWaitlistRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be positive")
    private Integer partySize;
}
```

**Validation Rules:**
- Customer ID is required and must not be null
- Party size is required and must be a positive integer

### Response DTO (`AddCustomerToWaitlistResponse`)
```java
public class AddCustomerToWaitlistResponse {
    private UUID waitlistEntryId;
    private UUID businessId;
    private UUID customerId;
    private String businessName;
    private String customerName;
    private String customerPhone;
    private Integer partySize;
    private Integer position;
    private Integer estimatedWaitTime;
    private WaitlistStatus status;
    private LocalDateTime createdAt;
    private boolean smsNotificationSent;
}
```

### Use Case (`AddCustomerToWaitlistUseCase`)
The use case orchestrates the business logic:

1. **Validation Steps:**
   - Validates business exists and is active
   - Validates customer exists
   - Checks customer is not already on waitlist

2. **Business Logic:**
   - Calculates next position in waitlist
   - Creates waitlist entry with estimated wait time
   - Sends SMS notification to customer
   - Returns comprehensive response

3. **Error Handling:**
   - `IllegalArgumentException` for validation failures
   - `IllegalStateException` for business rule violations
   - Graceful SMS failure handling

### Controller Integration
The `WaitlistController` has been updated to use the new use case:

```java
@PostMapping
public ResponseEntity<AddCustomerToWaitlistResponse> addToWaitlist(
        @Valid @RequestBody AddCustomerToWaitlistRequest request, 
        Authentication authentication) {
    try {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        UUID businessId = userPrincipal.getBusinessId();
        
        AddCustomerToWaitlistResponse response = addCustomerToWaitlistUseCase.execute(request, businessId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

## Testing

### 1. Use Case Tests (`AddCustomerToWaitlistUseCaseTest`)
Comprehensive unit tests covering:
- ✅ Successful customer addition to waitlist
- ✅ Business validation (not found, inactive)
- ✅ Customer validation (not found)
- ✅ Duplicate customer prevention
- ✅ Position calculation (empty waitlist, existing entries)
- ✅ SMS notification handling (success, failure, exception)
- ✅ Wait time calculation accuracy

### 2. Validation Tests (`AddCustomerToWaitlistRequestTest`)
Bean validation tests covering:
- ✅ Valid request data
- ✅ Null customer ID validation
- ✅ Null party size validation
- ✅ Zero/negative party size validation
- ✅ Multiple validation failures
- ✅ Edge cases (minimum/maximum values)

### 3. Controller Tests (`WaitlistControllerTest`)
Integration tests covering:
- ✅ Successful API calls
- ✅ Request validation failures
- ✅ Business logic exceptions
- ✅ Error response mapping

## API Endpoint

### POST `/api/waitlist`
**Request:**
```json
{
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "partySize": 4
}
```

**Success Response (201 Created):**
```json
{
  "waitlistEntryId": "456e7890-e89b-12d3-a456-426614174001",
  "businessId": "789e0123-e89b-12d3-a456-426614174002",
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "businessName": "Test Restaurant",
  "customerName": "John Doe",
  "customerPhone": "+1987654321",
  "partySize": 4,
  "position": 3,
  "estimatedWaitTime": 180,
  "status": "WAITING",
  "createdAt": "2024-01-15T10:30:00",
  "smsNotificationSent": true
}
```

**Error Responses:**
- `400 Bad Request`: Validation failures or business not found
- `409 Conflict`: Customer already on waitlist
- `500 Internal Server Error`: Unexpected errors

## Key Features

### 1. Clean Architecture Compliance
- Clear separation of concerns across layers
- Domain logic isolated from infrastructure
- Use case orchestration without framework dependencies

### 2. Comprehensive Validation
- Input validation with Bean Validation annotations
- Business rule validation in use case
- Proper error handling and HTTP status codes

### 3. SMS Integration
- Abstracted SMS service interface
- Graceful failure handling
- Notification status tracking

### 4. Position Management
- Automatic position calculation
- Handles empty waitlists correctly
- Maintains proper ordering

### 5. Wait Time Estimation
- Business-specific average service time
- Position-based calculation
- Configurable per business

### 6. Security
- JWT authentication required
- Business-scoped access control
- User principal integration

## Test Results
```
✅ AddCustomerToWaitlistUseCaseTest: 9 tests passed
✅ AddCustomerToWaitlistRequestTest: 8 tests passed
✅ WaitlistControllerTest: 4 tests passed
```

## Benefits of This Implementation

1. **Maintainability**: Clear separation of concerns makes code easy to understand and modify
2. **Testability**: Each layer can be tested independently with proper mocking
3. **Scalability**: Use case pattern allows for easy addition of new business rules
4. **Reliability**: Comprehensive validation and error handling prevent invalid states
5. **Flexibility**: SMS service abstraction allows for easy provider changes
6. **Security**: Proper authentication and authorization throughout

This implementation serves as a template for implementing other use cases in the system following the same Clean Architecture principles.

