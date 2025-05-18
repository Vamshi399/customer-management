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
| SILVER    | $0 - $999              |
| GOLD      | $1,000 - $9,999        |
| PLATINUM  | $10,000+               |

## Error Handling

The API implements robust error handling mechanisms:

-   **Input Validation:** Bean Validation (JSR 380) is utilized on request DTOs. Invalid input will trigger a `400 Bad Request` response, including clear messages detailing the validation errors.
-   **Resource Not Found:** Attempts to access a resource that does not exist (e.g., a customer with an unknown ID) will result in a `404 Not Found` status.
-   **Duplicate Email:** Attempting to create a customer with an email that already exists will result in a `409 Conflict` status.
-   **Global Exception Handler:** A centralized exception handler (`@ControllerAdvice`) catches common Spring exceptions and custom application-specific exceptions. It ensures that errors are returned as structured JSON responses with appropriate HTTP status codes (e.g., `500 Internal Server Error` for unexpected issues).


## Technologies

- Java 21
- Spring Boot 3.4.5
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

- Java 21 JDK
- Maven 3.8+

### Installation

1. Clone the repository
2. Build the project: `mvn clean install`
3. Run the application: `mvn spring-boot:run`
   - The application will start on `http://localhost:8081`.


## API Documentation

After starting the application, the OpenAPI documentation is available at:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## API Endpoints

| Method | Endpoint                     | Description                      |
|--------|------------------------------|----------------------------------|
| POST   | /api/customers               | Create a new customer            |
| GET    | /api/customers/{id}          | Get a specific customer by ID    |
| GET    | /api/customers?name={name}   | Get a specific customer by Name  |
| GET    | /api/customers?email={email} | Get a specific customer by Email |
| PUT    | /api/customers/{id}          | Update an existing customer      |
| DELETE | /api/customers/{id}          | Delete a customer                |

## Testing

Run all tests with:
`mvn test`

---

# Customer Management Application

This project is a Spring Boot application for managing customers.

## Prerequisites

Before you begin, ensure you have the following installed:

