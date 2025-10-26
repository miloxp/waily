# Postman Test Setup Guide

## 🚀 Quick Start

### 1. Import Collection and Environment

1. **Open Postman**
2. **Import Collection:**
   - Click "Import" button
   - Select `Waitlist_API_Postman_Collection.json`
   - Click "Import"

3. **Import Environment:**
   - Click "Import" button
   - Select `Waitlist_API_Environment.json`
   - Click "Import"

4. **Select Environment:**
   - In the top-right corner, select "Waitlist API Environment"

### 2. Start the Backend Server

Make sure the backend is running on `http://localhost:8080`:

```bash
cd /Users/milo/Work/waitlist/backend
java -jar target/waitlist-backend-0.0.1-SNAPSHOT.jar --server.port=8080 --spring.datasource.url=jdbc:postgresql://localhost:5432/waitlist --spring.datasource.username=waitlist --spring.datasource.password=waitlist --spring.jpa.hibernate.ddl-auto=create-drop
```

### 3. Run the Test Sequence

**Important:** Run the tests in this specific order for best results:

1. **Authentication** → Login
2. **Business Management** → Create Business
3. **Customer Management** → Create Customer
4. **Waitlist Management** → Add Customer to Waitlist (Success)
5. **Waitlist Management** → Test other waitlist scenarios
6. **Reservation Management** → Create Reservation
7. **SMS Notifications** → Send SMS
8. **Public Endpoints** → Test public access
9. **Error Testing** → Test error scenarios

## 📋 Test Categories

### 🔐 Authentication Tests
- **Login** - Get JWT token for authenticated requests
- Tests JWT token generation and storage

### 🏢 Business Management Tests
- **Create Business** - Create a new business
- **Get All Businesses** - Retrieve all businesses
- **Get Business by ID** - Retrieve specific business

### 👥 Customer Management Tests
- **Create Customer** - Create a new customer
- **Get All Customers** - Retrieve all customers

### ⏰ Waitlist Management Tests (Main Use Case)
- **Add Customer to Waitlist (Success)** - Happy path scenario
- **Add Customer to Waitlist (Conflict)** - Customer already on waitlist
- **Add Customer to Waitlist (Invalid Customer ID)** - Customer not found
- **Add Customer to Waitlist (Invalid Party Size)** - Validation error
- **Get All Waitlist Entries** - List all waitlist entries
- **Get Waitlist Entry by ID** - Get specific waitlist entry
- **Update Waitlist Entry Status** - Change status (e.g., to SEATED)
- **Notify Customer** - Send notification to customer
- **Get Waitlist Statistics** - Get business waitlist metrics

### 📅 Reservation Management Tests
- **Create Reservation** - Create a new reservation
- **Get All Reservations** - Retrieve all reservations

### 📱 SMS Notification Tests
- **Send SMS** - Send SMS notification
- **Check SMS Status** - Check delivery status

### 🌐 Public Endpoints Tests
- **Get Business Info (Public)** - Public business information
- **Get Wait Time (Public)** - Public wait time information

### ❌ Error Testing
- **Test Unauthorized Access** - Test without authentication
- **Test Invalid Business ID** - Test with non-existent ID
- **Test Malformed JSON** - Test with invalid JSON

## 🔧 Environment Variables

The collection uses these environment variables:

- `baseUrl`: `http://localhost:8080/api`
- `jwtToken`: Automatically set after login
- `businessId`: Automatically set after creating business
- `customerId`: Automatically set after creating customer
- `waitlistEntryId`: Automatically set after adding to waitlist

## ✅ Test Scripts

Each request includes automated test scripts that:

1. **Verify HTTP status codes**
2. **Validate response structure**
3. **Extract and store IDs for subsequent requests**
4. **Check error messages for error scenarios**

## 🎯 Key Test Scenarios

### Main Use Case: Add Customer to Waitlist

1. **Success Flow:**
   - Login → Create Business → Create Customer → Add to Waitlist
   - Verifies: 201 status, waitlist entry creation, position assignment

2. **Error Scenarios:**
   - **Conflict:** Try to add same customer twice
   - **Not Found:** Use invalid customer ID
   - **Validation:** Use invalid party size (0 or negative)

3. **Status Management:**
   - Update waitlist entry status
   - Send notifications
   - Get statistics

### Authentication Flow

1. **Login** with valid credentials
2. **JWT token** automatically stored in environment
3. **Subsequent requests** use stored token
4. **Error testing** verifies 401/403 responses without auth

### Data Flow

1. **Business** created first (required for waitlist)
2. **Customer** created (required for waitlist)
3. **Waitlist entry** created (main use case)
4. **Reservations** can be created independently
5. **SMS notifications** can be sent to any customer

## 🚨 Common Issues

### Backend Not Running
- **Error:** Connection refused
- **Solution:** Start the backend server

### Database Connection Issues
- **Error:** 500 Internal Server Error
- **Solution:** Ensure PostgreSQL is running and accessible

### Authentication Issues
- **Error:** 401 Unauthorized
- **Solution:** Run the Login request first to get JWT token

### Missing Data
- **Error:** 404 Not Found
- **Solution:** Run requests in order (Business → Customer → Waitlist)

## 📊 Expected Results

### Successful Test Run
- All requests return appropriate status codes
- JWT token is automatically stored
- IDs are automatically extracted and stored
- Error scenarios return expected error codes

### Sample Response (Add Customer to Waitlist)
```json
{
  "waitlistEntryId": "123e4567-e89b-12d3-a456-426614174000",
  "businessId": "456e7890-e89b-12d3-a456-426614174001",
  "customerId": "789e0123-e89b-12d3-a456-426614174002",
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

## 🔄 Running Tests

### Individual Tests
- Click on any request and click "Send"
- Check the "Test Results" tab for automated test results

### Collection Runner
1. Click on the collection name
2. Click "Run" button
3. Select requests to run
4. Click "Run Waitlist & Reservations API"

### Newman (Command Line)
```bash
newman run Waitlist_API_Postman_Collection.json -e Waitlist_API_Environment.json
```

## 📈 Performance Testing

The collection includes tests for:
- **Load testing** with multiple concurrent requests
- **Error handling** with invalid data
- **Security testing** with authentication bypass attempts

## 🎉 Success Criteria

A successful test run should show:
- ✅ All authentication tests pass
- ✅ All CRUD operations work correctly
- ✅ Main use case (Add Customer to Waitlist) works end-to-end
- ✅ Error scenarios return appropriate error codes
- ✅ Public endpoints work without authentication
- ✅ SMS notifications are sent (mocked in development)

This comprehensive test suite validates the entire "Add Customer to Waitlist" use case and the broader API functionality!

