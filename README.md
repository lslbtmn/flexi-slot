# FlexiSlot – SaaS MVP

Monorepo: **Backend** (Java Spring Boot 3.x + MySQL) + **Frontend** (React Vite + Axios).  
JWT auth, role-based access (ADMIN, BUSINESS_OWNER, CUSTOMER), multi-tenant business scoping, dynamic pricing, slot booking with concurrency-safe reservations.

---

## 1) Repository structure

```
flexislot/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/flexislot/
│   │   ├── FlexiSlotApplication.java
│   │   ├── config/          (Security, CORS, JWT, OpenAPI, SeedDataRunner)
│   │   ├── controller/      (Auth, Business, Customer, Service, Slot, Booking, Payment)
│   │   ├── domain/          (User, Business, Customer, Service, Slot, Booking, Payment, BaseEntity, enums)
│   │   ├── dto/             (auth, business, customer, service, slot, booking, payment, common)
│   │   ├── exception/       (AppException, ResourceNotFoundException, ForbiddenException, GlobalExceptionHandler, ErrorResponse)
│   │   ├── repository/      (JPA repositories)
│   │   ├── security/        (JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal, UserDetailsServiceImpl)
│   │   └── service/         (Auth, Business, Customer, ServiceCatalog, Slot, Booking, Payment, pricing/PricingService)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/schema.sql
│   ├── src/test/java/...    (PricingServiceTest, BookingServiceTest)
│   └── README.md
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx, App.jsx, index.css
│       ├── api/client.js
│       ├── components/Layout.jsx
│       └── pages/ (Login, Register, PublicServices, BusinessDashboard, CustomerDashboard, SlotPicker)
├── postman/
│   └── FlexiSlot-API.postman_collection.json
└── README.md (this file)
```

---

## 2) Database (MySQL / XAMPP)

1. Start **MySQL** (e.g. XAMPP → Start MySQL).
2. Create DB and apply schema:

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS flexislot;"
mysql -u root flexislot < backend/src/main/resources/db/schema.sql
```

Or in phpMyAdmin: create database `flexislot`, then run the contents of `backend/src/main/resources/db/schema.sql`.

- **Tables**: `users`, `business`, `customer`, `services`, `slot`, `bookings`, `payments` (all with ULID `id` VARCHAR(26), InnoDB, FKs and indexes as in schema).

---

## 3) Run backend

- **Java 17**, **Maven 3.8+**.

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

- API base: **http://localhost:8080/api**
- Swagger UI: **http://localhost:8080/api/swagger-ui.html**
- Config: `backend/src/main/resources/application.yml` (MySQL: `localhost:3306`, user `root`, blank password).

On first run, seed data creates:

| Email                  | Password    | Role           |
|------------------------|------------|----------------|
| admin@flexislot.com    | admin123   | ADMIN          |
| owner@flexislot.com    | owner123   | BUSINESS_OWNER  |
| customer@flexislot.com | customer123| CUSTOMER       |

---

## 4) Run frontend

```bash
cd frontend
npm install
npm run dev
```

- App: **http://localhost:5173**
- Vite proxy forwards `/api` to `http://localhost:8080`.

---

## 5) Quick test

1. **Backend**: `cd backend && mvn clean package` then `mvn spring-boot:run`.
2. **DB**: Schema applied (see step 2).
3. **Frontend**: `cd frontend && npm install && npm run dev`.
4. Open http://localhost:5173 → Register or Login (e.g. `owner@flexislot.com` / `owner123`).
5. **Business owner**: “My business” → create/update business, add services, create slots.
6. **Customer**: Login as `customer@flexislot.com` / `customer123` → “My bookings” → create profile → “Browse services” (enter business ID from step 5) → pick service → “View slots” → Book.

---

## 6) API summary

- **Auth**: `POST /auth/register`, `POST /auth/login` (JWT + userId + role).
- **Business**: `POST /business`, `GET /business/me`, `GET /business/{id}`, `PUT /business/{id}`, `DELETE /business/{id}`.
- **Services**: `POST /services`, `GET /services/{id}`, `GET /services/business/{businessId}`, `PUT /services/{id}`, `DELETE /services/{id}`.
- **Slots**: `POST /slots`, `GET /slots/service/{serviceId}`, `PUT /slots/{id}`, `DELETE /slots/{id}`.
- **Customers**: `POST /customers`, `GET /customers/me`, `GET /customers/{id}`, `PUT /customers/{id}`, `DELETE /customers/{id}`.
- **Bookings**: `POST /bookings`, `GET /bookings/customer/{customerId}`, `PUT /bookings/{id}/cancel`.
- **Payments**: `POST /payments/initiate`, `PUT /payments/{id}/success`, `PUT /payments/{id}/fail`.

Details and curl examples: **backend/README.md**.  
Postman: import **postman/FlexiSlot-API.postman_collection.json**; run “Login” to set `token`, then use other requests.

---

## 7) Security and quality

- **JWT** + **BCrypt**; **@PreAuthorize** and method security; business resources scoped by owner (never trust client `businessId` for mutations).
- **Booking**: transactional create with **pessimistic lock** on slot + unique `slot_id` in `bookings` to prevent double booking.
- **Pricing**: `PricingService` (utilization-based: &gt;80% ×1.3, 40–80% ×1, &lt;40% ×0.7); `BigDecimal` scale 2, HALF_UP.
- DTOs only (no entity exposure), validation, global exception handler, SLF4J logging, pagination where specified.
