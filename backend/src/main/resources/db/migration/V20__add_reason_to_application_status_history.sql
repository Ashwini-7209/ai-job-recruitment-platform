-- Add reason column to application_status_history for tracking unhire/reopen reasons
ALTER TABLE application_status_history ADD COLUMN reason VARCHAR(500) NULL;
