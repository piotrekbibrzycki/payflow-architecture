### payflow

##   description

Microservices-based payment system with two independent services (account management and transfers) running in Docker.

Includes Python smoke testing, Flyway database migrations, and a GitHub Actions CI pipeline for automated validation.


##   architecture

Two independent Spring Boot services, each with its own PostgreSQL database. 

* `Account Service` handles accounts and balances.
* `Transfer Service` handles money transfers and talks to the account service over REST.

Everything runs in Docker Compose. A Python smoke test validates the critical transfer flow after startup. GitHub Actions runs unit tests, starts the Docker environment, and executes smoke tests on every push.

![architecture](docs/payflow-architecture.svg)


## saga pattern

When a transfer is requested, the system follows a step-by-step process. If any step fails, all previous steps get reversed so no money is lost. 

The transfer service talks to the account service over REST. If debiting the sender succeeds but crediting the receiver fails, the system automatically refunds the sender (compensation) and marks the transfer as FAILED.

![saga](docs/payflow-saga.svg)


## database per service

Each microservice manages its own separate PostgreSQL database. There are no foreign keys connecting the tables. 

The logical relationship between `accounts` and `transfers` is maintained strictly at the application level via REST API calls, which is why the Saga pattern is required to keep data consistent.

![database](docs/database-per-service.svg)

## CI pipeline
GitHub Actions runs the validation pipeline on every push. It executes unit tests for both services, starts the Docker Compose environment, and runs the smoke test to verify the critical transfer flow. This makes sure the project is checked both at the code level and at the full system level.

![ci-pipeline](docs/ci-pipeline.svg)

## API documentation

After starting the application, Swagger UI is available at:

  * Account Service: `http://localhost:8081/swagger-ui/index.html`
  * Transfer Service: `http://localhost:8082/swagger-ui/index.html`

## database migrations

Database schema changes are managed with `Flyway`. Each service applies its own migrations independently at startup, which keeps the schema aligned with the service that owns the data.


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
