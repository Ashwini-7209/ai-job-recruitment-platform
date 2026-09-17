-- Step 19: Fix V17 index name collisions and remove duplicate indexes
-- V17 used CREATE INDEX IF NOT EXISTS which silently skipped when index names collided

-- Fix 1: Drop the 2-column index from V5, recreate as 3-column from V17
DROP INDEX idx_applications_candidate_status ON applications;
CREATE INDEX idx_applications_candidate_status ON applications(candidate_id, status, applied_at DESC);

-- Fix 2: Drop the 2-column index from V12, recreate as 3-column from V17
DROP INDEX idx_notifications_user_read ON notifications;
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read, created_at DESC);

-- Fix 3: Remove duplicate index (identical to V13's idx_saved_jobs_candidate)
DROP INDEX idx_saved_jobs_candidate_date ON saved_jobs;

-- Fix 4: Remove redundant index (duplicates unique constraint uk_saved_job_candidate_job)
DROP INDEX idx_saved_jobs_candidate_job ON saved_jobs;

-- Fix 5: Remove duplicate index (identical to V16's idx_audit_logs_actor)
DROP INDEX idx_audit_logs_actor_date ON audit_logs;

-- Fix 6: Add composite index for recruiter job listing queries
CREATE INDEX idx_jobs_recruiter_status_created ON jobs(recruiter_id, status, created_at DESC);

-- Fix 7: Add composite index for recruiter application listing queries
CREATE INDEX idx_applications_job_applied ON applications(job_id, applied_at DESC);
