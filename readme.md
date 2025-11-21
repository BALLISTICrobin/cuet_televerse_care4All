# 🛡️ CareForAll — Resilient Donation Platform
## A Microservices-based System Built to Survive Real-World Chaos

***

## 🚨 Background: Why We Rebuilt the System

The previous CareForAll donation backend collapsed under high-traffic conditions due to critical architectural flaws:

| Issue | Root Cause | Impact |
| :--- | :--- | :--- |
| ⚠️ **Double Charging** | No idempotency | Donors charged twice |
| 👻 **Ghost Donations** | Pledge saved but event not sent | Totals incorrect / lost money |
| ❌ **Wrong Payment Status** | Out-of-order webhooks overwrote valid states | Negative campaign totals |
| 🔥 **Database Meltdown** | `SUM` query on every refresh | 100% CPU → Total crash |
| 🕵️‍♂️ **No Observability** | No metrics or traces | Debugging impossible |

📌 The system lacked **idempotency, reliable messaging, state validation, read models, retries & monitoring** → all must be fixed.

***

## 🚀 Goal

Build a donation platform that remains:

- **✔ Correct**
- **✔ Scalable**
- **✔ Fault-tolerant**
- **✔ Monitored**
- **✔ Real-world ready**

Even during retry storms, webhook chaos & 1000+ RPS spikes.

***

## 🏗 Architecture Overview

The system utilizes an **event-driven microservice architecture** powered by Docker Compose/Swarm.

| Service | Responsibility | Key DB/Component |
| :--- | :--- | :--- |
| **Auth Service** | User management, JWT generation. | Auth DB |
| **Campaign Service** | Campaign info, fast read totals. | Campaign DB (Read-heavy) |
| **Donation Service** | Core business logic, pledge creation. | Donation DB (Write-heavy) + Outbox |
| **Payment Service** | External provider communication, webhook handling. | Payment DB (State Machine) |
| **API Gateway (Nginx)** | Load balancing, routing, SSL termination. | N/A |

```

Client
↳ API Gateway (Nginx)
↳ Auth Service (Users + JWT)
↳ Campaign Service (Read-heavy)
↳ Donation Service (Write-heavy + Outbox)
↳ Payment Service (Webhook + State Machine)

RabbitMQ  ←→  Redis (Idempotency Keys)
↓
PostgreSQL DBs (Business Source of Truth)

````

📦 **Fully Containerized** — Runs seamlessly with `docker compose`.

***

## 💡 Key Engineering Solutions

### 🛑 1️⃣ Redis Idempotency → No Double Charges
* Each donation request **must** include an `Idempotency-Key` header.
* **Redis** is used as a fast, volatile cache to check this key.
* If the key exists, the request is instantly blocked and the previous result returned, ensuring **exactly-once donation safety**.

### 🔄 2️⃣ Transactional Outbox → No Ghost Donations
* The **Pledge** record and the corresponding **Outbox event** are saved in **one atomic DB transaction** (in the Donation DB).
* A dedicated worker (Outbox Relayer) retries publishing the event to RabbitMQ until delivery succeeds.
* **✔ No missing totals** and **no donation is ever lost** due to mid-request crashes.

### 🔐 3️⃣ Payment State Machine → No Negative Totals
* The **Payment Service** uses logic enforced by the database schema (`version` column) and service validation to manage status transitions.
* **Valid Transitions:** `INITIATED` → `AUTHORIZED` → `CAPTURED` → `REFUNDED` / `FAILED`.
* **🚫 Backward transitions are rejected** (e.g., a delayed webhook cannot force `CAPTURED` back to `AUTHORIZED`), ensuring payment history integrity.

### ⚡ 4️⃣ CQRS + Read Model → No Database Meltdown
* We use **Command Query Responsibility Segregation (CQRS)** by separating the write data (Pledges table) from the read data (Campaign Totals table).
* Instead of recalculating totals on every read, we maintain pre-computed totals:
    ```sql
    SELECT total_pledged FROM campaign_totals ... ✔️
    ```
* **✔ Reads = O(1)** and the system is **stable under 1000+ refreshes/sec**.

***

## 📊 Observability & Stress Test Proof

* **Integrated:** We utilize the **Prometheus, Grafana, and Jaeger/OpenTelemetry** stack (as per Checkpoint 3) within our Docker Compose setup.
* **Validated:** Stress testing confirmed:
    * CPU usage shows predictable **"Burst & Recover"** behavior.
    * All services (RabbitMQ, DB, Payment Service) spike and recover in sync.
    * Flat memory usage confirms **zero leaks**.

Architecture validated under real load — **zero crashes**.

***

## 🗂 Data Model Summary

Our robust schema includes keys for fault tolerance:

| Table | Purpose | Key Fields for Resilience |
| :--- | :--- | :--- |
| `pledges` | Permanent business record | `idempotency_key`, `version` |
| `outbox_events` | Guaranteed event signaling | `processed` flag |
| `payments` | State-machine protected status | `webhook_idempotency_key` |
| `campaign_totals` | Fast read model (CQRS) | `total_captured`, `donation_count` |

***

## 🔁 CI/CD Pipeline

The pipeline uses **GitHub Actions** to automate continuous integration and deployment.

### Continuous Integration (CI)
* Runs unit tests on every pull request.
* Uses intelligent caching to **only build images for changed microservices**.
* Prevents broken code from merging.

### Continuous Deployment (CD)
* Automatically runs:
    ```bash
    docker compose pull
    docker compose up -d --build
    ```
* **✔ Services are individually versioned** (e.g., `donation-service:v1.2.1`).

***

## ▶️ How to Run Locally

### Requirements
- Docker
- Docker Compose

### Start Everything
```bash
docker compose up -d --build
````

### Stop Services

```bash
docker compose down
```

### Reset (includes DB + queues data):

```bash
docker compose down -v
```

### 🧪 Testing Endpoints

All traffic goes through the API Gateway (port 8080):

| Endpoint Example | Use |
| :--- | :--- |
| `POST /donate` | Create pledge (requires `Idempotency-Key` header) |
| `GET /campaigns/:id/total` | Check raised amount (fast O(1) read) |

📌 **Use the `Idempotency-Key` header to test duplicate blocking and prove the system's resilience.**

-----

## 🏁 Final Outcome

| Critical Requirement | Status |
| :--- | :--- |
| Prevent duplicate charges | **✔ Solved** |
| Prevent lost donations | **✔ Solved** |
| Prevent wrong payment state | **✔ Solved** |
| Handle high load | **✔ Proven** |
| Add observability | **✔ Integrated** |
| Fault-tolerant microservices | **✔ Achieved** |

**🥇 System now survives everything that destroyed the old one.**

-----

Made by
**Team API Avengers**

CareForAll Microservice Hackathon — November 21, 2025

Department of ETE, CUET

```
```