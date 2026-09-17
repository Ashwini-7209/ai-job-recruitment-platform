# Architecture

## High-Level Overview

The HireFlow Platform follows a modular monolith architecture with clear separation between frontend and backend concerns.

```
┌──────────────────────────────────────────────────┐
│                   Frontend                       │
│              React + TypeScript                  │
│                 (Vite)                           │
│                                                  │
│  ┌──────┐ ┌──────────┐ ┌────────┐ ┌──────────┐  │
│  │Auth  │ │Candidate │ │Recruiter│ │  Admin   │  │
│  │Pages │ │  Pages   │ │  Pages  │ │  Pages   │  │
│  └──────┘ └──────────┘ └────────┘ └──────────┘  │
│                                                  │
│  Services: API Client (Axios + JWT)              │
│  Components: UI Library (Button, Card, etc.)     │
├──────────────────────────────────────────────────┤
│              HTTP / REST API                     │
│              Nginx Reverse Proxy                  │
├──────────────────────────────────────────────────┤
│                  Backend                         │
│             Spring Boot (Java 21)                │
│                                                  │
│  ┌──────┐ ┌──────────┐ ┌────────┐ ┌──────────┐  │
│  │ Auth │ │ Candidate│ │Recruiter│ │   Job    │  │
│  └──────┘ └──────────┘ └────────┘ └──────────┘  │
│  ┌──────────┐ ┌───────────┐ ┌────────────────┐   │
│  │Application│ │  Resume   │ │  AI Services  │   │
│  └──────────┘ └───────────┘ └────────────────┘   │
│  ┌──────────┐ ┌───────────┐ ┌────────────────┐   │
│  │Interview │ │  Analytics │ │  Notification  │   │
│  └──────────┘ └───────────┘ └────────────────┘   │
│  ┌──────────┐ ┌───────────┐ ┌────────────────┐   │
│  │  Config  │ │ Exception │ │   Security     │   │
│  └──────────┘ └───────────┘ └────────────────┘   │
├──────────────────────────────────────────────────┤
│                 MySQL Database                   │
│            22 tables, 19 migrations              │
└──────────────────────────────────────────────────┘
```

## Frontend/Backend Separation

- **Backend** owns business logic, data access, and API contracts
- **Frontend** owns UI rendering, state management, and user experience
- Communication is via RESTful JSON APIs
- Nginx proxies `/api` requests to the backend in Docker
- CORS is configured for cross-origin requests in development

## Modular Monolith Decision

### Why Modular Monolith

1. **Simplicity**: One deployment unit during early development
2. **Performance**: No network overhead between modules
3. **Developer experience**: Easy to test, debug, and iterate
4. **Transaction consistency**: ACID transactions across modules when needed
5. **Avoid premature optimization**: Microservices add complexity without proven need

### When to Split

Microservices would be considered when:
- Specific modules need independent scaling
- Team size grows enough to justify separate deploy cycles
- Specific modules have drastically different technology needs
- Clear bounded contexts are proven by real usage

## Modules

### Core Domain Modules
- **Auth**: JWT authentication, registration, login
- **User**: Profile management, role-based access
- **Candidate**: Resume management, job preferences, applications
- **Recruiter**: Job posting, candidate search, hiring workflows
- **Job**: Job listings, search, requirements, compensation
- **Application**: Application tracking, status management, notes

### Platform Modules
- **AI Services**: Resume analysis, job matching, recommendations, job description generation
- **Notifications**: In-app notifications, preferences
- **Analytics**: Recruitment metrics, platform analytics
- **Interview**: Interview scheduling and management
- **Saved Jobs / Job Alerts**: Bookmarks and keyword alerts

### Infrastructure Modules
- **Config**: Security, CORS, JPA auditing, data seeding
- **Exception**: Global error handling, validation
- **Security**: JWT filter, role authorization

## Request Flow

```
Client Request → Nginx → Security Filter → Controller → Service → Repository → Database
      ↑                                                                      │
      └──────────── Response ← DTO ← Service ← Repository ←────────────────┘
```

1. HTTP request arrives at Nginx
2. Nginx proxies `/api` requests to the backend
3. JWT filter validates the token (if present)
4. `@PreAuthorize` checks role-based access
5. Controller validates input (Jakarta Bean Validation)
6. Service contains business logic
7. Repository accesses the database via JPA/Hibernate
8. Response is wrapped in `ApiResponse` and serialized to JSON

## Database

- **Engine**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **Migrations**: Flyway (V1–V19)
- **22 tables** with proper foreign keys and indexes
- **Performance indexes** added in V17, V18, V19

## Security

- **Password hashing**: BCrypt
- **Session management**: Stateless (JWT)
- **Role authorization**: `@PreAuthorize("hasRole('CANDIDATE')")`
- **IDOR protection**: All resource access verified via `CurrentUserUtil`
- **Input validation**: Jakarta Bean Validation on all DTOs
- **CORS**: Configurable allowed origins
- **Error handling**: Stack traces hidden in production

---

*Architecture evolves with the project. This document reflects current decisions and will be updated as the platform grows.*
