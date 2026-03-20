<div align="center">

# ExpenseTracker

**Full-stack personal finance management — Spring Boot · Flutter · AWS EC2**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Flutter](https://img.shields.io/badge/Flutter-3.x-02569B?logo=flutter&logoColor=white)](https://flutter.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws&logoColor=white)](https://aws.amazon.com/ec2/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)](https://github.com/features/actions)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

[![Download APK](https://img.shields.io/github/v/release/seshathri044/expense-tracker-backend?label=Download%20APK&logo=android&logoColor=white&color=3DDC84)](https://github.com/seshathri044/expense-tracker-backend/releases/latest)

> **Frontend Repository**: [expense-tracker-frontend](https://github.com/seshathri044/expense-tracker-frontend)


</div>

---

## Overview

ExpenseTracker is a production-deployed personal finance application. The backend is a Spring Boot REST API secured with JWT authentication, backed by MySQL, containerised with Docker, and running on AWS EC2. The Flutter mobile client connects to the live server over HTTPS, supporting income/expense tracking, real-time analytics, and user profile management — all with dark and light mode.

---

## System Architecture
<img width="1536" height="1024" alt="architecture" src="https://github.com/user-attachments/assets/a5ed7a57-370d-4768-a3fa-4d5ff85cd37d" />

---

## CI/CD Pipeline & Testing
<img width="1536" height="1024" alt="CI-CD" src="https://github.com/user-attachments/assets/e7ae9e5d-ae94-4a3c-8992-ab8875454cc1" />

Every push to `main` triggers a GitHub Actions workflow: the JAR is built, copied to EC2 via SSH, the old container is stopped, a new Docker image is built, and the container is restarted — with zero manual steps.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 3.x, Java 21 |
| Security | Spring Security, JWT, BCrypt |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Email | Brevo SMTP |
| Frontend | Flutter 3.x (Android) |
| Containerisation | Docker |
| Cloud | AWS EC2 (Ubuntu) |
| CI/CD | GitHub Actions |
| Testing | JUnit 5, Mockito, MockMvc |
| Build | Maven |

---

## Features

### Authentication
- Registration with OTP email verification — unverified users are held in memory only, never persisted until OTP is confirmed
- JWT-based stateless authentication on all protected routes
- Password reset via OTP email
- BCrypt password hashing

### Expense & Income Management
- Full CRUD for expenses and incomes, scoped strictly to the authenticated user
- Category tagging, date tracking, descriptions

### Analytics
- Monthly category breakdown
- Yearly reports
- Top spending categories
- Chart data for pie, bar, and line charts

### Mobile App
- Home dashboard: balance, top 3 spending categories, last 3 transactions
- Add, view, edit, and delete income and expenses
- Profile with yearly statistics charts
- Dark and light mode

---

## API Reference

### Base URL
```
http://localhost:8080
```

All protected endpoints require:
```
Authorization: Bearer <jwt-token>
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register and trigger OTP email |
| POST | `/api/auth/verify-otp` | Confirm OTP, persist user |
| POST | `/api/auth/login` | Authenticate, receive JWT |

**Register**
```json
POST /api/auth/register
{
  "username": "seshathri",
  "email": "seshathri686@gmail.com",
  "password": "SecurePass123!"
}
```

**Login response**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "seshathri686@gmail.com",
  "username": "seshathri"
}
```

### Expenses

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/expenses` | Create expense |
| GET | `/api/expenses?page=0&size=10` | Paginated list |
| GET | `/api/expenses/{id}` | Get by ID |
| PUT | `/api/expenses/{id}` | Update |
| DELETE | `/api/expenses/{id}` | Delete |

**Create expense**
```json
{
  "title": "Grocery Shopping",
  "amount": 1500.00,
  "date": "2024-10-23",
  "category": "Food",
  "description": "Weekly groceries"
}
```

### Income

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/income` | Create income |
| GET | `/api/income?page=0&size=10` | Paginated list |
| PUT | `/api/income/{id}` | Update |
| DELETE | `/api/income/{id}` | Delete |

### Statistics

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/stats/dashboard` | Balance, min/max, latest transactions |
| GET | `/api/stats/chart` | Data for pie, bar, line charts |

**Dashboard response**
```json
{
  "totalIncome": 50000.00,
  "totalExpense": 25000.00,
  "balance": 25000.00,
  "latestIncomes": [],
  "latestExpenses": [],
  "minExpense": 100.00,
  "maxExpense": 5000.00
}
```

### Profile

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/profile` | Get profile |
| PUT | `/api/profile` | Update username / email |
| POST | `/api/profile/reset-password` | Change password via OTP |

---

## Database Schema

```sql
CREATE TABLE user_entity (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE expense (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    amount      DECIMAL(19,2) NOT NULL,
    date        DATE NOT NULL,
    category    VARCHAR(100),
    description TEXT,
    user_id     BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_entity(id)
);

CREATE TABLE income (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    amount      DECIMAL(19,2) NOT NULL,
    date        DATE NOT NULL,
    category    VARCHAR(100),
    description TEXT,
    user_id     BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_entity(id)
);
```

> Tables are created manually. Hibernate auto-DDL is disabled in production due to a dialect compatibility issue with Spring Boot 3 and Hibernate 6.

---

## Local Setup

### Prerequisites
- Java 21+
- Maven 3.6+
- MySQL 8.0+

### Steps

```bash
# 1. Clone
git clone https://github.com/seshathri044/expense-tracker-backend.git
cd expense-tracker-backend

# 2. Create database
mysql -u root -p -e "CREATE DATABASE expense_tracker;"

# 3. Configure environment
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
# Edit datasource URL, credentials, and JWT secret

# 4. Build and run
mvn clean install
mvn spring-boot:run
```

Application starts at `http://localhost:8080`.

### Generate JWT secret

```bash
openssl rand -base64 64
```

### application.properties (minimum)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

jwt.secret.key=YOUR_GENERATED_KEY

spring.mail.host=smtp-relay.brevo.com
spring.mail.username=YOUR_BREVO_EMAIL
spring.mail.password=YOUR_BREVO_SMTP_KEY
```

---

## Deployment

The application runs in a Docker container on an AWS EC2 Ubuntu instance. The MySQL database runs on the EC2 host. Disk was expanded to 20 GB to accommodate Docker images and logs.

```⚠️ Note: This server is hosted on AWS EC2 Free Tier. The free tier period ends in ~6 months, after which the server may be taken offline or migrated.```

### Dockerfile

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/expense-tracker-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Run container (production)

```bash
docker build -t expense-tracker .
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_PASSWORD=$DB_PASSWORD \
  -e JWT_SECRET=$JWT_SECRET \
  -e MAIL_PASSWORD=$MAIL_PASSWORD \
  --name expense-tracker \
  expense-tracker
```

### CI/CD — GitHub Actions

The pipeline triggers on every push to `main`:

1. Build JAR with Maven
2. Copy JAR to EC2 via SCP
3. SSH into EC2 — stop old container, build new image, start container

All secrets (DB password, JWT key, SSH key, email credentials) are stored in GitHub repository secrets and injected at runtime — nothing is hardcoded.

---

## Testing

### Unit tests — `ExpenseService`
- Uses Mockito to mock the repository layer
- No real database required
- Covers: create, read, update, delete, ownership validation

### Integration tests — `ExpenseController`
- Uses MockMvc to simulate full HTTP request/response cycles
- JWT tokens are generated and included in test requests
- Covers: success paths, validation errors, unauthorized access (401), resource not found (404)

```bash
# Run all tests
mvn test

# With coverage report
mvn clean test jacoco:report
```

---

## Security

- All sensitive configuration is injected via environment variables — no secrets in source control
- Every API request passes through the `JwtRequestFilter` before reaching any controller
- User data isolation: every repository query is scoped by `userId` extracted from the JWT
- CORS is configured globally in `SecurityConfig` — only the Flutter app's origin is whitelisted in production
- Passwords are hashed with BCrypt; the raw password is never stored or logged

---

## Error Responses

```json
{
  "timestamp": "2024-10-23T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'amount'",
  "path": "/api/expenses"
}
```

| Code | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 400 | Validation error |
| 401 | Missing or invalid JWT |
| 403 | Forbidden (wrong user) |
| 404 | Resource not found |
| 500 | Internal server error |

---

## Project Structure

```
ExpenseTracker/
├── src/main/java/com/example/ExpenseTracker/
│   ├── Controller/          # REST controllers
│   ├── DTO/                 # Data transfer objects
│   ├── Entity/              # JPA entities
│   ├── Filter/              # JWT request filter
│   ├── IO/                  # Auth and profile request/response models
│   ├── Repository/          # Spring Data repositories
│   ├── Service/             # Business logic (interface + impl)
│   ├── SpringConfig/        # Security and CORS configuration
│   └── Util/                # JWT utility
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-prod.properties
├── src/test/                # JUnit + Mockito + MockMvc tests
├── .github/workflows/       # GitHub Actions CI/CD
├── Dockerfile
└── pom.xml
```

---

## Mobile App

The Flutter frontend connects to the live EC2 server. An installable APK is available on the Releases page.

[![Download APK](https://img.shields.io/github/v/release/seshathri044/expense-tracker-backend?label=Download%20APK&logo=android&logoColor=white&color=3DDC84)](https://github.com/seshathri044/expense-tracker-backend/releases/latest)

---

## Author

**Seshathri M**
[LinkedIn](https://www.linkedin.com/in/seshathri-m/) · [GitHub](https://github.com/seshathri044) · seshathri686@gmail.com

---

## License

[MIT](LICENSE)
