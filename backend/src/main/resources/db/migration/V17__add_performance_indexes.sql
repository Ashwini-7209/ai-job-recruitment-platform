-- Step 17: Add performance indexes for search, filtering, and pagination
-- MySQL 8.0 compatible: uses information_schema checks instead of IF NOT EXISTS

-- Helper procedure for idempotent index creation
DROP PROCEDURE IF EXISTS _create_idx;

DELIMITER //
CREATE PROCEDURE _create_idx(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_cols VARCHAR(500)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = p_table_name
        AND index_name = p_index_name
        LIMIT 1
    ) THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table_name, '(', p_index_cols, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Jobs: status + published_at for public job listing
CALL _create_idx('jobs', 'idx_jobs_status_published', 'status, published_at DESC');

-- Applications: candidate + status + applied_at for candidate application search
CALL _create_idx('applications', 'idx_applications_candidate_status', 'candidate_id, status, applied_at DESC');

-- Applications: job_id + status for recruiter applicant search
CALL _create_idx('applications', 'idx_applications_job_status', 'job_id, status');

-- Notifications: user + read + created_at for notification listing and unread count
CALL _create_idx('notifications', 'idx_notifications_user_read', 'user_id, is_read, created_at DESC');

-- Saved jobs: candidate + created_at for saved job listing
CALL _create_idx('saved_jobs', 'idx_saved_jobs_candidate_date', 'candidate_id, created_at DESC');

-- Saved jobs: candidate + job for existence checks
CALL _create_idx('saved_jobs', 'idx_saved_jobs_candidate_job', 'candidate_id, job_id');

-- Job alerts: active + candidate for alert matching
CALL _create_idx('job_alerts', 'idx_job_alerts_active_candidate', 'active, candidate_id');

-- Users: role + enabled for admin combined filters
CALL _create_idx('users', 'idx_users_role_enabled', 'role, enabled');

-- Audit logs: actor + created_at
CALL _create_idx('audit_logs', 'idx_audit_logs_actor_date', 'actor_user_id, created_at DESC');

-- Cleanup helper procedure
DROP PROCEDURE IF EXISTS _create_idx;
