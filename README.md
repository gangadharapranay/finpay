# FinPay - Uber-like Rider Payment and Tracking Backend

FinPay is a microservices backend inspired by ride-hailing apps like Uber.  
It is designed to power rider-trip payment lifecycle events with reliable processing, status updates, and service-to-service communication.

## What this project is

- Backend system for handling rider payment requests in an Uber-like flow.
- Supports asynchronous payment lifecycle updates (initiated, processing, completed/failed).
- Built with an event-driven architecture for scale and fault tolerance.
- Suitable as a fintech/microservices backend project for learning and interviews.

## Core features (point-wise)

- **API Gateway routing** for a single entry point to internal services.
- **Service discovery** using Eureka for dynamic microservice registration.
- **Payment initiation API** with `Idempotency-Key` support for safe retries.
- **Asynchronous processing** via Kafka topics for decoupled communication.
- **Saga-style orchestration** in payment processing for multi-step transaction flow.
- **Account and ledger handling** for debit/credit operations with consistency checks.
- **Payment status publishing** so clients can track transaction state changes.
- **Optimistic locking and retry-safe design** to reduce duplicate or inconsistent updates.

## Tech stack

- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Cloud components**: Spring Cloud Gateway, Eureka (Netflix)
- **Database**: PostgreSQL
- **Messaging/Event Streaming**: Apache Kafka + Zookeeper
- **Build tool**: Maven (`mvnw`)
- **Containerized local infra**: Docker Compose

## Services and ports

- `service-registry` - Eureka discovery server (`8761`)
- `api-gateway` - gateway routes (`8000`)
- `payment-api-service` - payment creation and event publish (`8001`)
- `payment-process-service` - async processing and status publish (`8002`)
- `account-service` - account balances and ledger transactions (`8003`)
- `reconciliation-service` - reconciliation module (WIP)

## How to start the app locally

### 1) Prerequisites

- Java 21 installed
- Docker Desktop running
- Maven wrapper permission enabled (if needed)

### 2) Start infrastructure (Postgres + Kafka + Zookeeper)

From project root:

```bash
docker compose up -d
```

### 3) Start all microservices

Open separate terminals and run from each service directory:

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

Start services in this order for smoother boot:

1. `service-registry`
2. `api-gateway`
3. `account-service`
4. `payment-api-service`
5. `payment-process-service`
6. `reconciliation-service` (optional/WIP)

### 4) Verify services

- Eureka dashboard: `http://localhost:8761`
- API Gateway base URL: `http://localhost:8000`

## High-level payment flow

1. Client sends payment request through `api-gateway`.
2. `payment-api-service` validates and stores payment request.
3. Payment initiation event is published to Kafka.
4. `payment-process-service` consumes event and coordinates processing.
5. `account-service` performs transfer/ledger update.
6. Payment status event is published and persisted for tracking.

## Future improvements

- Add real-time rider/trip status push via WebSocket or SSE.
- Add Spring Security + JWT and role-based authorization.
- Add Flyway/Liquibase for production-grade schema migrations.
- Add observability (Actuator, Micrometer, OpenTelemetry).
- Add Testcontainers-based integration testing.

