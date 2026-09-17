-- Step 17: Add performance indexes for search, filtering, and pagination

-- Jobs: status + published_at for public job listing
CREATE INDEX idx_jobs_status_published ON jobs(status, published_at DESC);

-- Applications: candidate + status + applied_at for candidate application search
CREATE INDEX idx_applications_candidate_status ON applications(candidate_id, status, applied_at DESC);

-- Applications: job_id + status for recruiter applicant search
CREATE INDEX idx_applications_job_status ON applications(job_id, status);

-- Notifications: user + read + created_at for notification listing and unread count
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read, created_at DESC);

-- Saved jobs: candidate + created_at for saved job listing
CREATE INDEX idx_saved_jobs_candidate_date ON saved_jobs(candidate_id, created_at DESC);

-- Saved jobs: candidate + job for existence checks
CREATE INDEX idx_saved_jobs_candidate_job ON saved_jobs(candidate_id, job_id);

-- Job alerts: active + candidate for alert matching
CREATE INDEX idx_job_alerts_active_candidate ON job_alerts(active, candidate_id);

-- Users: role + enabled for admin combined filters
CREATE INDEX idx_users_role_enabled ON users(role, enabled);

-- Audit logs: actor + created_at
CREATE INDEX idx_audit_logs_actor_date ON audit_logs(actor_user_id, created_at DESC);
