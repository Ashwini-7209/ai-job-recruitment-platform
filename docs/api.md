# API Documentation

Base URL: `http://localhost:8080/api` (development) or via Nginx proxy at `/api` (Docker)

All responses follow the `ApiResponse` wrapper:
```json
{
  "success": true,
  "message": "optional message",
  "data": { ... },
  "timestamp": "2026-09-14T12:00:00Z"
}
```

## Authentication

### Register
```
POST /api/auth/register
```
**Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CANDIDATE"
}
```
**Response:** `RegisterResponse` with id, fullName, email, role.

### Login
```
POST /api/auth/login
```
**Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
**Response:** `LoginResponse` with accessToken, tokenType, expiresIn, user.

---

## Health

### Health Check
```
GET /api/health
```
**Auth:** None
**Response:** `{ "status": "UP" }`

---

## Jobs (Public)

### Search Jobs
```
GET /api/jobs?q=&location=&employmentType=&workplaceType=&experienceMin=&experienceMax=&salaryMin=&salaryMax=&skills=&companyName=&postedAfter=&page=0&size=20
```
**Auth:** None
**Response:** `PagedResponse<JobSummaryResponse>` — list of published jobs with recruiter name.

### Get Job
```
GET /api/jobs/{id}
```
**Auth:** None
**Response:** `JobResponse` — full job details.

---

## Jobs (Candidate)

### Search Jobs (with saved/applied status)
```
GET /api/candidate/jobs?q=&location=&...&page=0&size=20
```
**Auth:** CANDIDATE
**Response:** `PagedResponse<JobSummaryResponse>` — includes `saved` and `applied` booleans.

### Get Job (with status)
```
GET /api/candidate/jobs/{id}
```
**Auth:** CANDIDATE

---

## Jobs (Recruiter)

### Create Job
```
POST /api/recruiter/jobs
```
**Auth:** RECRUITER
**Body:**
```json
{
  "title": "Software Engineer",
  "description": "Job description...",
  "location": "New York",
  "employmentType": "FULL_TIME",
  "workplaceType": "HYBRID",
  "experienceMin": 2,
  "experienceMax": 5,
  "salaryMin": 80000,
  "salaryMax": 120000,
  "skills": "Java, Spring Boot, React"
}
```

### Generate Description (AI)
```
POST /api/recruiter/jobs/generate-description
```
**Auth:** RECRUITER
**Body:** `{ "title": "...", "skills": "...", "employmentType": "...", "workplaceType": "...", "experienceMin": 2, "experienceMax": 5 }`

### List My Jobs
```
GET /api/recruiter/jobs?status=&page=0&size=20
```
**Auth:** RECRUITER

### Get My Job
```
GET /api/recruiter/jobs/{id}
```
**Auth:** RECRUITER (must own job)

### Update Job
```
PUT /api/recruiter/jobs/{id}
```
**Auth:** RECRUITER (must own job)

### Publish Job
```
POST /api/recruiter/jobs/{id}/publish
```
**Auth:** RECRUITER

### Close Job
```
POST /api/recruiter/jobs/{id}/close
```
**Auth:** RECRUITER

### Delete Job
```
DELETE /api/recruiter/jobs/{id}
```
**Auth:** RECRUITER

### Job Stats
```
GET /api/recruiter/jobs/stats
```
**Auth:** RECRUITER

---

## Candidate Profile

### Get Profile
```
GET /api/candidates/me/profile
```
**Auth:** CANDIDATE

### Update Profile
```
PUT /api/candidates/me/profile
```
**Auth:** CANDIDATE

---

## Recruiter Profile

### Get Profile
```
GET /api/recruiters/me/profile
```
**Auth:** RECRUITER

### Update Profile
```
PUT /api/recruiters/me/profile
```
**Auth:** RECRUITER

---

## Recruiter Candidate Search

### Search Candidates
```
GET /api/recruiters/candidates?q=&location=&skills=&minExperience=&maxExperience=&jobTitle=&page=0&size=20
```
**Auth:** RECRUITER

---

## Applications (Candidate)

### Apply to Job
```
POST /api/jobs/{jobId}/applications
```
**Auth:** CANDIDATE
**Body:** Multipart form with optional `coverLetter` and `resumeId`.

### List My Applications
```
GET /api/candidates/me/applications?q=&status=&page=0&size=20
```
**Auth:** CANDIDATE

### Get Application
```
GET /api/candidates/me/applications/{applicationId}
```
**Auth:** CANDIDATE

### Withdraw Application
```
PATCH /api/candidates/me/applications/{applicationId}/withdraw
```
**Auth:** CANDIDATE

### Application Stats
```
GET /api/candidates/me/application-stats
```
**Auth:** CANDIDATE

---

## Applications (Recruiter)

### List Applications
```
GET /api/recruiters/me/applications?q=&jobId=&status=&page=0&size=20
```
**Auth:** RECRUITER

### List Applications for Job
```
GET /api/recruiters/me/jobs/{jobId}/applications?page=0&size=20
```
**Auth:** RECRUITER

### Job Application Stats
```
GET /api/recruiters/me/jobs/{jobId}/applications/stats
```
**Auth:** RECRUITER

### Get Application
```
GET /api/recruiters/me/applications/{applicationId}
```
**Auth:** RECRUITER

### Match Score
```
GET /api/recruiters/me/applications/{applicationId}/match
```
**Auth:** RECRUITER

### Update Status
```
PATCH /api/recruiters/me/applications/{applicationId}/status
```
**Auth:** RECRUITER
**Body:** `{ "status": "SHORTLISTED", "notes": "Great experience" }`

### Application Stats
```
GET /api/recruiters/me/application-stats
```
**Auth:** RECRUITER

### Create Note
```
POST /api/recruiters/me/applications/{applicationId}/notes
```
**Auth:** RECRUITER
**Body:** `{ "content": "Note text" }`

### List Notes
```
GET /api/recruiters/me/applications/{applicationId}/notes
```
**Auth:** RECRUITER

### Update Note
```
PUT /api/recruiters/me/applications/{applicationId}/notes/{noteId}
```
**Auth:** RECRUITER

### Delete Note
```
DELETE /api/recruiters/me/applications/{applicationId}/notes/{noteId}
```
**Auth:** RECRUITER

---

## Resumes (Candidate)

### Upload Resume
```
POST /api/candidates/me/resumes
```
**Auth:** CANDIDATE
**Body:** Multipart form-data with `file` (PDF, DOCX, TXT; max 10MB).

### List Resumes
```
GET /api/candidates/me/resumes
```
**Auth:** CANDIDATE

### Get Resume
```
GET /api/candidates/me/resumes/{resumeId}
```
**Auth:** CANDIDATE

### Download Resume
```
GET /api/candidates/me/resumes/{resumeId}/download
```
**Auth:** CANDIDATE

### Activate Resume
```
POST /api/candidates/me/resumes/{resumeId}/activate
```
**Auth:** CANDIDATE

### Delete Resume
```
DELETE /api/candidates/me/resumes/{resumeId}
```
**Auth:** CANDIDATE

### Parse Resume (AI)
```
POST /api/candidates/me/resumes/{resumeId}/parse
```
**Auth:** CANDIDATE

---

## Resumes (Recruiter)

### Download Application Resume
```
GET /api/recruiters/me/applications/{applicationId}/resume
```
**Auth:** RECRUITER

---

## Interviews (Candidate)

### List Interviews
```
GET /api/candidates/me/interviews?page=0&size=20
```
**Auth:** CANDIDATE

### Upcoming Interviews
```
GET /api/candidates/me/interviews/upcoming
```
**Auth:** CANDIDATE

### Get Interview
```
GET /api/candidates/me/interviews/{interviewId}
```
**Auth:** CANDIDATE

### Interview Stats
```
GET /api/candidates/me/interview-stats
```
**Auth:** CANDIDATE

---

## Interviews (Recruiter)

### Schedule Interview
```
POST /api/recruiters/me/applications/{applicationId}/interviews
```
**Auth:** RECRUITER
**Body:**
```json
{
  "scheduledAt": "2026-09-20T14:00:00",
  "interviewType": "VIDEO",
  "location": "https://meet.google.com/abc",
  "notes": "Technical interview"
}
```

### List Interviews for Application
```
GET /api/recruiters/me/applications/{applicationId}/interviews
```
**Auth:** RECRUITER

### List All Interviews
```
GET /api/recruiters/me/interviews?page=0&size=20
```
**Auth:** RECRUITER

### Upcoming Interviews
```
GET /api/recruiters/me/interviews/upcoming
```
**Auth:** RECRUITER

### Get Interview
```
GET /api/recruiters/me/interviews/{interviewId}
```
**Auth:** RECRUITER

### Update Interview
```
PATCH /api/recruiters/me/interviews/{interviewId}
```
**Auth:** RECRUITER

### Cancel Interview
```
PATCH /api/recruiters/me/interviews/{interviewId}/cancel
```
**Auth:** RECRUITER

### Update Interview Status
```
PATCH /api/recruiters/me/interviews/{interviewId}/status
```
**Auth:** RECRUITER

### Interview Stats
```
GET /api/recruiters/me/interview-stats
```
**Auth:** RECRUITER

---

## AI Features

### Match Score
```
GET /api/candidates/me/jobs/{jobId}/match
```
**Auth:** CANDIDATE
**Response:** `{ "matchScore": 85, "breakdown": {...}, "recommendations": [...] }`

### Skill Gap Analysis
```
GET /api/candidates/me/jobs/{jobId}/skill-gap
```
**Auth:** CANDIDATE

### Career Insights
```
GET /api/candidates/me/career-insights
```
**Auth:** CANDIDATE

### Resume Improvement Analysis
```
POST /api/candidates/me/resumes/{resumeId}/improvement-analysis
```
**Auth:** CANDIDATE

### Job Recommendations
```
GET /api/candidates/me/recommendations?minScore=60&page=0&size=20
```
**Auth:** CANDIDATE

---

## Saved Jobs

### Save Job
```
POST /api/candidates/me/saved-jobs/{jobId}
```
**Auth:** CANDIDATE

### Unsave Job
```
DELETE /api/candidates/me/saved-jobs/{jobId}
```
**Auth:** CANDIDATE

### List Saved Jobs
```
GET /api/candidates/me/saved-jobs?q=&location=&workplaceType=&employmentType=&page=0&size=20
```
**Auth:** CANDIDATE

### Check Saved Status
```
GET /api/candidates/me/saved-jobs/{jobId}/status
```
**Auth:** CANDIDATE

---

## Job Alerts

### Create Alert
```
POST /api/candidates/me/job-alerts
```
**Auth:** CANDIDATE
**Body:** `{ "keywords": "Java, React", "location": "New York", "employmentType": "FULL_TIME" }`

### List Alerts
```
GET /api/candidates/me/job-alerts?page=0&size=20
```
**Auth:** CANDIDATE

### Get Alert
```
GET /api/candidates/me/job-alerts/{alertId}
```
**Auth:** CANDIDATE

### Update Alert
```
PATCH /api/candidates/me/job-alerts/{alertId}
```
**Auth:** CANDIDATE

### Delete Alert
```
DELETE /api/candidates/me/job-alerts/{alertId}
```
**Auth:** CANDIDATE

### Toggle Alert Status
```
PATCH /api/candidates/me/job-alerts/{alertId}/status
```
**Auth:** CANDIDATE

---

## Notifications

### List Notifications
```
GET /api/notifications?page=0&size=20
```
**Auth:** Any authenticated user

### Unread Count
```
GET /api/notifications/unread-count
```
**Auth:** Any authenticated user

### Mark as Read
```
PATCH /api/notifications/{id}/read
```
**Auth:** Any authenticated user

### Mark All as Read
```
PATCH /api/notifications/read-all
```
**Auth:** Any authenticated user

---

## Notification Preferences

### Get Preferences
```
GET /api/notifications/preferences
```
**Auth:** CANDIDATE or RECRUITER

### Update Preferences
```
PATCH /api/notifications/preferences
```
**Auth:** CANDIDATE or RECRUITER

---

## Analytics (Candidate)

### Summary
```
GET /api/candidates/me/analytics/summary
```
**Auth:** CANDIDATE

### Application Status Distribution
```
GET /api/candidates/me/analytics/application-status
```
**Auth:** CANDIDATE

### Application Trend
```
GET /api/candidates/me/analytics/application-trend?from=&to=
```
**Auth:** CANDIDATE

---

## Analytics (Recruiter)

### Summary
```
GET /api/recruiters/me/analytics/summary
```
**Auth:** RECRUITER

### Recruitment Funnel
```
GET /api/recruiters/me/analytics/funnel
```
**Auth:** RECRUITER

### Per-Job Analytics
```
GET /api/recruiters/me/analytics/jobs?page=0&size=20
```
**Auth:** RECRUITER

### Application Trend
```
GET /api/recruiters/me/analytics/application-trend?from=&to=
```
**Auth:** RECRUITER

### Hiring Metrics
```
GET /api/recruiters/me/analytics/hiring-metrics
```
**Auth:** RECRUITER

---

## Analytics (Admin)

### Summary
```
GET /api/admin/analytics/summary
```
**Auth:** ADMIN

### User Growth
```
GET /api/admin/analytics/user-growth?from=&to=&role=
```
**Auth:** ADMIN

### Job Analytics
```
GET /api/admin/analytics/jobs
```
**Auth:** ADMIN

### Application Analytics
```
GET /api/admin/analytics/applications
```
**Auth:** ADMIN

### Interview Analytics
```
GET /api/admin/analytics/interviews
```
**Auth:** ADMIN

### Application Trend
```
GET /api/admin/analytics/application-trend?from=&to=
```
**Auth:** ADMIN

---

## Admin Management

### List Users
```
GET /api/admin/users?q=&role=&enabled=&page=0&size=20
```
**Auth:** ADMIN

### Get User
```
GET /api/admin/users/{userId}
```
**Auth:** ADMIN

### Toggle User Status
```
PATCH /api/admin/users/{userId}/status
```
**Auth:** ADMIN
**Body:** `{ "enabled": false }`

### Change User Role
```
PATCH /api/admin/users/{userId}/role
```
**Auth:** ADMIN
**Body:** `{ "role": "RECRUITER" }`

### Dashboard Stats
```
GET /api/admin/users/dashboard-stats
```
**Auth:** ADMIN

### List Jobs
```
GET /api/admin/jobs?status=&page=0&size=20
```
**Auth:** ADMIN

### Get Job
```
GET /api/admin/jobs/{jobId}
```
**Auth:** ADMIN

### Update Job Status
```
PATCH /api/admin/jobs/{jobId}/status
```
**Auth:** ADMIN
**Body:** `{ "status": "CLOSED" }`

### List Applications
```
GET /api/admin/applications?jobId=&status=&candidateName=&page=0&size=20
```
**Auth:** ADMIN

### Audit Logs
```
GET /api/admin/audit-logs?actorId=&action=&entityType=&page=0&size=20
```
**Auth:** ADMIN
