# 💰 ExpenseTracker Backend API

> A production-ready Spring Boot REST API for personal finance management with JWT authentication, expense/income tracking, real-time analytics, and comprehensive CRUD operations. Built with Spring Security, MySQL, and modern best practices.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-red.svg)](https://jwt.io/)

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## ✨ Features

### Authentication & Authorization
- 🔐 JWT-based authentication
- 🔑 Secure password encryption
- 👤 User registration and login

### Expense Management
- ➕ Create, read, update, and delete expenses
- 🏷️ Categorize expenses
- 📊 Track spending patterns
- 📅 Date-based expense tracking

### Income Management
- 💵 Record income transactions
- 📈 Track income sources
- 📊 Income analytics

### Statistics & Analytics
- 📊 Financial statistics
- 📉 Spending trends
- 💹 Income vs Expense comparison
- 📅 Monthly/Yearly reports

### User Profile
- 👤 User profile management

## 🛠️ Tech Stack

### Backend Framework
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM framework

### Database
- **MySQL 8.0** - Primary database
- **HikariCP** - Connection pooling

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing

### Build Tool
- **Maven** - Dependency management

### Additional Libraries
- **Lombok** - Reduce boilerplate code
- **ModelMapper** - Object mapping
- **Jakarta Validation** - Input validation

## 📁 Project Structure

```
ExpenseTracker/
├── src/
│   ├── main/
│   │   ├── java/com/example/ExpenseTracker/
│   │   │   ├── Controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ExpenseController.java
│   │   │   │   ├── IncomeController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   └── StatsController.java
│   │   │   │
│   │   │   ├── DTO/
│   │   │   │   ├── ExpenseDTO.java
│   │   │   │   ├── GraphDTO.java
│   │   │   │   ├── IncomeDTO.java
│   │   │   │   └── StatsDTO.java
│   │   │   │
│   │   │   ├── Entity/
│   │   │   │   ├── Expense.java
│   │   │   │   ├── Income.java
│   │   │   │   └── UserEntity.java
│   │   │   │
│   │   │   ├── Filter/
│   │   │   │   └── JwtRequestFilter.java
│   │   │   │
│   │   │   ├── IO/
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── ProfileRequest.java
│   │   │   │   ├── ProfileResponse.java
│   │   │   │   └── ResetPasswordRequest.java
│   │   │   │
│   │   │   ├── Repository/
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   ├── IncomeRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   │
│   │   │   ├── Service/
│   │   │   │   ├── Stats/
│   │   │   │   │   ├── StatsService.java
│   │   │   │   │   └── StatsServiceImpl.java
│   │   │   │   ├── AppUserDetailsService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   ├── ExpenseServiceImpl.java
│   │   │   │   ├── IncomeService.java
│   │   │   │   ├── IncomeServiceImpl.java
│   │   │   │   ├── ProfileService.java
│   │   │   │   └── ProfileServiceImpl.java
│   │   │   │
│   │   │   ├── SpringConfig/
│   │   │   │   ├── CustomAuthenticationEntryPoint.java
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   └── Util/
│   │   │       └── JwtUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── static/
│   │       └── templates/
│   │
│   └── test/
│       └── java/com/example/ExpenseTracker/
│
├── .gitignore
├── pom.xml
└── README.md
```

## 📦 Prerequisites

Before running this application, ensure you have the following installed:

- **Java 17+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Git** - [Download](https://git-scm.com/downloads)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/expense-tracker-backend.git
cd expense-tracker-backend
```

### 2. Create MySQL Database

```sql
CREATE DATABASE expense_tracker;
```

### 3. Configure Application Properties

Update `src/main/resources/application.properties`:

```properties
# Application Name
spring.application.name=ExpenseTracker

# Database Configuration
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JPA/Hibernate Configuration
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret.key=YOUR_SECRET_KEY_HERE
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## ⚙️ Configuration

### Environment-Specific Properties

#### Development (`application-dev.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker
spring.jpa.show-sql=true
```

#### Production (`application-prod.properties`)
```properties
spring.datasource.url=jdbc:mysql://your-production-db:3306/expense_tracker
spring.jpa.show-sql=false
```

### JWT Configuration

Generate a secure secret key:

```bash
openssl rand -base64 64
```

Add to your properties file:
```properties
jwt.secret.key=<your-generated-key>
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "john@example.com",
  "username": "john_doe"
}
```

### Expense Endpoints

All expense endpoints require JWT authentication:
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
  "date": "2024-10-23",
  "category": "Food",
  "description": "Weekly groceries"
}
```

#### Get All Expenses
```http
GET /api/expenses?page=0&size=10
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
  "title": "Updated Grocery Shopping",
  "amount": 1600.00,
  "date": "2024-10-23",
  "category": "Food",
  "description": "Weekly groceries - updated"
}
```

#### Delete Expense
```http
DELETE /api/expenses/{id}
```

### Income Endpoints

#### Create Income
```http
POST /api/income
Content-Type: application/json

{
  "title": "Monthly Salary",
  "amount": 50000.00,
  "date": "2024-10-01",
  "category": "Salary",
  "description": "October salary"
}
```

#### Get All Income
```http
GET /api/income?page=0&size=10
```

#### Update Income
```http
PUT /api/income/{id}
Content-Type: application/json
```

#### Delete Income
```http
DELETE /api/income/{id}
```

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

### Profile Endpoints

#### Get User Profile
```http
GET /api/profile
```

#### Update Profile
```http
PUT /api/profile
Content-Type: application/json

{
  "username": "john_doe_updated",
  "email": "john.updated@example.com"
}
```

#### Reset Password
```http
POST /api/profile/reset-password
Content-Type: application/json

{
  "oldPassword": "OldPass123!",
  "newPassword": "NewPass123!"
}
```

## 🗄️ Database Schema

### User Entity
```sql
CREATE TABLE user_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    FOREIGN KEY (user_id) REFERENCES user_entity(id)
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
    FOREIGN KEY (user_id) REFERENCES user_entity(id)
);
```

## 🔒 Security

### Authentication Flow

1. User registers/logs in with credentials
2. Server validates credentials
3. Server generates JWT token
4. Client stores token (localStorage/sessionStorage)
5. Client includes token in Authorization header for subsequent requests
6. Server validates token and processes request

### Password Security

- Passwords are hashed using **BCrypt** algorithm
- Minimum password requirements enforced
- Secure password reset mechanism

### CORS Configuration

Configure allowed origins in `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Test Coverage

```bash
mvn clean test jacoco:report
```

## 🐛 Error Handling

The API uses standard HTTP status codes:

| Status Code | Description |
|------------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

### Error Response Format

```json
{
  "timestamp": "2024-10-23T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/expenses"
}
```

## 📈 Performance Optimization

- **Connection Pooling**: HikariCP for efficient database connections
- **Pagination**: Implemented for large datasets
- **Caching**: Strategic caching for frequently accessed data
- **Lazy Loading**: Hibernate lazy loading for optimal performance

## 🚀 Deployment

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/expense-tracker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t expense-tracker-backend .
docker run -p 8080:8080 expense-tracker-backend
```

### Cloud Deployment

The application is ready to deploy on:
- **AWS EC2**
- **Heroku**
- **Google Cloud Platform**
- **Azure**

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Write meaningful commit messages
- Add comments for complex logic
- Write unit tests for new features

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)
- Email: your.email@example.com

## 🙏 Acknowledgments

- Spring Boot Community
- JWT.io for authentication guidance
- MySQL Documentation
- Stack Overflow Community

## 📞 Support

For support, email your.email@example.com or open an issue in the repository.

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

Made with ❤️ and ☕

</div>
