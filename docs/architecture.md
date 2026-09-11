# Architecture

## High-Level Overview

The Job Recruitment Platform follows a modular monolith architecture with clear separation between frontend and backend concerns.

```
┌──────────────────────────────────────────────────┐
│                   Frontend                       │
│              React + TypeScript                  │
│                 (Vite)                           │
├──────────────────────────────────────────────────┤
│              HTTP / REST API                     │
├──────────────────────────────────────────────────┤
│                  Backend                         │
│             Spring Boot (Java 21)                │
│                                                  │
│  ┌──────┐ ┌──────────┐ ┌────────┐ ┌──────────┐ │
│  │ User │ │ Candidate│ │Recruiter│ │   Job    │ │
│  └──────┘ └──────────┘ └────────┘ └──────────┘ │
│  ┌──────────┐ ┌───────────┐ ┌────────────────┐  │
│  │ Company  │ │Application│ │   AI Services  │  │
│  └──────────┘ └───────────┘ └────────────────┘  │
│                                                  │
│  ┌──────────┐ ┌───────────┐ ┌────────────────┐  │
│  │  Config  │ │ Exception │ │   Security     │  │
│  └──────────┘ └───────────┘ └────────────────┘  │
├──────────────────────────────────────────────────┤
│                 MySQL Database                   │
└──────────────────────────────────────────────────┘
```

## Frontend/Backend Separation

- **Backend** owns business logic, data access, and API contracts
- **Frontend** owns UI rendering, state management, and user experience
- Communication is via RESTful JSON APIs
- CORS is configured for cross-origin requests

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

## Planned Major Modules

### Core Domain Modules
- **User**: Authentication, authorization, profile management
- **Candidate**: Resume management, job preferences, applications
- **Recruiter**: Job posting, candidate search, hiring workflows
- **Company**: Company profiles, team management
- **Job**: Job listings, requirements, compensation
- **Application**: Application tracking, status management

### Platform Modules
- **AI Services**: Resume analysis, job matching, recommendations
- **Notifications**: Email, push, in-app notifications
- **Analytics**: Recruitment metrics, platform analytics

### Infrastructure Modules
- **Config**: Application configuration
- **Security**: Authentication, authorization
- **Exception**: Global error handling

## Basic Request Flow

```
Client Request → Security Filter → Controller → Service → Repository → Database
     ↑                                                              │
     └──────────── Response ← DTO ← Service ← Repository ←────────┘
```

1. HTTP request arrives at the backend
2. Security filter validates authentication (future)
3. Controller handles the request and validates input
4. Service contains business logic
5. Repository accesses the database
6. Response is serialized to JSON and returned

## Database/AI Infrastructure

**Database**: MySQL with Spring Data JPA will be introduced in later stages. Schema will be designed incrementally as domain models are implemented.

**AI Infrastructure**: AI services (OpenAI, custom ML models) will be integrated as separate service layers, not tightly coupled to domain logic.

## Why Premature Microservices Are Avoided

1. **Premature abstraction**: Without proven bounded contexts, service boundaries would be wrong
2. **Operational overhead**: Kubernetes, service mesh, distributed tracing — significant infrastructure cost
3. **Data consistency**: Distributed transactions are complex; local transactions are simple
4. **Developer velocity**: Single codebase is faster for iteration in early stages
5. **Testing simplicity**: Integration tests are straightforward in a monolith

The modular monolith provides the same code organization benefits while avoiding distributed system complexity.

---

*Architecture evolves with the project. This document reflects current decisions and will be updated as the platform grows.*
