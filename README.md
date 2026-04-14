### payflow

##   description

Microservices-based payment system built with Java 21 and Spring Boot 4.

Two independent services handle account management and money transfers, each with its own PostgreSQL database, connected over REST. Transfers follow the Saga pattern with automatic compensation on failure, so no money is ever lost.

The system includes JWT authentication with role-based access control, Flyway database migrations, structured logging with correlation IDs across services, Docker Compose orchestration, Python smoke testing, and a GitHub Actions CI pipeline


##   architecture

Two independent Spring Boot services, each with its own PostgreSQL database. 

* `Account Service` handles accounts and balances.
* `Transfer Service` handles money transfers and talks to the account service over REST.

Everything runs in Docker Compose. A Python smoke test validates the critical transfer flow after startup. GitHub Actions runs unit tests, starts the Docker environment, and executes smoke tests on every push.

![architecture](docs/payflow-architecture.svg)


## saga pattern

When a transfer is requested, the system follows a step-by-step process. If any step fails, all previous steps get reversed so no money is lost. 

The transfer service talks to the account service over REST. If debiting the sender succeeds but crediting the receiver fails, the system automatically refunds the sender (compensation) and marks the transfer as FAILED.

If compensation fails, the system logs a CRITICAL error with the transfer ID for manual intervention.

![saga](docs/payflow-saga.svg)

## security

Authentication and authorization are handled through JWT tokens issued by the account service. Users register and log in through dedicated auth endpoints. Passwords are hashed with BCrypt and never stored in plain text.

After a successful login, the server returns a signed JWT containing the user's email and role. Both services validate this token on every request using a shared secret key. Role-based access control restricts account creation to ADMIN users, while any authenticated user can perform transfers and read data.

The transfer service does not manage users or issue tokens. It only validates tokens issued by the account service, keeping the auth logic in one place. A default admin account is seeded via Flyway migration so the system is functional immediately after startup.

![security](docs/jwt-authentication.svg)

## observability

Each service includes a correlation ID filter that tags every request with a unique `X-Correlation-Id` header. If the client sends a correlation ID, both services propagate it across the entire request chain. If no ID is provided, the first service generates one automatically.

The correlation ID appears in every log line via SLF4J MDC, making it possible to trace a single transfer across both services and their databases. The transfer service forwards the correlation ID to the account service on every REST call, so a single request can be followed end-to-end through the logs.

Spring Boot Actuator exposes a public `/actuator/health` endpoint for container health checks and monitoring.

![observability](docs/observability.svg)

## database per service

Each microservice manages its own separate PostgreSQL database. There are no foreign keys connecting the tables. 

The logical relationship between `accounts` and `transfers` is maintained strictly at the application level via REST API calls, which is why the Saga pattern is required to keep data consistent.

![database](docs/database-per-service.svg)

## CI pipeline
GitHub Actions runs the validation pipeline on every push. It executes unit tests for both services, starts the Docker Compose environment, and runs the smoke test to verify the critical transfer flow. This makes sure the project is checked both at the code level and at the full system level.

![ci-pipeline](docs/ci-pipeline.svg)

## database migrations

Database schema changes are managed with Flyway. Each service applies its own migrations independently at startup, which keeps the schema aligned with the service that owns the data.

## API documentation

After starting the application, Swagger UI is available at:

  * Account Service: `http://localhost:8081/swagger-ui/index.html`
  * Transfer Service: `http://localhost:8082/swagger-ui/index.html`

## run locally

Clone the repository and start the full environment with Docker Compose:

```bash
docker compose up -d --build
```
To verify the critical transfer flow:
```bash
python scripts/smoke_test.py
```
To stop the environment:
```bash
docker compose down -v
```


##  tech stack

[![My Stack](https://skillicons.dev/icons?i=java,spring,postgres,docker,py,githubactions&theme=light)](https://skillicons.dev)
