CREATE TABLE recruiter_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    phone VARCHAR(20),
    job_title VARCHAR(100),
    department VARCHAR(100),
    company_name VARCHAR(100),
    company_website VARCHAR(255),
    company_description VARCHAR(2000),
    company_location VARCHAR(100),
    linkedin_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_recruiter_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_recruiter_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_recruiter_profiles_user ON recruiter_profiles (user_id);
