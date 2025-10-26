# Waitlist & Reservations

A full-stack application for restaurants and service businesses to manage waitlists and table reservations. Customers receive SMS updates when their table is ready or can check wait times via public links.

## Architecture

- **Backend**: Java 17 + Spring Boot 3 with Clean Architecture
- **Frontend**: React.js with TypeScript, Vite, and Tailwind CSS
- **Database**: PostgreSQL
- **Messaging**: Twilio SMS (abstracted behind interface)
- **Security**: Spring Security with JWT authentication

## Project Structure

```
waitlist/
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/waitlist/
│   │       ├── domain/      # Domain entities and business logic
│   │       ├── application/ # Use cases and services
│   │       ├── infrastructure/ # External concerns (DB, SMS, etc.)
│   │       └── presentation/ # Controllers and DTOs
│   ├── src/test/java/       # Test classes
│   └── src/main/resources/  # Configuration files
├── frontend/                # React application
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── pages/          # Page components
│   │   ├── services/       # API services
│   │   └── types/          # TypeScript types
│   └── public/             # Static assets
└── docker-compose.yml      # Docker orchestration
```

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Docker and Docker Compose
- PostgreSQL (or use Docker)

### Backend Setup

1. Navigate to backend directory:
   ```bash
   cd backend
   ```

2. Copy environment variables:
   ```bash
   cp env.example .env
   ```

3. Update `.env` with your configuration

4. Run with Docker:
   ```bash
   docker-compose up -d
   ```

5. Or run locally:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup

1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start development server:
   ```bash
   npm run dev
   ```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new business account
- `POST /api/auth/login` - Authenticate and return JWT
- `GET /api/auth/profile` - Get business profile

### Business Management
- `GET /api/business` - List businesses
- `GET /api/business/{id}` - Get business details
- `PUT /api/business/{id}` - Update business info

### Customer Management
- `POST /api/customers` - Create a customer
- `GET /api/customers/{id}` - Get customer info

### Waitlist Management
- `POST /api/waitlist` - Add customer to waitlist
- `GET /api/waitlist` - List all waitlist entries for authenticated business
- `PATCH /api/waitlist/{id}/status` - Update status (waiting, called, seated, canceled)
- `DELETE /api/waitlist/{id}` - Remove from waitlist

### Reservation Management
- `POST /api/reservations` - Create a reservation
- `GET /api/reservations` - List all reservations
- `GET /api/reservations/{id}` - Get reservation details
- `PATCH /api/reservations/{id}` - Update reservation (status or time)
- `DELETE /api/reservations/{id}` - Cancel reservation

### Notifications
- `POST /api/notifications/sms` - Send SMS to a customer using Twilio
- `GET /api/notifications/status/{id}` - Check SMS delivery status

### Public Endpoints (No Auth)
- `GET /public/waitlist/{businessId}` - Public endpoint showing estimated wait time

## API Documentation

Once the backend is running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## Features

- **Business Registration**: Register new business accounts with user authentication
- **User Management**: Link users to businesses with role-based access
- **Waitlist Management**: Queue customers with position tracking and SMS notifications
- **Reservation System**: Handle table reservations with time slots
- **SMS Notifications**: Send updates to customers via Twilio (with mock for development)
- **Public Waitlist Info**: Customers can check wait times via SMS links
- **JWT Authentication**: Secure API endpoints with business-scoped access
- **Clean Architecture**: Maintainable and testable code structure

## Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

The project includes comprehensive unit tests, including a complete test suite for the "Add Customer to Waitlist" use case demonstrating:
- Successful waitlist addition
- Business validation
- Customer validation
- Duplicate entry prevention
- Position calculation
- SMS notification handling
- Error scenarios

### Frontend Tests
```bash
cd frontend
npm test
```

## Environment Variables

### Backend (.env)
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/waitlist_db
SPRING_DATASOURCE_USERNAME=waitlist_user
SPRING_DATASOURCE_PASSWORD=waitlist_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# Twilio Configuration (optional - uses mock if not provided)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=your-twilio-phone-number

# Application Configuration
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# SMS Configuration
SMS_MOCK_ENABLED=true
```

### Frontend (.env)
```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api

# Application Configuration
VITE_APP_NAME=Waitlist & Reservations
VITE_APP_VERSION=1.0.0
```

## Demo Credentials

After running the application, you can register a new business account or use the demo data. The system supports:

- **Business Registration**: Create new business accounts through the registration endpoint
- **User Roles**: BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_STAFF
- **Business-scoped Access**: Users can only access data for their associated business

## SMS Integration

The system includes a complete SMS notification system:

- **Twilio Integration**: Real SMS sending via Twilio SDK
- **Mock Service**: Development-friendly mock implementation
- **Message Templates**: Predefined templates for waitlist and reservation notifications
- **Status Tracking**: SMS delivery status monitoring (mock implementation)

## Production Deployment

For production deployment:

1. Update environment variables with production values
2. Set up proper Twilio credentials
3. Configure production database
4. Update CORS settings for production domains
5. Use proper JWT secrets
6. Set up SSL/TLS certificates

## Contributing

This is a production-ready scaffold that follows Clean Architecture principles and includes comprehensive testing. The codebase is structured for easy extension and maintenance.
