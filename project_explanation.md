# FlexiSlot: Project Codebase Explanation

FlexiSlot is a SaaS MVP for slot-based booking and scheduling, designed to connect businesses offering services with customers making bookings. The project uses a modern monolithic full-stack architectural split: a **Spring Boot** backend API and a **React** frontend.

## 1. Backend Architecture (Spring Boot)

The backend is built with Java 17 and Spring Boot 3. It provides a RESTful API and uses Spring Data JPA for data persistence (MySQL) and Spring Security with JWT for authentication.

### Core Domain Entities (`com.flexislot.domain`)
The domain layer models the business logic and relationships:

*   **`User`**: The base authentication entity for both businesses and customers.
*   **`Business`**: Represents a service provider / merchant on the platform.
*   **`Customer`**: Represents an end-user who books services.
*   **`Service`**: Different offerings a Business provides (e.g., "30-min Consultation").
*   **`Slot`**: Represents the available time intervals for a Business to provide a Service.
*   **`Booking`**: A reservation made by a Customer for a specific Slot and Service.
*   **`Payment`**: Records payment transactions for Bookings.

*Note: All entities inherit from `BaseEntity`, which handles common auditing fields (`created_at`, `updated_at`) and uses **ULIDs** (Universally Unique Lexicographically Sortable Identifiers) for conflict-free, time-sortable primary string keys.*

### Controllers (`com.flexislot.controller`)
The controllers expose REST API endpoints and manage HTTP routing:

*   **`AuthController`**: Handles login and registration, issuing JWTs.
*   **`BusinessController` & `CustomerController`**: Manages profiles and dashboards for the respective user types.
*   **`ServiceController` & `SlotController`**: Allows businesses to define what they offer and when they are available.
*   **`BookingController`**: Handles the reservation workflow.
*   **`PaymentController`**: Manages booking payments.

## 2. Frontend Architecture (React + Vite)

The frontend is a React Single Page Application (SPA), styled with CSS, and organized around user roles and core workflows.

### Pages (`src/pages`)
The main views of the application are split by unauthenticated and authenticated user flows:

*   **Public Views**:
    *   `Home.jsx`: Landing page.
    *   `Login.jsx` & `Register.jsx`: Authentication flows.
    *   `PublicServices.jsx`: Allows users to browse available businesses and their offerings without necessarily logging in.
*   **Authenticated Views**:
    *   `BusinessDashboard.jsx`: The portal for service providers to manage their Services, Slots, and view Bookings.
    *   `CustomerDashboard.jsx`: The portal for users to view and manage their upcoming Bookings.
    *   `SlotPicker.jsx`: The interactive UI component/page where customers select an available time for a chosen service.

### Components (`src/components`)
*   **`Layout.jsx`**: The foundational shell wrapper for the application, handling consistent navigation (navbars/sidebars) and global UI structure across different pages.

## 3. Communication Flow

1.  **Authentication**: Users sign in via the React frontend -> Backend `AuthController` verifies credentials -> Returns a JWT.
2.  **API Requests**: The React app uses an interceptor or API client (`src/api/client.js`) to append the JWT to the `Authorization: Bearer <token>` header of all subsequent API calls.
3.  **Data Flow**: The backend Controllers map requests to internal Services, query/store data via Repositories (JPA), and return JSON DTOs (Data Transfer Objects) back to the frontend.

## 4. Key Features

### Dynamic Pricing
The platform implements a dynamic pricing engine (`PricingService.java`) that adjusts the price of a service slot based on current utilization (demand).
*   **High Demand**: If the slot utilization (booked slots / total slots for a service) exceeds 80%, the slot price is increased using a 1.3x multiplier.
*   **Low Demand**: If the utilization falls below 40%, the slot price is discounted to 0.7x of the base price.
*   **Normal Demand**: For utilization between 40% and 80%, the standard base price is used.
These real-time prices are calculated dynamically when slots are created or fetched to ensure competitive pricing and maximize revenue for businesses.

### Auto Slot Creation
Businesses can automatically generate a full schedule of slots for a day using the bulk generation feature (`SlotService.java`).
*   **Operating Hours**: The system reads the business's defined operating hours (e.g., "09:00-17:00"). If none are provided or they are invalid, it defaults to a standard 9-to-5 schedule.
*   **Duration Increments**: It continuously creates available slots starting from the opening time, incrementing by the defined `durationMinutes` of the service, until the closing time is reached.
*   **Conflict Prevention**: Before creating each slot, it checks the database to see if a slot already exists for that specific date and time period, preventing duplicate slots.