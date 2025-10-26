# Postman Test Cases for Waitlist & Reservations API

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints (except public ones) require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 1. Authentication Endpoints

### 1.1 Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "username": "admin@waitlist.com",
  "password": "admin123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

**Test Cases:**
- ✅ Valid credentials
- ❌ Invalid username
- ❌ Invalid password
- ❌ Missing credentials

---

## 2. Business Management

### 2.1 Create Business
**POST** `/businesses`

**Request Body:**
```json
{
  "name": "Test Restaurant",
  "type": "RESTAURANT",
  "address": "123 Main St, City, State",
  "phone": "+1234567890",
  "email": "test@restaurant.com",
  "capacity": 50,
  "averageServiceTime": 60
}
```

**Expected Response (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Test Restaurant",
  "type": "RESTAURANT",
  "address": "123 Main St, City, State",
  "phone": "+1234567890",
  "email": "test@restaurant.com",
  "capacity": 50,
  "averageServiceTime": 60,
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

### 2.2 Get All Businesses
**GET** `/businesses`

**Expected Response (200 OK):**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Test Restaurant",
    "type": "RESTAURANT",
    "address": "123 Main St, City, State",
    "phone": "+1234567890",
    "email": "test@restaurant.com",
    "capacity": 50,
    "averageServiceTime": 60,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### 2.3 Get Business by ID
**GET** `/businesses/{id}`

**Expected Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Test Restaurant",
  "type": "RESTAURANT",
  "address": "123 Main St, City, State",
  "phone": "+1234567890",
  "email": "test@restaurant.com",
  "capacity": 50,
  "averageServiceTime": 60,
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

## 3. Customer Management

### 3.1 Create Customer
**POST** `/customers`

**Request Body:**
```json
{
  "phone": "+1987654321",
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Expected Response (201 Created):**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "phone": "+1987654321",
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 3.2 Get All Customers
**GET** `/customers`

**Expected Response (200 OK):**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "phone": "+1987654321",
    "name": "John Doe",
    "email": "john@example.com",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

---

## 4. Waitlist Management (Main Use Case)

### 4.1 Add Customer to Waitlist
**POST** `/waitlist`

**Request Body:**
```json
{
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "partySize": 4
}
```

**Expected Response (201 Created):**
```json
{
  "waitlistEntryId": "789e0123-e89b-12d3-a456-426614174002",
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "businessName": "Test Restaurant",
  "customerName": "John Doe",
  "customerPhone": "+1987654321",
  "partySize": 4,
  "position": 1,
  "estimatedWaitTime": 60,
  "status": "WAITING",
  "createdAt": "2024-01-15T10:30:00",
  "smsNotificationSent": true
}
```

**Test Cases:**
- ✅ Valid request with existing customer
- ❌ Invalid customer ID (404 Not Found)
- ❌ Customer already on waitlist (409 Conflict)
- ❌ Invalid party size (400 Bad Request)
- ❌ Missing customer ID (400 Bad Request)
- ❌ Zero or negative party size (400 Bad Request)

### 4.2 Get All Waitlist Entries
**GET** `/waitlist`

**Expected Response (200 OK):**
```json
[
  {
    "id": "789e0123-e89b-12d3-a456-426614174002",
    "businessId": "123e4567-e89b-12d3-a456-426614174000",
    "customerId": "456e7890-e89b-12d3-a456-426614174001",
    "partySize": 4,
    "position": 1,
    "estimatedWaitTime": 60,
    "status": "WAITING",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### 4.3 Get Waitlist Entry by ID
**GET** `/waitlist/{id}`

**Expected Response (200 OK):**
```json
{
  "id": "789e0123-e89b-12d3-a456-426614174002",
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "partySize": 4,
  "position": 1,
  "estimatedWaitTime": 60,
  "status": "WAITING",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 4.4 Update Waitlist Entry Status
**PUT** `/waitlist/{id}/status`

**Request Body:**
```json
{
  "status": "SEATED"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "789e0123-e89b-12d3-a456-426614174002",
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "partySize": 4,
  "position": 1,
  "estimatedWaitTime": 60,
  "status": "SEATED",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 4.5 Notify Customer
**PUT** `/waitlist/{id}/notify`

**Expected Response (200 OK):**
```json
{
  "id": "789e0123-e89b-12d3-a456-426614174002",
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "partySize": 4,
  "position": 1,
  "estimatedWaitTime": 60,
  "status": "NOTIFIED",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 4.6 Get Waitlist Statistics
**GET** `/waitlist/stats/{businessId}`

**Expected Response (200 OK):**
```json
{
  "waitingCount": 5,
  "activeCount": 3
}
```

---

## 5. Reservation Management

### 5.1 Create Reservation
**POST** `/reservations`

**Request Body:**
```json
{
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "partySize": 4,
  "reservationTime": "2024-01-15T19:00:00",
  "specialRequests": "Window table please"
}
```

**Expected Response (201 Created):**
```json
{
  "id": "abc12345-e89b-12d3-a456-426614174003",
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "partySize": 4,
  "reservationTime": "2024-01-15T19:00:00",
  "specialRequests": "Window table please",
  "status": "CONFIRMED",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 5.2 Get All Reservations
**GET** `/reservations`

**Expected Response (200 OK):**
```json
[
  {
    "id": "abc12345-e89b-12d3-a456-426614174003",
    "businessId": "123e4567-e89b-12d3-a456-426614174000",
    "customerId": "456e7890-e89b-12d3-a456-426614174001",
    "partySize": 4,
    "reservationTime": "2024-01-15T19:00:00",
    "specialRequests": "Window table please",
    "status": "CONFIRMED",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

---

## 6. SMS Notifications

### 6.1 Send SMS
**POST** `/notifications/sms`

**Request Body:**
```json
{
  "customerId": "456e7890-e89b-12d3-a456-426614174001",
  "message": "Your table is ready! Please come to the host stand."
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "messageId": "SM1234567890abcdef",
  "message": "Your table is ready! Please come to the host stand.",
  "timestamp": 1705324200000
}
```

### 6.2 Check SMS Status
**GET** `/notifications/status/{messageId}`

**Expected Response (200 OK):**
```json
{
  "messageId": "SM1234567890abcdef",
  "status": "delivered",
  "timestamp": 1705324200000,
  "note": "This is a mock implementation. In production, integrate with Twilio webhooks for real status tracking."
}
```

---

## 7. Public Endpoints (No Authentication Required)

### 7.1 Get Business Info
**GET** `/public/businesses/{id}`

**Expected Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Test Restaurant",
  "type": "RESTAURANT",
  "address": "123 Main St, City, State",
  "phone": "+1234567890",
  "email": "test@restaurant.com",
  "capacity": 50,
  "averageServiceTime": 60,
  "isActive": true
}
```

### 7.2 Get Wait Time
**GET** `/public/businesses/{id}/wait-time`

**Expected Response (200 OK):**
```json
{
  "businessId": "123e4567-e89b-12d3-a456-426614174000",
  "currentWaitTime": 45,
  "queueLength": 8,
  "lastUpdated": "2024-01-15T10:30:00"
}
```

---

## 8. Error Responses

### 8.1 Validation Errors (400 Bad Request)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/waitlist",
  "details": [
    {
      "field": "customerId",
      "message": "Customer ID is required"
    },
    {
      "field": "partySize",
      "message": "Party size must be positive"
    }
  ]
}
```

### 8.2 Not Found (404 Not Found)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found",
  "path": "/api/waitlist"
}
```

### 8.3 Conflict (409 Conflict)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Customer is already on the waitlist",
  "path": "/api/waitlist"
}
```

### 8.4 Unauthorized (401 Unauthorized)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid",
  "path": "/api/waitlist"
}
```

---

## 9. Postman Collection Setup

### 9.1 Environment Variables
Create a Postman environment with these variables:
```
base_url: http://localhost:8080/api
jwt_token: (will be set after login)
business_id: (will be set after creating business)
customer_id: (will be set after creating customer)
waitlist_entry_id: (will be set after adding to waitlist)
```

### 9.2 Pre-request Scripts
Add this to your login request to automatically set the JWT token:
```javascript
pm.test("Login successful", function () {
    var jsonData = pm.response.json();
    pm.environment.set("jwt_token", jsonData.token);
});
```

### 9.3 Test Scripts
Add this to your requests to verify responses:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
});
```

---

## 10. Complete Test Flow

### Step 1: Authentication
1. **Login** → Get JWT token
2. Set token in Authorization header for all subsequent requests

### Step 2: Setup Data
1. **Create Business** → Get business ID
2. **Create Customer** → Get customer ID

### Step 3: Test Main Use Case
1. **Add Customer to Waitlist** → Test success case
2. **Add Same Customer Again** → Test conflict case
3. **Add Customer with Invalid Data** → Test validation errors
4. **Get Waitlist Entries** → Verify entry was created
5. **Update Waitlist Status** → Test status changes
6. **Notify Customer** → Test notification

### Step 4: Test Edge Cases
1. **Invalid Customer ID** → Test 404 error
2. **Invalid Business ID** → Test 404 error
3. **Missing Authentication** → Test 401 error
4. **Invalid Party Size** → Test validation errors

### Step 5: Test Additional Features
1. **Create Reservation** → Test reservation system
2. **Send SMS** → Test notification system
3. **Get Statistics** → Test reporting
4. **Public Endpoints** → Test public access

---

## 11. Performance Testing

### 11.1 Load Testing
- Test with multiple concurrent requests
- Test with large party sizes
- Test with many customers on waitlist

### 11.2 Stress Testing
- Test with invalid data repeatedly
- Test with missing authentication
- Test with malformed JSON

---

## 12. Security Testing

### 12.1 Authentication
- Test with expired tokens
- Test with invalid tokens
- Test with missing tokens

### 12.2 Authorization
- Test cross-business access
- Test admin-only endpoints
- Test business-scoped data access

---

This comprehensive test suite covers all aspects of the "Add Customer to Waitlist" use case and the broader API functionality. Use these test cases to verify that your implementation works correctly and handles all edge cases properly.

