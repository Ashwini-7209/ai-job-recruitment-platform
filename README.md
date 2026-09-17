# HireFlow — AI-Powered Job & Recruitment Platform

A full-stack AI-powered job and recruitment platform connecting candidates with opportunities through intelligent matching, resume analysis, and recruitment automation.

## Features

### Candidate Portal
- JWT authentication with role-based access
- Profile management (skills, experience, education, links)
- Resume upload and parsing (PDF, DOCX, TXT)
- AI-powered job matching with scoring
- Skill gap analysis and career insights
- Resume improvement suggestions
- Job search with advanced filters (location, salary, skills, employment type)
- Save/bookmark jobs and create job alerts
- Application tracking with status timeline
- Interview schedule viewer
- Application analytics and trends

### Recruiter Portal
- Recruiter/company profile management
- Job creation, publishing, and management
- AI-generated job descriptions
- Application review with status updates and notes
- Candidate search by skills, experience, location
- AI-powered match scoring per application
- Interview scheduling and management
- Recruitment funnel and hiring analytics

### Admin Portal
- User management (search, enable/disable, role changes)
- Job moderation (approve, close, delete)
- Application oversight across platform
- Platform analytics (user growth, job stats, application trends)
- Audit log viewer
- Dashboard with key metrics

### AI Features (Optional)
- **Job Matching**: Rule-based + AI-enhanced matching with scoring (0-100)
- **Skill Gap Analysis**: Identifies missing skills for job requirements
- **Career Insights**: Personalized recommendations based on profile
- **Resume Improvement**: AI-powered resume analysis and suggestions
- **Job Description Generation**: AI drafts descriptions from title and skills
- All AI features have deterministic fallbacks when OpenAI API is unavailable

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Backend | Java + Spring Boot | 21 / 3.4.1 |
| Frontend | React + TypeScript | 19 / 6.0 |
| Build (FE) | Vite | 8.2.2 |
| Styling | Tailwind CSS | 4.3.3 |
| Database | MySQL | 8.0 |
| ORM | Spring Data JPA + Hibernate | — |
| Migrations | Flyway | 19 versions (V1–V19) |
| Auth | Spring Security + JWT | — |
| AI | OpenAI API (optional) | gpt-4o-mini |
| HTTP Client | Axios | 1.20 |
| Routing | React Router | 7.18 |
| Testing | JUnit 5 + Mockito + MockMvc | — |

## Architecture

```
┌──────────────────────────────────────────────────┐
│                   Frontend                       │
│         React + TypeScript (Vite)                │
│   Pages: Landing | Auth | Candidate | Recruiter  │
│          | Admin | Design System                  │
│   Services: API Client (Axios + JWT)             │
├──────────────────────────────────────────────────┤
│               REST API (JSON)                    │
│          Nginx reverse proxy (/api)              │
├──────────────────────────────────────────────────┤
│                  Backend                         │
│            Spring Boot (Java 21)                 │
│                                                  │
│  Modules: auth | user | candidate | recruiter    │
│           job | application | resume | interview  │
│           notification | savedjob | jobalert      │
│           analytics | matching | ai | admin       │
│                                                  │
│  Security: JWT + @PreAuthorize role checks       │
│  Database: Flyway migrations (V1–V19)            │
├──────────────────────────────────────────────────┤
│                 MySQL Database                   │
│             22 tables, indexed                    │
└──────────────────────────────────────────────────┘
```

## Database

- **Engine**: MySQL 8.0
- **Migrations**: 19 Flyway versions (V1–V19)
- **Tables**: users, candidate_profiles, recruiter_profiles, jobs, applications, resumes, interviews, notifications, notification_preferences, saved_jobs, job_alerts, audit_logs, and join/history tables
- **Features**: Proper foreign keys, indexes for search performance, duplicate cleanup (V19)
- **Seeding**: Demo data via `DataSeeder` (disabled in test profile, `@Profile("!test")`)

## Local Setup

