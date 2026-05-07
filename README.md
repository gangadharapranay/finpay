# FinPay (Backend / FinTech Microservices)

FinPay is a fintech-style backend built with **Spring Boot 3**, **Postgres**, **Kafka**, **Eureka Service Registry**, and an **API Gateway**.

## Services

- **`service-registry`**: Eureka discovery server (`:8761`)
- **`api-gateway`**: Spring Cloud Gateway routes (`:8000`)
- **`account-service`**: Account balances + ledger transactions (`:8003`)
- **`payment-api-service`**: Payment creation + idempotency + event publishing (`:8001`)
- **`payment-process-service`**: Async payment processing + account transfer + status publishing (`:8002`)
- **`reconciliation-service`**: (WIP) reconciliation placeholder

## Payment flow (happy path)

1. Client calls `POST /api/payments` on **gateway** with an **`Idempotency-Key`**
2. `payment-api-service` persists the payment and publishes a `payment-initiation` Kafka event (pacs.008 XML payload)
3. `payment-process-service` consumes, validates, calls `account-service` transfer, generates pacs.002, and publishes `payment-status`
4. `payment-api-service` consumes `payment-status` and updates the payment record

## Fintech-grade behaviors implemented

- **Idempotency (API)**: `Idempotency-Key` + **request hashing** to support safe retries
- **Optimistic locking**: `@Version` on `accounts` and `payments`
- **Retry safety (ledger)**: `account-service` transfer is **idempotent by `paymentId`** and protected by a DB unique constraint
- **Async processing**: Kafka topics with retry + DLQ behavior on listeners

## Running locally

Start infra:

```bash
docker compose up -d
```

Start services (separately, in each service folder):

```bash
./mvnw spring-boot:run
```

## Notes / next upgrades (high-signal for interviews)

- Add **Spring Security + JWT** at the gateway (RBAC, rate limits, audit trail)
- Replace `ddl-auto:update` with **Flyway** migrations
- Implement a **Transactional Outbox** for Kafka publishing (exactly-once-ish semantics)
- Add **Actuator + Micrometer + OpenTelemetry** (metrics/tracing)
- Add **Testcontainers** integration tests (Postgres + Kafka)


