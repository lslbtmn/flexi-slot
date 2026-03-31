# FlexiSlot Backend

Spring Boot 3.2 REST API for the FlexiSlot SaaS MVP.

## Requirements

- Java 17
- Maven 3.8+
- MySQL 8 (e.g. XAMPP) running on `localhost:3306`

## Database Setup

1. Start MySQL (XAMPP Control Panel → Start MySQL).
2. Create database and apply schema:

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS flexislot;"
mysql -u root flexislot < src/main/resources/db/schema.sql
```

Or run the SQL in `src/main/resources/db/schema.sql` via phpMyAdmin.

## Configuration

- `application.yml`: MySQL URL `jdbc:mysql://localhost:3306/flexislot`, user `root`, blank password.
- JWT secret and expiration are under `jwt.*`.

## Build & Run

```bash
mvn clean package
mvn spring-boot:run
```

API base: `http://localhost:8080/api`

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## Seed Data

On first run (when no users exist), demo data is created:

| Email                 | Password   | Role           |
|-----------------------|------------|----------------|
| admin@flexislot.com   | admin123   | ADMIN          |
| owner@flexislot.com   | owner123   | BUSINESS_OWNER |
| customer@flexislot.com| customer123| CUSTOMER       |

## API Examples (curl)

### Auth

```bash
# Register
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"new@test.com","password":"password123","role":"CUSTOMER"}'

# Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@flexislot.com","password":"owner123"}'
# Save the "token" from response as TOKEN for next calls.
```

### Business (use token from login)

```bash
export TOKEN="<your-jwt>"

# Create business (BUSINESS_OWNER)
curl -s -X POST http://localhost:8080/api/business \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"My Salon","email":"salon@test.com","phone":"+111","location":"Street 1","serviceType":"Beauty"}'

# Get business by ID
curl -s http://localhost:8080/api/business/<businessId>

# Update business
curl -s -X PUT http://localhost:8080/api/business/<businessId> \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"My Salon Updated","email":"salon@test.com","phone":"+111","location":"Street 1","serviceType":"Beauty"}'
```

### Services

```bash
# Create service (BUSINESS_OWNER; must have created business first)
curl -s -X POST http://localhost:8080/api/services \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"serviceName":"Haircut","basePrice":25.00,"durationMinutes":30}'

# Get service by ID
curl -s http://localhost:8080/api/services/<serviceId>

# List services by business (paginated)
curl -s "http://localhost:8080/api/services/business/<businessId>?page=0&size=20"
```

### Slots

```bash
# Create slot (BUSINESS_OWNER)
curl -s -X POST http://localhost:8080/api/slots \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"serviceId":"<serviceId>","slotDate":"2025-03-15","startTime":"09:00","endTime":"09:30"}'

# Get available slots for service (optional date range)
curl -s "http://localhost:8080/api/slots/service/<serviceId>?page=0&size=20"
curl -s "http://localhost:8080/api/slots/service/<serviceId>?fromDate=2025-03-01&toDate=2025-03-31&page=0&size=20"
```

### Customers

```bash
# Create customer profile (CUSTOMER role)
curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"John Doe","email":"john@test.com","phone":"+222"}'

# Get customer
curl -s http://localhost:8080/api/customers/<customerId> -H "Authorization: Bearer $TOKEN"
```

### Bookings

```bash
# Create booking (CUSTOMER; need customer profile first)
curl -s -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"slotId":"<slotId>"}'

# List bookings by customer
curl -s "http://localhost:8080/api/bookings/customer/<customerId>?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Cancel booking
curl -s -X PUT http://localhost:8080/api/bookings/<bookingId>/cancel \
  -H "Authorization: Bearer $TOKEN"
```

### Payments (mock)

```bash
# Initiate payment
curl -s -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"bookingId":"<bookingId>","amount":25.00,"currency":"USD","provider":"mock"}'

# Mock success
curl -s -X PUT http://localhost:8080/api/payments/<paymentId>/success -H "Authorization: Bearer $TOKEN"

# Mock fail
curl -s -X PUT http://localhost:8080/api/payments/<paymentId>/fail -H "Authorization: Bearer $TOKEN"
```

## Tests

```bash
mvn test
```

## Security

- JWT in `Authorization: Bearer <token>`.
- Roles: ADMIN, BUSINESS_OWNER, CUSTOMER.
- Business resources are scoped by owner; never trust client-supplied businessId for mutations.
- CORS allowed for `http://localhost:5173` (Vite default).
