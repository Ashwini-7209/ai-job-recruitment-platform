CREATE TABLE job_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    keywords VARCHAR(200),
    location VARCHAR(100),
    workplace_type VARCHAR(20),
    employment_type VARCHAR(20),
    minimum_experience INT,
    skills VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_triggered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_job_alerts_candidate FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_job_alerts_candidate ON job_alerts(candidate_id, created_at DESC);
CREATE INDEX idx_job_alerts_active ON job_alerts(active);
