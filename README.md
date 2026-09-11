# AI-Powered Job & Recruitment Platform

A full-stack AI-powered job and recruitment platform connecting candidates with opportunities through intelligent matching, resume analysis, and recruitment automation.

## Main Features

### Candidate Portal
- Register/login with JWT authentication
- Complete profile management (skills, experience, education)
- Upload and parse resumes (PDF, DOCX, TXT)
- AI-powered job matching with skill gap analysis
- Career insights and resume improvement suggestions
- Save jobs, create job alerts
- Track applications with status timeline
- View upcoming interviews
- Application analytics and trends

### Recruiter Portal
- Register/login with JWT authentication
- Complete recruiter/company profile
- Create, publish, and manage job listings
- Search/filter applications by candidate name, status, job
- Update application status with notes
- Schedule and manage interviews
- View recruitment funnel and hiring analytics
- Receive notifications

### Admin Portal
- User management (search, enable/disable, role changes)
- Job moderation (approve, close, delete)
- Application oversight
- Platform analytics (user growth, job stats, application trends)
- Audit log viewer
- Dashboard with key metrics

### AI Features
- **Job Matching**: Rule-based + AI-enhanced matching with scoring
- **Skill Gap Analysis**: Identifies missing skills for job requirements
- **Career Insights**: Personalized recommendations based on profile
- **Resume Improvement**: AI-powered resume analysis and suggestions
- All AI features have deterministic fallbacks when AI is unavailable

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Backend | Java + Spring Boot | 21 / 3.4.1 |
| Frontend | React + TypeScript | 19 / 6.0 |
| Build (FE) | Vite | 8.2.2 |
| Styling | Tailwind CSS | 4.3.3 |
| Database | MySQL | 8.0 |
| ORM | Spring Data JPA + Hibernate | - |
| Migrations | Flyway | 17 versions |
| Auth | Spring Security + JWT | - |
| AI | OpenAI API (optional) | gpt-4o-mini |
| HTTP Client | Axios | 1.20 |
| Routing | React Router | 7.18 |
| Testing | JUnit 5 + Mockito + MockMvc | - |

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  Frontend                        │
│         React + TypeScript (Vite)                │
│    Pages: Auth | Candidate | Recruiter | Admin   │
│    Services: API Client (Axios + JWT)            │
├─────────────────────────────────────────────────┤
│               REST API (JSON)                    │
├─────────────────────────────────────────────────┤
│                 Backend                          │
│           Spring Boot (Java 21)                  │
│                                                  │
│  Modules: auth | user | candidate | recruiter    │
│           job | application | resume | interview  │
│           notification | savedjob | jobalert      │
│           analytics | matching | ai | admin       │
│                                                  │
│  Security: JWT + @PreAuthorize role checks       │
├─────────────────────────────────────────────────┤
│                MySQL Database                    │
│          17 Flyway migrations (V1-V17)           │
└─────────────────────────────────────────────────┘
```

## Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.9+
- **Node.js**: 20+ LTS
- **npm**: 10+
- **MySQL**: 8.0+ (or use H2 for testing)
- **Docker** (optional): For containerized setup

## Database Setup

### Option 1: Local MySQL

```bash
# Create database
mysql -u root -e "CREATE DATABASE job_platform_dev;"

# Flyway runs automatically on startup — no manual migration needed
```

### Option 2: Docker

```bash
docker-compose up mysql -d
# Database is created automatically with the configured credentials
```

## Environment Variables

Copy `.env.example` to `.env` and configure:

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/job_platform_dev` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | (empty) |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | dev-only default |
| `JWT_EXPIRATION_MS` | Token expiration | `86400000` (24h) |
| `AI_API_KEY` | OpenAI API key (optional) | (empty — uses fallbacks) |
| `AI_MODEL` | AI model name | `gpt-4o-mini` |
| `SERVER_PORT` | Backend port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:5173` |

## Running the Backend

```bash
cd backend

# First build (compile + test + package)
mvn clean package

# Run development server
mvn spring-boot:run

# Or run the JAR directly
java -jar target/job-recruitment-platform-0.1.0-SNAPSHOT.jar
```

Backend starts at: `http://localhost:8080`

## Running the Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

Frontend starts at: `http://localhost:5173`

## Running with Docker

```bash
# Start all services (MySQL + Backend + Frontend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

Services:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- MySQL: `localhost:3306`

## Testing

### Backend Tests

```bash
cd backend

# Run all tests (524 tests)
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

## Production Build

### Backend

```bash
cd backend
mvn clean package -DskipTests
# Output: target/job-recruitment-platform-0.1.0-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
# Output: frontend/dist/
```

## API Documentation

When the backend is running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

### Key Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/health` | Public | Health check |
| GET | `/api/jobs` | Public | Search published jobs |
| GET | `/api/candidates/me/resumes` | CANDIDATE | List resumes |
| POST | `/api/candidates/me/resumes` | CANDIDATE | Upload resume |
| GET | `/api/candidates/me/recommendations` | CANDIDATE | Job recommendations |
| GET | `/api/candidates/me/jobs/{id}/match` | CANDIDATE | AI job match |
| GET | `/api/recruiter/jobs` | RECRUITER | List recruiter's jobs |
| POST | `/api/recruiter/jobs` | RECRUITER | Create job |
| GET | `/api/recruiters/me/analytics/summary` | RECRUITER | Analytics |
| GET | `/api/admin/users` | ADMIN | Manage users |
| GET | `/api/admin/analytics/summary` | ADMIN | Admin analytics |

## Default URLs

| Service | Development | Docker |
|---|---|---|
| Frontend | `http://localhost:5173` | `http://localhost:3000` |
| Backend API | `http://localhost:8080` | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | Same |
| Health Check | `http://localhost:8080/api/health` | Same |

## Troubleshooting

### Backend won't start
- Ensure MySQL is running and accessible
- Check `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables
- Verify Java 21: `java -version`

### Frontend build fails
- Run `npm install` to install dependencies
- Check Node.js version: `node -v` (requires 20+)
- Clear cache: `rm -rf node_modules && npm install`

### Database migration fails
- Ensure the database exists before starting
- Check Flyway migration files in `backend/src/main/resources/db/migration/`
- Do not modify already-applied migrations

### Tests fail
- Ensure H2 test database is available (in-memory, no setup needed)
- Run `mvn test` from the `backend/` directory

## Security Notes

- **Never commit secrets** — use environment variables for all sensitive values
- JWT secrets must be at least 32 characters in production
- Database passwords should be strong and unique per environment
- The default `JWT_SECRET` in `application.yml` is for development only
- All API endpoints enforce role-based authorization
- Input validation is applied on all DTOs
- CORS is restricted to configured origins

## Project Status

**Development Complete** — All 30 planned steps have been implemented and verified:

- 524 backend tests passing
- Frontend TypeScript clean, production build passes
- 17 Flyway database migrations
- JWT authentication with role-based authorization
- AI features with deterministic fallbacks
- Security hardened (IDOR protection, input validation, no hardcoded secrets)
- Docker containerization ready
- Comprehensive documentation

## License

Private — All rights reserved.
