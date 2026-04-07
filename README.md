### payflow

##   description

Microservices-based payment system with two independent services (account management and transfers) running in Docker, with Python automation scripts and a full CI/CD pipeline in GitHub Actions.


##   architecture

Two independent Spring Boot services, each with its own PostgreSQL database. 

* `Account Service` handles accounts and balances.
* `Transfer Service` handles money transfers and talks to the account service over REST.

Everything runs in Docker Compose. Python scripts automate smoke testing, log analysis, and deployment. GitHub Actions runs the full CI/CD pipeline on every push.

![architecture](docs/payflow-architecture.svg)


## saga pattern

When a transfer is requested, the system follows a step-by-step process. If any step fails, all previous steps get reversed so no money is lost. 

The transfer service talks to the account service over REST. If debiting the sender succeeds but crediting the receiver fails, the system automatically refunds the sender (compensation) and marks the transfer as FAILED.

![saga](docs/payflow-saga.svg)


## database per service

Each microservice manages its own separate PostgreSQL database. There are no foreign keys connecting the tables. 

The logical relationship between `accounts` and `transfers` is maintained strictly at the application level via REST API calls, which is why the Saga pattern is required to keep data consistent.

![database](docs/database-per-service.svg)

##  tech stack
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
