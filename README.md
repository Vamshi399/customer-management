# Customer Management API

This project is a Spring Boot RESTful API designed for managing customer data. A key feature is the automatic calculation of a customer's membership tier based on their annual spend.

## Features

- CRUD operations for customer data
- Automatic calculation of membership tier based on annual spend
- RESTful endpoints with proper HTTP status codes
- Input validation
- Comprehensive error handling
- OpenAPI documentation
- Unit tests for service and controller layers

## Project Structure

The application follows a standard layered architecture:

-   `com.example.customer_management.controller`: Handles incoming HTTP requests and routes them to the appropriate services.
-   `com.example.customer_management.service`: Contains the core business logic, including membership tier calculation.
-   `com.example.customer_management.repository`: Manages data persistence using Spring Data JPA.
-   `com.example.customer_management.model` (or `entity`): Defines the JPA entities, such as `Customer`.
-   `com.example.customer_management.dto`: Data Transfer Objects used for API request and response payloads, ensuring a clean separation between the API layer and domain models.
-   `com.example.customer_management.exception`: Custom exception classes and a global exception handler (`@ControllerAdvice`) for consistent error responses.
-   `com.example.customer_management.config`: Configuration classes, such as for OpenAPI.

## Data Model

The primary entity in this application is `Customer`:

-   `id` (Long): The unique identifier for the customer (auto-generated).
-   `name` (String): The customer's name (subject to validation).
-   `email` (String): The customer's email address (unique, subject to validation).
-   `annualSpend` (BigDecimal): The total amount spent by the customer annually (subject to validation).
-   `membershipTier` (Enum: `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`): Automatically calculated based on `annualSpend`.
-   `dateCreated` (LocalDateTime): Timestamp of when the customer record was created (auto-generated).
-   `lastUpdated` (LocalDateTime): Timestamp of the last update to the customer record (auto-generated).


## Membership Tiers

| Tier      | Annual Spend Threshold |
|-----------|------------------------|
| BRONZE    | $0 - $999              |
| SILVER    | $1,000 - $4,999        |
| GOLD      | $5,000 - $9,999        |
| PLATINUM  | $10,000+               |

## Error Handling

The API implements robust error handling mechanisms:

-   **Input Validation:** Bean Validation (JSR 380) is utilized on request DTOs. Invalid input will trigger a `400 Bad Request` response, including clear messages detailing the validation errors.
-   **Resource Not Found:** Attempts to access a resource that does not exist (e.g., a customer with an unknown ID) will result in a `404 Not Found` status.
-   **Duplicate Email:** Attempting to create a customer with an email that already exists will result in a `409 Conflict` status.
-   **Global Exception Handler:** A centralized exception handler (`@ControllerAdvice`) catches common Spring exceptions and custom application-specific exceptions. It ensures that errors are returned as structured JSON responses with appropriate HTTP status codes (e.g., `500 Internal Server Error` for unexpected issues).


## Technologies

- Java 17
- Spring Boot 3.1.0
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- ModelMapper
- OpenAPI 3.0
- JUnit 5
- Maven (Build Tool)
- Mockito

## Getting Started

### Prerequisites

- Java 17 JDK
- Maven 3.8+

### Installation

1. Clone the repository
2. Build the project: `mvn clean install`
3. Run the application: `mvn spring-boot:run`
   - The application will start on `http://localhost:8080`.


## API Documentation

After starting the application, the OpenAPI documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## API Endpoints

| Method | Endpoint          | Description                     |
|--------|-------------------|---------------------------------|
| GET    | /api/customers    | Get all customers               |
| GET    | /api/customers/{id} | Get a specific customer by ID   |
| POST   | /api/customers    | Create a new customer           |
| PUT    | /api/customers/{id} | Update an existing customer     |
| DELETE | /api/customers/{id} | Delete a customer               |

## Testing

Run all tests with:
```bash
mvn test