-- Step 10: Add index for date-based sorting/filtering on candidate applications
CREATE INDEX idx_app_applied_at ON applications(applied_at);
