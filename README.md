# 📦 Inventory Management System API

A backend-heavy **Spring Boot + PostgreSQL** REST API for managing warehouse inventory.  
It supports secure authentication, product CRUD operations, stock management, image upload, and low-stock monitoring.

---

## 🚀 Features

- **JWT-based Authentication** (Login, Signup, Google OAuth optional)
- **Product CRUD**: Create, Read, Update, Delete
- **Stock Management**: Increase/Decrease with validation
- **Low Stock Alerts**: Products below `lowStockThreshold`
- **Image Upload** for products
- **Centralized Error Handling** (400/404/409 JSON responses)
- **PostgreSQL** persistence

---

## 🛠️ Tech Stack

- **Java 22**
- **Spring Boot 3.5.x**
- **Spring Security 6 + JWT**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL**
- **Lombok**
- **Maven**

---

## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/yourusername/inventory-store.git
cd inventory-store
```
### 2️⃣ Setup PostgreSQL Database
```sql
CREATE DATABASE inventory_store;
```
### 3️⃣ Configure Database
```yml
Update src/main/resources/application.yml:

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_store
    username: your_db_user
    password: your_db_pass
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect
server:
  port: 8080
```
### 4️⃣ Build and Run
```bash
mvn clean install
mvn spring-boot:run
```
## Server will run at:
👉 http://localhost:8080

## 🧪 Testing with Postman (Step-by-Step)

1) Signup → POST /api/auth/signup
2) Login → POST /api/auth/login → copy token
3) Add header → Authorization: Bearer <token>
4) Create product → POST /api/products
5) List products → GET /api/products
6) Increase stock → POST /api/products/{id}/increase?qty=10
7) Decrease stock → POST /api/products/{id}/decrease?qty=50 (fails if below threshold)
8) Low stock → GET /api/products/low-stock