1.  **Java Development Kit (JDK)**
    *   Download and install JDK 21 or later from [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/#java21).
    *   **Set up Environment Variables:**
        *   Set the `JAVA_HOME` environment variable to your JDK installation directory.
        *   Add the `bin` directory of your JDK installation to your system's `PATH` environment variable.
            *   **Windows:**
                1.  Search for "environment variables" and select "Edit the system environment variables".
                2.  In the System Properties window, click the "Environment Variables..." button.
                3.  Under "System variables", click "New..." to add `JAVA_HOME`. Variable name: `JAVA_HOME`, Variable value: `C:\Program Files\Java\jdk-21` (or your JDK installation path).
                4.  Find the `Path` variable in "System variables", select it, and click "Edit...". Click "New" and add `%JAVA_HOME%\bin`.
            *   **macOS/Linux:**
                1.  Open your shell configuration file (e.g., `~/.bashrc`, `~/.zshrc`, `~/.profile`).
                2.  Add the following lines:
                    ```bash
                    export JAVA_HOME="/path/to/your/jdk-21" # Replace with your actual JDK path
                    export PATH="$JAVA_HOME/bin:$PATH"
                    ```
                3.  Save the file and run `source ~/.bashrc` (or your respective shell config file) or open a new terminal.
    *   Verify the installation by opening a new terminal or command prompt and typing:
        ```bash
        java -version
        javac -version
        ```

2.  **Visual Studio Code (VSCode)**
    *   Download and install VSCode from VSCode Download Page.

## Setup Instructions

1.  **Install VSCode Extensions:**
    Open VSCode, go to the Extensions view (Ctrl+Shift+X or Cmd+Shift+X), and install the following extensions:
    *   `formulahendry.code-runner` (Code Runner)
    *   `vscjava.vscode-java-debug` (Debugger for Java)
    *   `vscjava.vscode-java-pack` (Extension Pack for Java - this will install several of the below automatically)
    *   `redhat.java` (Language Support for Java(TM) by Red Hat)
    *   `vscjava.vscode-maven` (Maven for Java)
    *   `vscjava.vscode-java-dependency` (Project Manager for Java)
    *   `vscjava.vscode-spring-boot-dashboard` (Spring Boot Dashboard)
    *   `vmware.vscode-spring-boot` (Spring Boot Tools)
    *   `pivotal.vscode-spring-initializer` (Spring Initializer Java Support)
    *   `vscjava.vscode-java-test` (Test Runner for Java)

2.  **Clone the Repository:**
    Open your terminal or command prompt and clone the project into your desired workspace directory:
    ```bash
    git clone https://github.com/Vamshi399/customer-management.git
    cd customer-management
    ```

3.  **Open Project in VSCode:**
    Open the cloned `customer-management` folder in VSCode.
    ```bash
    code .
    ```
    VSCode should automatically detect it as a Maven project. Allow it to import/load the project if prompted.

## Running and Debugging the Application

1.  **VSCode Debugger Configuration:**
    The project includes a launch configuration for VSCode. If it's not already present, ensure your `.vscode/launch.json` file looks like this:

    ```jsonc
    {
        "version": "0.2.0",
        "configurations": [
            {
                "type": "java",
                "name": "Spring Boot-CustomerManagementApplication<customer-management>",
                "request": "launch",
                "cwd": "${workspaceFolder}",
                "mainClass": "com.example.customermanagement.CustomerManagementApplication",
                "projectName": "customer-management",
                "args": "",
                "envFile": "${workspaceFolder}/.env"
            }
        ]
    }
    ```
    *(Note: If you have an existing `launch.json` with other configurations, you can add the above configuration to the `configurations` array.)*

2.  **Start the Application:**
    *   Open the "Run and Debug" view in VSCode (Ctrl+Shift+D or Cmd+Shift+D).
    *   Select the `Spring Boot-CustomerManagementApplication<customer-management>` configuration from the dropdown.
    *   Click the green play button (Start Debugging) or press F5.

    The application will start, and you should see Tomcat server logs in the VSCode terminal or debug console, typically indicating it has started on port `8080` (or `8081` if `server.port=8081` is set in `application.properties` or your `.env` file).

## Accessing the Application

Once the Tomcat server has started successfully:

1.  **Swagger UI (API Documentation & Testing):**
    Open your web browser and navigate to:
    http://localhost:8081/swagger-ui/index.html#/

    *   *(Note: The port might be `8080` if not explicitly configured to `8081` in your application properties or `.env` file. Please check your `application.properties` or `.env` for the `server.port` configuration.)*
    *   You can explore and test the available APIs using Swagger UI. Provide appropriate inputs for each endpoint to see the responses.

2.  **H2 Database Console:**
    To access the in-memory H2 database console, open your web browser and navigate to:
    http://localhost:8080/h2-console

    *   **JDBC URL:** Ensure the JDBC URL matches the one configured in your `application.properties` (usually `jdbc:h2:mem:testdb`).
    *   **User Name:** `sa` (or as configured)
    *   **Password:** (leave blank or as configured)
    *   Click "Connect".

    You can then browse the database tables and run SQL queries.

## API Testing

Use the Swagger UI (mentioned above) to try out the APIs.
*   Provide appropriate JSON payloads for POST/PUT requests.
*   Observe the HTTP status codes and response bodies.
*   For example, creating a new customer should return a `201 Created` status, and fetching customers should return a `200 OK` with a list of customers.
* create customer success
![create_customer_success_swagger](https://github.com/user-attachments/assets/5433e497-f26d-478b-a04b-c380b95d53dc)

* get customer by ID success
![get_customer_byID_success_swagger](https://github.com/user-attachments/assets/8762f38b-ef64-4cb7-a2a7-bd7f74d121cc)

* get customer by Name success
![get_customer_byName_success_swagger](https://github.com/user-attachments/assets/9eaa8d16-03d0-45b0-870a-19d6b4893fd9)

* get customer by Email success
![get_customer_byEmail_success_swagger](https://github.com/user-attachments/assets/c10e31ab-60cd-4e28-9e58-3863fcfec0d2)

* update customer by ID success
![update_customer_byID_success_swagger](https://github.com/user-attachments/assets/c9e273fe-b03a-4ed7-abfc-5b3ad235a5fc)

* delete customer by ID success
![delete_customer_byID_success_swagger](https://github.com/user-attachments/assets/46b366de-6d11-4d69-b783-bb53400ae85d)