### Prerequisites
- Java 21+ (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Node.js 20+ LTS (`node -v`)
- MySQL 8.0+ (or use Docker)
- Docker + Docker Compose (optional, for containerized setup)

### Option 1: Docker (Recommended)

```bash
# Clone and configure
cp .env.example .env
# Edit .env with your settings (at minimum, set a strong JWT_SECRET)

# Start all services
docker compose up -d

# View logs
docker compose logs -f
```

Services:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- MySQL: internal (not exposed publicly)

### Option 2: Manual Setup

```bash
# 1. Start MySQL (or use Docker for just MySQL)
docker compose up mysql -d

# 2. Configure environment
cp .env.example .env
# Edit .env — set DB credentials, JWT_SECRET

# 3. Build and run backend
cd backend
mvn clean package
java -jar target/job-recruitment-platform-0.1.0-SNAPSHOT.jar

# 4. In a separate terminal — run frontend
cd frontend
npm install
npm run dev
```

### Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Candidate | `candidate@demo.com` | `password123` |
| Recruiter | `recruiter@demo.com` | `password123` |
| Admin | `admin@demo.com` | `password123` |

## Environment Variables

Copy `.env.example` to `.env` and configure:

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/job_platform` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | (empty) |
| `DB_ROOT_PASSWORD` | MySQL root password | `rootpassword` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | dev-only default |
| `JWT_EXPIRATION_MS` | Token expiration (ms) | `86400000` (24h) |
| `AI_API_KEY` | OpenAI API key (optional) | (empty — uses fallbacks) |
| `AI_MODEL` | AI model name | `gpt-4o-mini` |
| `SERVER_PORT` | Backend port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:3000` |
| `VITE_API_BASE_URL` | Frontend API base URL | `/api` |

**Production**: Generate a secure JWT secret with `openssl rand -base64 48`.

## API Overview

The backend exposes **103 REST endpoints** across 26 controllers. See [docs/api.md](docs/api.md) for complete documentation.

### Key Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/health` | Public | Health check |
| GET | `/api/jobs` | Public | Search published jobs |
| GET | `/api/jobs/{id}` | Public | Get job details |
| GET | `/api/candidates/me/profile` | CANDIDATE | Get profile |
| POST | `/api/candidates/me/resumes` | CANDIDATE | Upload resume |
| GET | `/api/candidates/me/recommendations` | CANDIDATE | AI job recommendations |
| GET | `/api/candidates/me/jobs/{id}/match` | CANDIDATE | AI match score |
| POST | `/api/recruiter/jobs` | RECRUITER | Create job |
| GET | `/api/recruiters/me/analytics/summary` | RECRUITER | Analytics |
| GET | `/api/admin/users` | ADMIN | Manage users |
| GET | `/api/admin/analytics/summary` | ADMIN | Platform analytics |

### Swagger UI

When the backend is running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Docs: `http://localhost:8080/api-docs`

## Testing

### Backend (536 tests)

```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SecurityAuthorizationTests

# Run with coverage
mvn test jacoco:report
```

### Frontend

```bash
cd frontend

# TypeScript check
npx tsc --noEmit

# Lint
npm run lint

# Production build (includes TypeScript check)
npm run build
```

## Production Deployment

### Docker Production

```bash
# 1. Configure production environment
cp .env.production.example .env.production
# Edit .env.production with real credentials

# 2. Build and start
docker compose --env-file .env.production up -d --build

# 3. Verify
docker compose ps
docker compose logs backend
curl http://localhost:8080/api/health
```

### Manual Production Build

```bash
# Backend
cd backend
mvn clean package -DskipTests
java -jar target/job-recruitment-platform-0.1.0-SNAPSHOT.jar

# Frontend
cd frontend
npm run build
# Deploy frontend/dist/ to your static host
```

### Production Checklist

- [ ] Set strong `JWT_SECRET` (min 32 chars)
- [ ] Set strong database passwords
- [ ] Configure `CORS_ALLOWED_ORIGINS` for your domain
- [ ] Enable HTTPS (reverse proxy / load balancer)
- [ ] Disable MySQL public access
- [ ] Verify `SPRING_PROFILES_ACTIVE=prod`
- [ ] Set `AI_API_KEY` if using AI features
- [ ] Monitor `/api/health` endpoint
- [ ] Review backend logs for errors

## Project Structure

```
job-portal/
├── backend/
│   ├── src/main/java/com/jobplatform/
│   │   ├── auth/           # Authentication (login, register, JWT)
│   │   ├── admin/          # Admin management (users, jobs, audit)
│   │   ├── analytics/      # Analytics (candidate, recruiter, admin)
│   │   ├── application/    # Application management
│   │   ├── candidate/      # Candidate profiles
│   │   ├── config/         # Security, CORS, JPA, data seeding
│   │   ├── exception/      # Global error handling
│   │   ├── health/         # Health check endpoint
│   │   ├── interview/      # Interview scheduling
│   │   ├── job/            # Job CRUD and search
│   │   ├── jobalert/       # Job alert subscriptions
│   │   ├── matching/       # AI matching engine
│   │   ├── notification/   # Notifications and preferences
│   │   ├── recruiter/      # Recruiter profiles
│   │   ├── resume/         # Resume upload and parsing
│   │   ├── savedjob/       # Saved/bookmarked jobs
│   │   └── security/       # JWT filter and utilities
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/   # V1–V19 Flyway migrations
│   └── src/test/java/      # 536 tests
├── frontend/
│   ├── src/
│   │   ├── components/     # Reusable UI components
│   │   │   ├── admin/      # Admin sidebar, layout
│   │   │   ├── auth/       # Auth layout, password input
│   │   │   ├── candidate/  # Candidate sidebar
│   │   │   ├── landing/    # Landing page components
│   │   │   ├── layout/     # AppShell, Sidebar, TopBar
│   │   │   ├── notifications/
│   │   │   ├── recruiter/  # Recruiter sidebar, layout
│   │   │   └── ui/         # Button, Card, Badge, Input, etc.
│   │   ├── hooks/          # Custom React hooks
│   │   ├── layouts/        # Page layouts
│   │   ├── pages/          # Route pages (auth, candidate, recruiter, admin)
│   │   ├── routes/         # AppRouter
│   │   ├── services/       # API service layer
│   │   ├── styles/         # Shared styles
│   │   ├── types/          # TypeScript type definitions
│   │   └── utils/          # Utility functions
│   ├── nginx.conf          # Nginx config for Docker
│   └── vite.config.ts
├── docs/
│   ├── architecture.md
│   └── api.md
├── docker-compose.yml
├── .env.example
├── .env.production.example
└── README.md
```

## Security

- **Authentication**: JWT tokens with configurable expiration (default 24h)
- **Password Hashing**: BCrypt (Spring Security)
- **Authorization**: Role-based (`@PreAuthorize`) — CANDIDATE, RECRUITER, ADMIN
- **IDOR Protection**: All resource access checks ownership via `CurrentUserUtil`
- **Input Validation**: Jakarta Bean Validation on all DTOs
- **CORS**: Configurable allowed origins (no wildcard in production)
- **SQL Injection**: Parameterized queries via JPA/Hibernate
- **File Upload**: Size limits enforced (10MB max)
- **Error Handling**: Stack traces hidden in production (`include-stacktrace: never`)
- **Secrets**: All sensitive values via environment variables, never hardcoded
- **Git History**: No secrets committed (verified — only `.env.example` files tracked)

## Known Limitations

- AI features require an OpenAI API key; without it, deterministic fallbacks are used
- File uploads stored on local filesystem (not cloud storage)
- No email service integration (notifications are in-app only)
- No WebSocket for real-time updates (uses polling)
- No automated CI/CD pipeline configured
- Single-server deployment (no horizontal scaling setup)

## License

Private — All rights reserved.
