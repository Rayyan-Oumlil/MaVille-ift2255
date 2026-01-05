# MaVille - Public Works Management Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=spring)
![Next.js](https://img.shields.io/badge/Next.js-15.5.7-black?style=for-the-badge&logo=next.js)
![React](https://img.shields.io/badge/React-19.2.0-blue?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?style=for-the-badge&logo=typescript)

**Modern full-stack application for coordinating public works in Montreal**

[Features](#-features) • [Technologies](#-tech-stack) • [Installation](#-installation) • [Documentation](#-documentation)

</div>

---

## 📋 Description

**MaVille** is a comprehensive web platform developed to automate and improve public works management in Montreal. The application facilitates communication and coordination between three types of stakeholders:

- **Residents**: Report road issues, view public works, manage notifications
- **Service Providers**: View problems, submit applications, manage projects
- **STPM Agents**: Validate applications, manage priorities, supervise projects

### 🎯 Objectives

- ✅ Automate the issue reporting and work management process
- ✅ Improve communication between all stakeholders
- ✅ Provide a modern and intuitive interface
- ✅ Ensure traceability and transparency of operations
- ✅ Optimize performance with modern technologies

---

## 🚀 Features

### 🔐 Multi-Role Authentication

- **Secure system**: BCrypt password hashing
- **Three user types**: Residents (email), Service Providers (NEQ), STPM Agents
- **Persistent session**: Management with localStorage and Context API
- **Route protection**: Automatic redirection to `/login` if not authenticated
- **Adaptive menu**: Customized interface based on role

### 👥 For Residents

- ✅ **Problem reporting**: Complete form with location, work type, description
- ✅ **View public works**: Filters by neighborhood and type, advanced search
- ✅ **Notification management**: Personalized subscriptions (neighborhood, street, type)
- ✅ **Preferences**: Notification configuration (email, neighborhood, type)
- ✅ **Dashboard**: Overview with statistics and charts

### 🏢 For Service Providers

- ✅ **View problems**: List with advanced filters and pagination
- ✅ **Submit applications**: Complete form with dates, cost, description
- ✅ **Project management**: Status updates, view ongoing projects
- ✅ **Notifications**: Alerts for new problems matching criteria
- ✅ **Subscriptions**: Filters by neighborhood and work type

### 🏛️ For STPM Agents

- ✅ **Validate applications**: Accept/reject with automatic project creation
- ✅ **Manage priorities**: Assign priorities (LOW, MEDIUM, HIGH)
- ✅ **Supervision**: Overview of all problems, applications and projects
- ✅ **Notifications**: Alerts for new problems and applications
- ✅ **Administrative dashboard**: Complete statistics and activity charts

### 🔔 Real-Time Notification System

- ✅ **WebSocket STOMP**: Instant notifications (Spring Boot backend + frontend client)
- ✅ **Personalized subscriptions**: By neighborhood, street, or work type
- ✅ **Automatic notifications**: Project creation, status changes, priorities
- ✅ **Preference management**: Enable/disable by notification type
- ✅ **User interface**: Badge with notification count, real-time toasts

### 📊 Dashboard and Analytics

- ✅ **Real-time statistics**: Number of problems, applications, projects
- ✅ **Interactive charts**: Activity by period, distribution by type
- ✅ **Advanced filters**: Search, sort, pagination on all lists
- ✅ **Customized views**: Adapted to user role

---

## 🛠️ Tech Stack

### Backend

| Category | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.2.0 |
| **Database** | PostgreSQL | 15 (Docker) |
| **ORM** | Spring Data JPA / Hibernate | 3.2.0 |
| **WebSocket** | Spring WebSocket (STOMP) | 3.2.0 |
| **Validation** | Jakarta Validation | 3.0 |
| **Security** | Spring Security Crypto (BCrypt) | 6.2.0 |
| **Logging** | SLF4J + Logback | 1.4.11 |
| **API Documentation** | SpringDoc OpenAPI (Swagger) | 2.0.4 |
| **Tests** | JUnit 5, Mockito, Testcontainers | 5.9.3, 5.18.0, 1.19.3 |
| **Build** | Maven | 3.8+ |
| **HTTP Client** | OkHttp | 4.11.0 |

**Backend Architecture:**
- ✅ REST API with 14 synchronized endpoints
- ✅ Controllers with automatic validation (`@Valid`)
- ✅ Global error handling (`@ControllerAdvice`)
- ✅ DTOs for JSON serialization
- ✅ Business services with transactions (`@Transactional`)
- ✅ JPA Repositories with optimized queries (`@EntityGraph`)
- ✅ Spring Cache to improve performance
- ✅ WebSocket STOMP for real-time notifications

### Frontend

| Category | Technology | Version |
|-----------|------------|---------|
| **Framework** | Next.js | 15.5.7 |
| **Language** | TypeScript | 5.0 |
| **UI Library** | React | 19.2.0 |
| **Styling** | Tailwind CSS | 4.1.9 |
| **UI Components** | Radix UI / shadcn/ui | Latest |
| **State Management** | React Query | 5.62.0 |
| **WebSocket Client** | @stomp/stompjs + SockJS | 7.0.0, 1.6.1 |
| **Forms** | React Hook Form + Zod | 7.60.0, 3.25.76 |
| **Charts** | Recharts | Latest |
| **Toasts** | Sonner | 1.7.4 |
| **Monitoring** | Sentry | 10.29.0 |
| **E2E Tests** | Playwright | Latest |
| **Build** | Next.js (Turbopack) | 15.5.7 |

**Frontend Architecture:**
- ✅ App Router (Next.js 15)
- ✅ Server Components and Client Components
- ✅ React Query for caching and synchronization
- ✅ WebSocket client with automatic reconnection
- ✅ Reusable components (Upload, Comments)
- ✅ Production optimizations (code splitting, lazy loading)
- ✅ E2E tests with Playwright

### Infrastructure

- **Containerization**: Docker + Docker Compose
- **Database**: PostgreSQL 15 (production) / H2 (tests)
- **CI/CD**: GitHub Actions
- **Version Control**: Git
- **Documentation**: Swagger/OpenAPI, JavaDoc

---

## 📦 Installation

### Prerequisites

- **Java**: 21 or higher
- **Node.js**: 18 or higher
- **Maven**: 3.8 or higher
- **Docker**: For PostgreSQL (optional)
- **Git**: To clone the repository

### 🐳 Starting with Docker (Recommended)

**1. Clone the repository:**
```bash
git clone https://github.com/Rayyan-Oumlil/MaVille-ift2255.git
cd MaVille-ift2255
```

**2. Start PostgreSQL:**
```bash
# Option 1: Docker Compose
docker-compose up -d postgres

# Option 2: Docker run
docker run -d --name maville-postgres \
  -e POSTGRES_DB=maville \
  -e POSTGRES_USER=maville_user \
  -e POSTGRES_PASSWORD=maville_password \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine
```

**3. Start the backend:**
```bash
mvn spring-boot:run
```

The backend will be accessible at `http://localhost:7000/api`

**4. Start the frontend:**
```bash
cd frontend
npm install  # or pnpm install
npm run dev   # or pnpm dev
```

The frontend will be accessible at `http://localhost:3000`

### 📍 Application Access

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:7000/api
- **Swagger UI**: http://localhost:7000/swagger-ui.html
- **Login page**: http://localhost:3000/login

### 🔑 Test Data

**Resident:**
- Email: `marie@test.com`
- Password: `password123`

**Service Provider:**
- NEQ: `ABC123`
- Password: `password123`

**STPM Agent:**
- Username: `stpm1`
- Password: `password123`

---

## 🧪 Tests

### Backend Tests

**E2E Tests (5 complete scenarios):**
```bash
mvn test -Dtest=WorkflowE2ETest
```

**Unit tests:**
```bash
mvn test
```

**Results:**
- ✅ 5 E2E tests (complete workflows)
- ✅ 17 unit tests (models, services, storage)
- ✅ 31 integration tests (require Java 21)

### Frontend Tests

**E2E Tests with Playwright:**
```bash
cd frontend
npm run test:e2e          # Run all tests
npm run test:e2e:ui       # UI interface for debugging
npm run test:e2e:headed   # Visible mode (with browser)
```

**Available tests:**
- ✅ Authentication (login, errors)
- ✅ Dashboard (navigation, display)
- ✅ Problem reporting

---

## 📚 API Documentation

### Main Endpoints

**Authentication:**
- `POST /api/auth/login` - User login

**Residents:**
- `POST /api/residents/problemes` - Report a problem
- `GET /api/residents/travaux` - View public works
- `GET /api/residents/{email}/notifications` - Notifications
- `PUT /api/residents/{email}/preferences` - Update preferences

**Service Providers:**
- `GET /api/prestataires/problemes` - View available problems
- `POST /api/prestataires/candidatures` - Submit application
- `GET /api/prestataires/{neq}/projets` - Provider's projects

**STPM:**
- `GET /api/stpm/candidatures` - View applications (pagination)
- `PUT /api/stpm/candidatures/{id}/valider` - Validate/reject
- `PUT /api/stpm/problemes/{id}/priorite` - Change priority

**Complete documentation:** http://localhost:7000/swagger-ui.html

---

## 🏗️ Architecture

### Project Structure

```
MaVille-ift2255/
├── frontend/                 # Next.js application
│   ├── app/                  # Pages (App Router)
│   ├── components/           # React components
│   ├── hooks/                # Custom hooks
│   ├── lib/                  # Utilities and API client
│   ├── e2e/                  # Playwright E2E tests
│   └── package.json
│
├── src/
│   ├── main/java/ca/udem/maville/
│   │   ├── api/
│   │   │   ├── controller/   # REST controllers
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── exception/     # Error handling
│   │   │   └── service/       # API services
│   │   ├── config/            # Spring configuration
│   │   ├── entity/            # JPA entities
│   │   ├── repository/        # JPA repositories
│   │   └── service/           # Business services
│   └── test/                  # Tests
│
├── docs/                      # Documentation
├── docker-compose.yml         # Docker configuration
└── pom.xml                    # Maven configuration
```

### Patterns and Best Practices

- ✅ **REST Architecture**: Clear separation of responsibilities
- ✅ **DTOs**: Optimized data transfer
- ✅ **Validation**: Jakarta Validation with English messages
- ✅ **Error handling**: Centralized `@ControllerAdvice`
- ✅ **Transactions**: `@Transactional` for data integrity
- ✅ **Cache**: Spring Cache to improve performance
- ✅ **JPA Optimization**: `@EntityGraph` to avoid N+1 queries
- ✅ **Structured logging**: SLF4J + Logback with MDC
- ✅ **Tests**: E2E, unit, integration

---

## 🎯 Recent Improvements

### ✅ Backend (100% Complete)

- ✅ **PostgreSQL**: Complete migration with Docker
- ✅ **E2E Tests**: 5 complete workflow scenarios
- ✅ **Performance Optimization**: Spring Cache + JPA `@EntityGraph`
- ✅ **Error handling**: English messages, conditional stack traces
- ✅ **WebSocket STOMP**: Complete infrastructure for real-time notifications
- ✅ **Validation**: DTOs with `@Valid` and custom messages
- ✅ **API Documentation**: Swagger/OpenAPI fully enabled
- ✅ **Logging**: Structured with MDC and JSON support

### ✅ Frontend (100% Complete)

- ✅ **React Query**: Complete migration of all components (14/14)
- ✅ **WebSocket Client**: STOMP with automatic reconnection
- ✅ **E2E Tests**: Playwright with 3 test files
- ✅ **File Upload**: Component with drag & drop
- ✅ **Comments**: Reusable component
- ✅ **Production Build**: Complete optimizations
- ✅ **Monitoring**: Sentry configured (optional)

---

## 📈 Project Statistics

- **Backend**: 100% functional and optimized
- **Frontend**: 100% complete and optimized
- **Synchronization**: 14/14 endpoints synchronized
- **E2E Tests**: 5 backend scenarios + 3 frontend files
- **Performance**: Cache + JPA optimizations + React Query
- **Documentation**: Complete Swagger + JavaDoc

---

## 👥 Team

- **Younes Lagha** - Development and testing
- **Rayyan Oumlil** - Documentation and UML diagrams
- **Karim Omairi** - Interface and architecture

---

## 📄 License

This project was developed as part of the IFT2255 (Software Engineering) course at Université de Montréal.

---

## 🔗 Useful Links

- **Swagger UI**: http://localhost:7000/swagger-ui.html
- **API Docs JSON**: http://localhost:7000/v3/api-docs
- **PostgreSQL Documentation**: [DOCKER_POSTGRES.md](DOCKER_POSTGRES.md)
- **Next Steps**: [docs/NEXT_STEPS.md](docs/NEXT_STEPS.md)

---

<div align="center">

**🎉 Project 100% complete and functional**

*Last updated: December 2025*

</div>
