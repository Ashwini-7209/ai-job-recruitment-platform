-- Step 18: Additional indexes for advanced search and candidate discovery
-- MySQL 8.0 and H2 compatible

-- Jobs: published_at for relevance/newest sort
CREATE INDEX idx_jobs_published_at ON jobs(published_at DESC);

-- Candidate profiles: location search
CREATE INDEX idx_candidate_profiles_location ON candidate_profiles(location);

-- Candidate profiles: experience filter
CREATE INDEX idx_candidate_profiles_experience ON candidate_profiles(years_of_experience);
