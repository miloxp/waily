# PostgreSQL Setup Guide

This guide will help you set up PostgreSQL locally for the Waitlist & Reservations application.

## Prerequisites

- macOS, Linux, or Windows
- PostgreSQL 12+ installed

## Installation

### macOS (using Homebrew)

```bash
# Install PostgreSQL
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Create a symbolic link (optional)
brew link postgresql@15
```

### Ubuntu/Debian

```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### Windows

1. Download PostgreSQL from [postgresql.org](https://www.postgresql.org/download/windows/)
2. Run the installer and follow the setup wizard
3. Remember the password you set for the `postgres` user

## Database Setup

### 1. Connect to PostgreSQL

```bash
# macOS/Linux
psql postgres

# Windows (if PostgreSQL is in PATH)
psql -U postgres
```

### 2. Create Database and User

```sql
-- Create the database
CREATE DATABASE waitlist_db;

-- Create the user
CREATE USER waitlist_user WITH PASSWORD 'waitlist_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE waitlist_db TO waitlist_user;

-- Connect to the new database
\c waitlist_db

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO waitlist_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO waitlist_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO waitlist_user;

-- Exit psql
\q
```

### 3. Verify Setup

```bash
# Test connection with the new user
psql -h localhost -U waitlist_user -d waitlist_db
```

## Environment Configuration

### Backend (.env)

Create a `.env` file in the `backend/` directory:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/waitlist_db
SPRING_DATASOURCE_USERNAME=waitlist_user
SPRING_DATASOURCE_PASSWORD=waitlist_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# Twilio Configuration (Optional)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=your-twilio-phone-number

# SMS Configuration
SMS_MOCK_ENABLED=true

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### Frontend (.env)

Create a `.env` file in the `frontend/` directory:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

## Running the Application

### 1. Start PostgreSQL

```bash
# macOS (if using Homebrew)
brew services start postgresql@15

# Linux (systemd)
sudo systemctl start postgresql

# Windows
# PostgreSQL should start automatically as a service
```

### 2. Start Backend

```bash
cd backend
./mvnw spring-boot:run
```

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

## Troubleshooting

### Connection Issues

1. **Check if PostgreSQL is running:**
   ```bash
   # macOS
   brew services list | grep postgresql
   
   # Linux
   sudo systemctl status postgresql
   ```

2. **Check if the database exists:**
   ```bash
   psql -h localhost -U postgres -l
   ```

3. **Reset database (if needed):**
   ```sql
   -- Connect as postgres user
   psql postgres
   
   -- Drop and recreate
   DROP DATABASE IF EXISTS waitlist_db;
   DROP USER IF EXISTS waitlist_user;
   
   -- Then run the setup commands again
   ```

### Port Conflicts

If port 5432 is already in use:

1. **Find what's using the port:**
   ```bash
   # macOS/Linux
   lsof -i :5432
   
   # Windows
   netstat -ano | findstr :5432
   ```

2. **Change PostgreSQL port in postgresql.conf:**
   ```bash
   # Find config file
   sudo -u postgres psql -c "SHOW config_file;"
   
   # Edit the file and change port = 5433
   # Then restart PostgreSQL
   ```

3. **Update your .env file:**
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/waitlist_db
   ```

## Database Management

### Useful Commands

```bash
# Connect to database
psql -h localhost -U waitlist_user -d waitlist_db

# List all tables
\dt

# Describe a table
\d table_name

# View all databases
\l

# Exit psql
\q
```

### Backup and Restore

```bash
# Backup
pg_dump -h localhost -U waitlist_user waitlist_db > backup.sql

# Restore
psql -h localhost -U waitlist_user -d waitlist_db < backup.sql
```

## Security Notes

- Change the default passwords in production
- Use environment variables for sensitive data
- Consider using SSL connections for production
- Regularly backup your database

## Next Steps

Once PostgreSQL is set up:

1. Copy `backend/env.example` to `backend/.env` and update values
2. Copy `frontend/env.example` to `frontend/.env` and update values
3. Start the backend: `cd backend && ./mvnw spring-boot:run`
4. Start the frontend: `cd frontend && npm run dev`
5. Access the application at `http://localhost:5173`

The application will automatically create the necessary tables on first startup.
