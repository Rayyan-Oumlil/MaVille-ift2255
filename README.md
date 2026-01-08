# MaVille - Public Works Management Platform

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Next.js](https://img.shields.io/badge/Next.js-15-black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

Modern full-stack application for coordinating public works in Montreal.

---

## Description

**MaVille** is a web platform for managing public works in Montreal. The application facilitates communication between residents, service providers, and STPM agents.

### Key Features

- Multi-role authentication (Residents, Service Providers, STPM Agents)
- Problem reporting and tracking
- Real-time notifications via WebSocket
- Project management and application validation
- Integration with Montreal Open Data API

---

## Tech Stack

### Backend
- **Java 21** with Spring Boot 3.2.0
- **PostgreSQL 15** with Spring Data JPA
- **WebSocket STOMP** for real-time notifications
- **Swagger/OpenAPI** for API documentation
- **Docker** for containerization

### Frontend
- **Next.js 15** with TypeScript
- **React 19** with Tailwind CSS
- **React Query** for state management
- **Radix UI** components
- **Playwright** for E2E testing

---

## Installation

### Prerequisites
- Java 21+
- Node.js 18+
- Docker (for PostgreSQL)

### Quick Start

**1. Start PostgreSQL:**
```bash
docker-compose up -d postgres
```

**2. Start Backend:**
```bash
mvn spring-boot:run
```
Backend available at `http://localhost:7000/api`

**3. Start Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Frontend available at `http://localhost:3000`

### Access Points
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:7000/api
- **Swagger UI**: http://localhost:7000/swagger-ui.html

### Test Credentials

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

## API Documentation

### Main Endpoints

**Authentication:**
- `POST /api/auth/login` - User login

**Residents:**
- `POST /api/residents/problemes` - Report a problem
- `GET /api/residents/travaux` - View public works
- `GET /api/residents/{email}/notifications` - Get notifications

**Service Providers:**
- `GET /api/prestataires/problemes` - View available problems
- `POST /api/prestataires/candidatures` - Submit application
- `GET /api/prestataires/{neq}/projets` - View projects

**STPM:**
- `GET /api/stpm/candidatures` - View applications
- `PUT /api/stpm/candidatures/{id}/valider` - Validate/reject application
- `PUT /api/stpm/problemes/{id}/priorite` - Update priority

**Full documentation:** http://localhost:7000/swagger-ui.html

---

## Testing

### Backend Tests
```bash
mvn test
```

### Frontend E2E Tests
```bash
cd frontend
npm run test:e2e
```

---

## Architecture

```
MaVille-ift2255/
├── frontend/                 # Next.js application
│   ├── app/                  # Pages (App Router)
│   ├── components/           # React components
│   ├── hooks/                # Custom hooks
│   └── lib/                  # API client and utilities
│
├── src/main/java/ca/udem/maville/
│   ├── api/
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   └── service/          # API services
│   ├── config/               # Spring configuration
│   ├── entity/               # JPA entities
│   ├── repository/           # JPA repositories
│   └── service/              # Business services
│
├── docker-compose.yml        # Docker configuration
└── pom.xml                   # Maven configuration
```

---

## Deployment

### Production Stack
- **Backend**: Google Cloud Run (auto-scaling)
- **Database**: Supabase PostgreSQL (managed)
- **Frontend**: Vercel (Next.js hosting)

### Cloud Run scaling (important)

- **Why you sometimes see “3 containers/instances”**:
  - **Local Docker**: `docker-compose.yml` in this repo only runs **PostgreSQL** (`maville-postgres`). If you saw ~3 containers locally, it was likely from other `docker run` / older compose projects (or you were running frontend/backend as containers separately).
  - **Cloud Run**: you’re seeing **instances** (Cloud Run spins up multiple copies of your container) when traffic/concurrency requires it (or because long-lived connections like WebSocket can tie up an instance).

- **Cap the number of instances** (recommended if you want max 1 or 2):
  - Using gcloud:

```bash
# max 1 instance, allow scale-to-zero
gcloud run services update maville-backend \
  --region YOUR_REGION \
  --min-instances 0 \
  --max-instances 1

# or allow up to 2 instances
gcloud run services update maville-backend \
  --region YOUR_REGION \
  --min-instances 0 \
  --max-instances 2
```

- **Note about WebSocket vs SSE**:
  - WebSocket connections are long-lived and can prevent scale-to-zero while users are connected.
  - This project supports **SSE** for notifications (better fit for Cloud Run). The frontend defaults to SSE for that reason.

### Environment Variables

**Backend:**
- `DATABASE_URL` - PostgreSQL connection string
- `DATABASE_USER` - Database username
- `DATABASE_PASSWORD` - Database password
- `CORS_ORIGINS` - Allowed origins

**Frontend:**
- `NEXT_PUBLIC_API_URL` - Backend API URL
- `NEXT_PUBLIC_WS_URL` - WebSocket URL

---

## Team

- **Younes Lagha** - Backend development
- **Rayyan Oumlil** - Documentation and architecture
- **Karim Omairi** - Frontend development

---

## License

Academic project developed for IFT2255 (Software Engineering) at Université de Montréal.

---

**Last updated: January 2026**
