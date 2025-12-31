# 💰 ExpenseTracker Backend API

A Spring Boot REST API for personal finance management with JWT authentication, email verification, expense/income tracking, and financial analytics. Built with Spring Security, MySQL, and Flutter mobile frontend.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-red.svg)](https://jwt.io/)

> **Note:** This is a personal learning project built to understand full-stack development. While functional, it's designed for educational purposes and local use.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Demo](#demo)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Known Limitations](#known-limitations)
- [Future Improvements](#future-improvements)
- [Contributing](#contributing)
- [License](#license)

---

## ✨ Features

### ✅ Implemented

#### Authentication & Authorization
- 🔐 JWT-based stateless authentication
- 📧 Email verification with OTP during registration
- 🔑 BCrypt password hashing with salt
- 🚪 Secure login/logout flow
- ⏱️ Token expiration handling

#### Expense Management
- ➕ Create, read, update, and delete expenses
- 🏷️ Categorize expenses (Food, Transport, Shopping, etc.)
- 📅 Date-based expense tracking
- 💰 Amount tracking with decimal precision

#### Income Management
- 💵 Record income transactions
- 📈 Track income sources with categories
- 📊 Income history management

#### Statistics & Analytics
- 📊 Monthly expense breakdown by category (Pie charts)
- 📉 Yearly spending trends (Line & Bar charts)
- 💹 Income vs Expense comparison
- 📅 Dashboard with latest transactions
- 🔢 Min/Max expense tracking

#### User Profile
- 👤 View user profile information
- 📊 Access to all historical data
- 🎨 Dark/Light theme support (Flutter app)

#### Email Integration
- 📧 Welcome email on registration
- 🔐 OTP verification emails
- 📮 Brevo SMTP integration

---

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM framework
- **MySQL 8.0** - Relational database
- **HikariCP** - Database connection pooling
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **Brevo SMTP** - Email service
- **Maven** - Build & dependency management

### Frontend
- **Flutter** - Cross-platform mobile app (Android & iOS)
- **Dart** - Programming language

### Additional Libraries
- **Lombok** - Reduce boilerplate code
- **Jakarta Validation** - Input validation
- **Springdoc OpenAPI** - API documentation (Swagger)

---

## 📱 Demo

### Screenshots
> **Coming Soon**: Add screenshots of your Flutter app here
> - Login/Registration screen
> - Dashboard with charts
> - Expense list
> - Add expense form
> - Dark/Light mode comparison

### Video Demo
> **Coming Soon**: Add a short demo video (30-60 seconds) showing the app flow

---

## 📁 Project Structure

```
ExpenseTracker/
├── src/
│   ├── main/
│   │   ├── java/com/example/ExpenseTracker/
│   │   │   ├── Controller/           # REST API endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   ├── IncomeController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   └── StatsController.java
│   │   │   │
│   │   │   ├── DTO/                  # Data Transfer Objects
│   │   │   │   ├── ExpenseDTO.java
│   │   │   │   ├── GraphDTO.java
│   │   │   │   ├── IncomeDTO.java
│   │   │   │   └── StatsDTO.java
│   │   │   │
│   │   │   ├── Entity/               # Database entities
│   │   │   │   ├── Expense.java
│   │   │   │   ├── Income.java
│   │   │   │   ├── UserEntity.java
│   │   │   │   └── PendingRegistration.java
│   │   │   │
│   │   │   ├── Filter/               # Security filters
│   │   │   │   └── JwtRequestFilter.java
│   │   │   │
│   │   │   ├── IO/                   # Request/Response models
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── ProfileRequest.java
│   │   │   │   └── ResetPasswordRequest.java
│   │   │   │
│   │   │   ├── Repository/           # Data access layer
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   ├── IncomeRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── PendingRegistrationRepository.java
│   │   │   │
│   │   │   ├── Service/              # Business logic
│   │   │   │   ├── Stats/
│   │   │   │   │   ├── StatsService.java
│   │   │   │   │   └── StatsServiceImpl.java
│   │   │   │   ├── AppUserDetailsService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   ├── IncomeService.java
│   │   │   │   └── ProfileService.java
│   │   │   │
│   │   │   ├── SpringConfig/         # Configuration classes
│   │   │   │   ├── CustomAuthenticationEntryPoint.java
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   └── Util/                 # Utility classes
│   │   │       └── JwtUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties          # Production config
│   │       └── application-dev.properties      # Development config
│   │
│   └── test/                         # Unit tests (TODO)
│       └── java/com/example/ExpenseTracker/
│
├── .gitignore
├── pom.xml
└── README.md
```

---

## 📦 Prerequisites

Before running this application, ensure you have:

- **Java 17 or higher** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Git** - [Download](https://git-scm.com/downloads)
- **Brevo Account** (for email service) - [Sign up](https://www.brevo.com/)

---

## 🚀 Installation

### 1. Clone the Repositories

```bash
# Backend
git clone https://github.com/seshathri044/expense-tracker-backend.git
cd expense-tracker-backend

# Frontend (in a separate terminal)
git clone https://github.com/seshathri044/expense-tracker-frontend.git
```

### 2. Create MySQL Database

```sql
CREATE DATABASE expense_tracker;
```

### 3. Set Up Brevo SMTP (Email Service)

1. Create a free account at [Brevo](https://www.brevo.com/)
2. Go to **SMTP & API** section
3. Generate SMTP credentials
4. Verify your sender email address

### 4. Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database
export DB_URL="jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=Asia/Kolkata"
export DB_USER="root"
export DB_PASSWORD="your_mysql_password"

# JWT Secret (generate a strong secret)
export JWT_SECRET_KEY="your_jwt_secret_key_here"

# Email Configuration
export MAIL_USERNAME="your_brevo_smtp_username"
export MAIL_PASSWORD="your_brevo_smtp_password"
export MAIL_FROM="your_verified_email@example.com"

# Server
export SERVER_PORT="8080"
```

**Generate a secure JWT secret:**
```bash
openssl rand -base64 64
```

### 5. Update `application-dev.properties`

For local development, you can directly update the file:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

jwt.secret.key=YOUR_GENERATED_SECRET

spring.mail.username=YOUR_BREVO_USERNAME
spring.mail.password=YOUR_BREVO_PASSWORD
spring.mail.properties.mail.smtp.from=YOUR_EMAIL
```

### 6. Build the Project

```bash
mvn clean install
```

### 7. Run the Application

```bash
# Development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or production mode
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The backend will start on `http://localhost:8080/api`

### 8. Set Up Flutter Frontend

```bash
cd expense-tracker-frontend
flutter pub get
flutter run
```

---

## ⚙️ Configuration

### Application Profiles

#### Development (`application-dev.properties`)
- SQL logging enabled
- Detailed error messages
- Debug logging level

#### Production (`application.properties`)
- Uses environment variables
- Minimal logging
- Production-ready settings

### Context Path

All API endpoints are prefixed with `/api`:
- Login: `http://localhost:8080/api/login`
- Expenses: `http://localhost:8080/api/expenses`
- Stats: `http://localhost:8080/api/stats/dashboard`

### Database Configuration

**HikariCP Connection Pool Settings:**
- Maximum pool size: 10
- Minimum idle connections: 5
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Swagger UI
Access interactive API documentation:
```
http://localhost:8080/api/swagger-ui.html
```

---

### Authentication Endpoints

#### 1. Register User
```http
POST /api/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "message": "OTP sent to your email. Please verify.",
  "email": "user@example.com"
}
```

#### 2. Verify OTP
```http
POST /api/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "message": "Email verified successfully. You can now login."
}
```

#### 3. Login
```http
POST /api/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com"
}
```

#### 4. Logout
```http
POST /api/logout
Authorization: Bearer <your-jwt-token>
```

---

### Expense Endpoints

**All endpoints below require authentication:**
```http
Authorization: Bearer <your-jwt-token>
```

#### Create Expense
```http
POST /api/expenses
Content-Type: application/json

{
  "title": "Grocery Shopping",
  "amount": 1500.00,
  "date": "2024-12-31",
  "category": "Food",
  "description": "Weekly groceries"
}
```

#### Get All Expenses
```http
GET /api/expenses
```

#### Get Expense by ID
```http
GET /api/expenses/{id}
```

#### Update Expense
```http
PUT /api/expenses/{id}
Content-Type: application/json

{
  "title": "Updated Grocery",
  "amount": 1600.00,
  "date": "2024-12-31",
  "category": "Food",
  "description": "Updated description"
}
```

#### Delete Expense
```http
DELETE /api/expenses/{id}
```

---

### Income Endpoints

#### Create Income
```http
POST /api/income
Content-Type: application/json

{
  "title": "Monthly Salary",
  "amount": 50000.00,
  "date": "2024-12-01",
  "category": "Salary",
  "description": "December salary"
}
```

#### Get All Income
```http
GET /api/income
```

#### Update Income
```http
PUT /api/income/{id}
```

#### Delete Income
```http
DELETE /api/income/{id}
```

---

### Statistics Endpoints

#### Get Dashboard Statistics
```http
GET /api/stats/dashboard
```

**Response:**
```json
{
  "totalIncome": 50000.00,
  "totalExpense": 25000.00,
  "balance": 25000.00,
  "latestIncomes": [...],
  "latestExpenses": [...],
  "minExpense": 100.00,
  "maxExpense": 5000.00
}
```

#### Get Chart Data
```http
GET /api/stats/chart
```

Returns data for monthly expense breakdown by category.

---

### Profile Endpoints

#### Get User Profile
```http
GET /api/profile
```

#### Send Password Reset OTP
```http
POST /api/send-reset-otp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### Reset Password
```http
POST /api/reset-password
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePass123!"
}
```

---

## 🗄️ Database Schema

### User Entity
```sql
CREATE TABLE user_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Pending Registration
```sql
CREATE TABLE pending_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    otp_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Expense Entity
```sql
CREATE TABLE expense (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    date DATE NOT NULL,
    category VARCHAR(100),
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE CASCADE
);
```

### Income Entity
```sql
CREATE TABLE income (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    date DATE NOT NULL,
    category VARCHAR(100),
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE CASCADE
);
```

---

## 🔒 Security

### Authentication Flow

1. **Registration:**
   - User submits email and password
   - System generates OTP and sends to email
   - User data stored in `pending_registration` table
   
2. **OTP Verification:**
   - User enters OTP received via email
   - System validates OTP (expires after 10 minutes)
   - User moved to `user_entity` table with verified status
   
3. **Login:**
   - User submits credentials
   - System validates email verification status
   - BCrypt compares hashed passwords
   - JWT token generated and returned
   
4. **Protected Routes:**
   - Client includes JWT in Authorization header
   - `JwtRequestFilter` intercepts and validates token
   - User identity extracted from token
   - Request proceeds if valid

### Password Security

- **Hashing Algorithm:** BCrypt with automatic salt generation
- **Password Requirements:** 
  - Minimum 8 characters (recommended)
  - Mix of letters, numbers, symbols (recommended by users)
- **Storage:** Only hashed passwords stored in database
- **Reset:** Secure OTP-based password reset flow

### JWT Configuration

- **Algorithm:** HS256 (HMAC with SHA-256)
- **Token Expiration:** Configurable (default: 24 hours recommended)
- **Claims:** Contains user email and issued time
- **Secret:** Stored in environment variables (never committed to repo)

### CORS Policy

Currently configured to allow all origins for development:

```java
configuration.setAllowedOriginPatterns(List.of("*"));
```

**⚠️ For production, restrict to specific origins:**
```java
configuration.setAllowedOrigins(List.of(
    "https://your-frontend-domain.com"
));
```

---

## ⚠️ Known Limitations

### Testing
- ❌ No unit tests implemented yet
- ❌ No integration tests
- ❌ No test coverage reports

### API Features
- ⚠️ No pagination implemented (will have performance issues with large datasets)
- ⚠️ No filtering/sorting options for expenses and income lists
- ⚠️ No rate limiting (vulnerable to abuse)
- ⚠️ No API versioning

### Data Management
- ⚠️ Hard delete only (no soft delete for recovery)
- ⚠️ No data export (CSV, PDF, Excel)
- ⚠️ No data backup functionality
- ⚠️ No audit trail (who deleted what and when)

### Features
- ⚠️ No recurring expense tracking
- ⚠️ No budget alerts/notifications
- ⚠️ No multi-currency support
- ⚠️ No receipt/document upload
- ⚠️ No sharing expenses with family/friends
- ⚠️ No categories management (predefined only)

### Security
- ⚠️ CORS allows all origins (needs production configuration)
- ⚠️ No refresh token mechanism
- ⚠️ No device tracking
- ⚠️ No suspicious login alerts

### Infrastructure
- ⚠️ Not deployed to cloud
- ⚠️ No Docker containerization
- ⚠️ No CI/CD pipeline
- ⚠️ No monitoring/logging infrastructure

---

## 🎯 Future Improvements

### High Priority
- [ ] Add comprehensive unit tests (JUnit 5, Mockito)
- [ ] Implement pagination for all list endpoints
- [ ] Add proper API documentation with Swagger annotations
- [ ] Implement data export features (CSV, PDF)
- [ ] Add soft delete with recovery option
- [ ] Deploy to cloud (AWS/Heroku/Railway)

### Medium Priority
- [ ] Add refresh token mechanism
- [ ] Implement rate limiting (bucket4j/resilience4j)
- [ ] Add budget tracking and alerts
- [ ] Implement recurring expenses
- [ ] Add audit logging
- [ ] Create Docker containers
- [ ] Set up CI/CD with GitHub Actions

### Low Priority
- [ ] Multi-currency support
- [ ] Receipt upload functionality
- [ ] Expense sharing with other users
- [ ] Custom categories management
- [ ] Mobile push notifications
- [ ] Data visualization enhancements
- [ ] Export to accounting software

---

## 🧪 Testing

### Run Tests (When Implemented)

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Current Status:** ❌ Tests not yet implemented

---

## 🐛 Error Handling

### HTTP Status Codes

| Status | Description | When It Occurs |
|--------|-------------|----------------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Email already exists |
| 500 | Internal Server Error | Unexpected server error |

### Error Response Format

```json
{
  "timestamp": "2024-12-31T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/api/register"
}
```

---

## 📈 Performance

### Current Optimizations

- ✅ **HikariCP Connection Pooling** - Efficient database connection management
- ✅ **Stateless JWT** - No server-side session storage
- ✅ **Lazy Loading** - Hibernate lazy loading for relationships
- ✅ **Indexed Columns** - User email and foreign keys indexed

### Performance Limitations

- ⚠️ No caching implemented (Redis/Caffeine)
- ⚠️ No pagination (memory issues with large datasets)
- ⚠️ No database query optimization analysis
- ⚠️ No CDN for static assets

---

## 🚀 Deployment

### Local Deployment

Already covered in [Installation](#installation) section.

### Docker Deployment (Future)

```dockerfile
# Dockerfile (to be created)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/expense-tracker-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Cloud Deployment Options

**Not yet deployed, but compatible with:**
- AWS EC2 / Elastic Beanstalk
- Heroku
- Railway
- Google Cloud Run
- Azure App Service

---

## 🤝 Contributing

Contributions are welcome! This is a learning project, so feel free to:

- Report bugs
- Suggest new features
- Submit pull requests
- Improve documentation

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Contribution Guidelines

- Write clear commit messages
- Add comments for complex logic
- Update README if adding new features
- Test your changes locally before PR

---

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👨‍💻 Author

**Seshathri M**

- GitHub: [@seshathri044](https://github.com/seshathri044)
- LinkedIn: [Seshathri M](https://www.linkedin.com/in/seshathri-m/)
- Email: mseshathri507@gmail.com

**Frontend Repository:** [expense-tracker-frontend](https://github.com/seshathri044/expense-tracker-frontend)

---

## 🙏 Acknowledgments

- Spring Boot and Spring Security communities
- Flutter framework and Dart team
- MySQL documentation
- Brevo for email service
- Stack Overflow community
- YouTube tutorials and blog posts that helped during development

---

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Known Limitations](#known-limitations) section
2. Open an issue in the repository with detailed information
3. Email: mseshathri507@gmail.com

---

## 🎓 Learning Resources

If you're learning from this project, here are helpful resources:

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security with JWT](https://jwt.io/introduction)
- [Flutter Documentation](https://flutter.dev/docs)
- [MySQL Tutorial](https://dev.mysql.com/doc/)
- [REST API Best Practices](https://restfulapi.net/)

---

<div align="center">

### ⭐ If you find this project helpful for learning, consider starring it!

**Made with 💻 and ☕ as a learning project**

*This is an educational project built to understand full-stack development.*
*Not intended for production use without significant improvements.*

</div>
