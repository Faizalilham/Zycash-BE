# 💰 ZyCash - AI-Powered Financial Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Kafka](https://img.shields.io/badge/Kafka-Event%20Driven-orange.svg)](https://kafka.apache.org/)
[![Ollama](https://img.shields.io/badge/Ollama-llama3.2-red.svg)](https://ollama.ai/)

ZyCash adalah sistem manajemen keuangan berbasis AI yang menggunakan **Ollama (Llama 3.2)** untuk menganalisis transaksi secara otomatis. Aplikasi ini dibangun dengan arsitektur microservices menggunakan Spring Cloud dan event-driven architecture dengan Apache Kafka.

---

## 📋 Table of Contents

- [Arsitektur Aplikasi](#-arsitektur-aplikasi)
- [Teknologi Stack](#-teknologi-stack)
- [Event-Driven dengan Kafka](#-event-driven-dengan-kafka)
- [Struktur Microservices](#-struktur-microservices)
- [Prerequisites](#-prerequisites)
- [Setup & Installation](#-setup--installation)
- [API Documentation](#-api-documentation)
- [Monitoring](#-monitoring)

---

## 🏗️ Arsitektur Aplikasi

### High-Level Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                              │
│                                                                   │
│                    ┌─────────────────────┐                        │
│                    │  Android Mobile App │                        │
│                    └───────────┬─────────┘                        │
│                                │                                  │
└────────────────────────────────┼──────────────────────────────────┘
                                 │ HTTPS / REST
                                 ▼
┌────────────────────────────────────────────────────────────────────┐
│                     API GATEWAY (:8080)                            │
│  ├─ Load Balancing                                                 │
│  ├─ Authentication                                                 │
│  ├─ Rate Limiting                                                  │
│  └─ Request Routing                                                │
└────────────────────────────────┬───────────────────────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        ▼                        ▼                        ▼
┌───────────────┐   ┌──────────────────┐   ┌─────────────────┐
│ Auth Service  │   │ Transaction      │   │ Report Service  │
│ (:8082)       │   │ Service (:8081)  │   │ (:8083)         │
│               │   │                  │   │                 │
│ ┌───────────┐ │   │ ┌──────────────┐ │   │ ┌─────────────┐ │
│ │ JWT Token │ │   │ │ AI Analysis  │ │   │ │ Statistics  │ │
│ │ OAuth2    │ │   │ │ Category     │ │   │ │ Charts      │ │
│ └───────────┘ │   │ └──────────────┘ │   │ └─────────────┘ │
└───────┬───────┘   └────────┬─────────┘   └────────┬────────┘
        │                    │                      │
        ▼                    ▼                      ▼
┌───────────────┐   ┌──────────────────┐   ┌─────────────────┐
│ postgres-auth │   │ postgres-        │   │ postgres-report │
│ (:5433)       │   │ transaction      │   │ (:5435)         │
│               │   │ (:5432)          │   │                 │
└───────────────┘   └────────┬─────────┘   └─────────────────┘
                             │
                    ┌────────┴─────────┐
                    │                  │
                    ▼                  ▼
            ┌──────────────┐   ┌─────────────────┐
            │ Redis Cache  │   │ Ollama + SERP   │
            │ (:6379)      │   │ (:11434)        │
            │              │   │                 │
            │ ┌──────────┐ │   │ ┌─────────────┐ │
            │ │ Session  │ │   │ │ llama3.2:3b │ │
            │ │ Token    │ │   │ │ SERP API    │ │
            │ │ Rate Lmt │ │   │ └─────────────┘ │
            │ └──────────┘ │   └─────────────────┘
            └──────────────┘
                    
┌─────────────────────────────────────────────────────────────────────┐
│                      EVENT BUS (Kafka)                              │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐         │
│  │ transaction-   │  │ report-events  │  │ notification-  │         │
│  │ events         │  │                │  │ events         │         │
│  └────────────────┘  └────────────────┘  └────────────────┘         │
└─────────────────────────────────────────────────────────────────────┘
          │                      │                      │
          └──────────────────────┴──────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │ Notification      │    │ Report Service    │
        │ Service (:8084)   │    │ (:8083)           │
        │                   │    │                   │
        │ ┌───────────────┐ │    │ ┌───────────────┐ │
        │ │ Email         │ │    │ │ Statistics    │ │
        │ │ Push Notif    │ │    │ │ Reports       │ │
        │ └───────────────┘ │    │ └───────────────┘ │
        └───────┬───────────┘    └────────┬──────────┘
                ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │ postgres-         │    │ postgres-report   │
        │ notification      │    │ (:5435)           │
        │ (:5436)           │    │                   │
        └───────────────────┘    └───────────────────┘

```

### Data Flow Example: Create Transaction

```
1. Client → API Gateway
   POST /api/transaction/transactions
   {
     "text": "Beli nasi goreng di warteg 25000"
   }

2. Gateway → Transaction Service
   - Validate JWT token
   - Route to transaction-service

3. Transaction Service Processing:
   a. Check Redis Cache for similar patterns
   b. Call Ollama API for AI analysis
   c. Call SERP API for additional context
   d. Parse amount, category, description
   e. Save to PostgreSQL

4. Transaction Service → Kafka
   Publish event to 'transaction-events':
   {
     "eventType": "TRANSACTION_CREATED",
     "transactionId": 123,
     "userId": "user-456",
     "amount": 25000,
     "category": "Makanan",
     "timestamp": "2026-01-03T10:30:00Z"
   }

5. Kafka Consumers:
   a. Report Service → Update statistics & generate reports
   b. Notification Service → Send notification

6. Response to Client:
   {
     "id": 123,
     "category": "Makanan",
     "amount": 25000,
     "description": "Beli nasi goreng di warteg",
     "createdAt": "2026-01-03T10:30:00Z"
   }
```

---

## 🛠️ Teknologi Stack

### Backend
- **Spring Boot 3.2.0** - Core framework
- **Spring Cloud** - Microservices infrastructure
  - Config Server - Centralized configuration
  - Eureka - Service discovery
  - Gateway - API Gateway with load balancing
- **PostgreSQL** - Primary database
- **Redis** - Caching & session management
- **Apache Kafka** - Event streaming platform

### AI & ML
- **Ollama (Llama 3.2:3b)** - Local LLM for transaction analysis
- **SERP API** - Web search for additional context

### DevOps
- **Docker & Docker Compose** - Containerization
- **Gradle** - Build automation

---

## 📡 Event-Driven dengan Kafka

### Kafka Topics Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    Kafka Broker (:9092)                    │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Topic: transaction-events (partitions: 3)                 │
│  ├─ TRANSACTION_CREATED                                    │
│  ├─ TRANSACTION_UPDATED                                    │
│  ├─ TRANSACTION_DELETED                                    │
│  └─ TRANSACTION_CATEGORIZED                                │
│                                                            │
│  Topic: report-events (partitions: 2)                      │
│  ├─ DAILY_REPORT_GENERATED                                 │ 
│  ├─ MONTHLY_REPORT_GENERATED                               │
│  └─ BUDGET_ALERT_TRIGGERED                                 │
│                                                            │
│  Topic: notification-events (partitions: 2)                │
│  ├─ EMAIL_NOTIFICATION (soon)                              │ 
│  ├─ PUSH_NOTIFICATION                                      │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Event Flow: Transaction Created

```
Transaction Service (Producer)
            │
            │  Publish Event
            ▼
┌─────────────────────────────────────────┐
│ Topic: transaction-events               │
│ Event: TRANSACTION_CREATED              │
│                                         │
│ {                                       │
│   "eventId": "evt-123",                 │
│   "transactionId": 456,                 │
│   "userId": "usr-789",                  │
│   "amount": 150000,                     │
│   "category": "Utilities",              │
│   "timestamp": "2026-01-03T10:00"       │
│ }                                       │
└─────────────────────────────────────────┘
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
┌─────────────────┐   ┌──────────────────┐
│ Report Service  │   │ Notification     │
│                 │   │ Service          │
│ - Update stats  │   │ - Send email     │
│ - Generate      │   │ - Push notif     │
│   reports       │   │ - Log event      │
└─────────────────┘   └──────────────────┘

```

---

## 📦 Struktur Microservices

```
zycash-be/
├── 📁 be-config-server/          # Configuration Management
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/config/
│           │       └── ConfigServerApplication.java
│           └── resources/
│               ├── application.properties
│               └── configs/                # Git-based configs
│                   ├── gateway.yaml
│                   ├── auth.yaml
│                   ├── transaction.yaml
│                   ├── report.yaml
│                   └── notification.yaml
│
├── 📁 be-discovery/               # Service Registry (Eureka)
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/discovery/
│           │       └── DiscoveryServerApplication.java
│           └── resources/
│               └── application.yml
│
├── 📁 be-gateway/                 # API Gateway
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/gateway/
│
├── 📁 be-auth/                    # Authentication Service
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/auth/
│
├── 📁 be-transaction/             # Transaction Service (AI-Powered)
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/transaction/
│
├── 📁 be-report/                  # Report Service (Kafka Consumer)
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/report/
│
├── 📁 be-notification/            # Notification Service (Kafka Consumer)
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/zycash/notification/
│
├── 📄 docker-compose.yml          # Local Development
├── 📄 docker-compose.prod.yml     # Production Deployment
├── 📄 .env.example                # Environment Variables Template
├── 📄 build-push.bat              # Build & Push Script
├── 📄 .gitignore
└── 📄 README.md
```

### Service Responsibilities

| Service | Port | Database | Purpose | Kafka Role |
|---------|------|----------|---------|------------|
| **Config Server** | 8888 | - | Centralized configuration management | - |
| **Discovery** | 8761 | - | Service registry & discovery | - |
| **Gateway** | 8080 | - | API Gateway, routing, authentication | - |
| **Auth** | 8082 | postgres-auth | User authentication & authorization | - |
| **Transaction** | 8081 | postgres-transaction | AI-powered transaction management | Producer |
| **Report** | 8083 | postgres-report | Statistical reports & analytics | Consumer |
| **Notification** | 8084 | postgres-notification | Multi-channel notifications | Consumer |

---

## 📋 Prerequisites

### Development Environment
- Java 17+
- Gradle 8.x
- Docker & Docker Compose
- IntelliJ IDEA / VS Code
- Git

### Production Server (Ubuntu)
- Ubuntu 20.04 LTS or higher
- Docker 20.x+
- Docker Compose 2.x+
- Minimum 4GB RAM
- 20GB Storage

---

## 🚢 Deployment Guide

### Production Deployment ke Ubuntu Server

#### 1. Setup Server

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose -y
```

#### 2. Transfer Files

```bash
# manual create
mkdir ~/zycash-be
cd ~/zycash-be
nano docker-compose.yml
```

#### 3. Create .env File

```bash
nano .env
# Paste dan edit environment variables
```

#### 4. Deploy Services

```bash
# Pull images
docker-compose pull

# Start services
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs -f
```

#### 5. Setup Ollama Model

```bash
docker exec -it ollama ollama pull llama3.2:3b
docker exec -it ollama ollama list
```

#### 6. Verify Deployment

```bash
# Check all services
curl http://localhost:8888/actuator/health  # Config
curl http://localhost:8761                  # Eureka
curl http://localhost:8081/actuator/health  # Transaction
```

### Update Workflow

**Di Laptop:**
```bash
# Build & push ke Docker Hub
./build-push.bat
```

**Di Server:**
```bash
# Pull & restart
docker-compose pull
docker-compose up -d

# Atau specific service
docker-compose pull transaction-service
docker-compose up -d transaction-service
```

---

## 📊 Monitoring

### Health Checks

```bash
# All services
curl http://localhost:8080/actuator/health

# Specific service
curl http://localhost:8081/actuator/health
```

### Metrics

```bash
# JVM Metrics
curl http://localhost:8081/actuator/metrics

# Kafka Consumer Lag
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group report-service-group
```

### Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f transaction-service

# Last 100 lines
docker-compose logs --tail=100 transaction-service
```

### Service Discovery (Eureka)

Open browser: `http://13.211.208.46:8761/`

Akan menampilkan semua registered services.

---

**Faizalilham** - Developer