CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recruiter_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    location VARCHAR(100),
    employment_type VARCHAR(20) NOT NULL,
    workplace_type VARCHAR(20) NOT NULL,
    experience_min INT,
    experience_max INT,
    salary_min INT,
    salary_max INT,
    skills VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    application_deadline TIMESTAMP,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_jobs_recruiter FOREIGN KEY (recruiter_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_jobs_recruiter ON jobs (recruiter_id);
CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_employment_type ON jobs (employment_type);
CREATE INDEX idx_jobs_workplace_type ON jobs (workplace_type);
CREATE INDEX idx_jobs_location ON jobs (location);
CREATE INDEX idx_jobs_published_status ON jobs (status, created_at DESC);
