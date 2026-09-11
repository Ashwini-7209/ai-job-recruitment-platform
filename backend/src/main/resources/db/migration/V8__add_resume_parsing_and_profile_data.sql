ALTER TABLE resumes ADD COLUMN parsing_status VARCHAR(20) NOT NULL DEFAULT 'NOT_PROCESSED';
ALTER TABLE resumes ADD COLUMN parsed_at TIMESTAMP NULL;
ALTER TABLE resumes ADD COLUMN failure_reason VARCHAR(500) NULL;

CREATE INDEX idx_resumes_parsing_status ON resumes (parsing_status);

CREATE TABLE resume_profile_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    location VARCHAR(100),
    professional_summary VARCHAR(2000),
    headline VARCHAR(100),
    skills VARCHAR(2000),
    total_years_of_experience INT,
    languages VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_resume_profile_data_resume UNIQUE (resume_id),
    CONSTRAINT fk_resume_profile_data_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    CONSTRAINT fk_resume_profile_data_candidate FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_resume_profile_data_candidate ON resume_profile_data (candidate_id);

CREATE TABLE resume_experiences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_profile_data_id BIGINT NOT NULL,
    company VARCHAR(100),
    job_title VARCHAR(100),
    location VARCHAR(100),
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    current BOOLEAN DEFAULT FALSE,
    description VARCHAR(2000),
    technologies VARCHAR(500),

    CONSTRAINT fk_resume_experiences_profile FOREIGN KEY (resume_profile_data_id) REFERENCES resume_profile_data(id) ON DELETE CASCADE
);

CREATE INDEX idx_resume_experiences_profile ON resume_experiences (resume_profile_data_id);

CREATE TABLE resume_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_profile_data_id BIGINT NOT NULL,
    institution VARCHAR(150),
    degree VARCHAR(100),
    field_of_study VARCHAR(100),
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    grade VARCHAR(50),

    CONSTRAINT fk_resume_education_profile FOREIGN KEY (resume_profile_data_id) REFERENCES resume_profile_data(id) ON DELETE CASCADE
);

CREATE INDEX idx_resume_education_profile ON resume_education (resume_profile_data_id);

CREATE TABLE resume_certifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_profile_data_id BIGINT NOT NULL,
    name VARCHAR(150),
    issuing_organization VARCHAR(150),
    issue_date VARCHAR(20),
    expiration_date VARCHAR(20),

    CONSTRAINT fk_resume_certifications_profile FOREIGN KEY (resume_profile_data_id) REFERENCES resume_profile_data(id) ON DELETE CASCADE
);

CREATE INDEX idx_resume_certifications_profile ON resume_certifications (resume_profile_data_id);

CREATE TABLE resume_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_profile_data_id BIGINT NOT NULL,
    name VARCHAR(150),
    description VARCHAR(2000),
    technologies VARCHAR(500),
    url VARCHAR(255),

    CONSTRAINT fk_resume_projects_profile FOREIGN KEY (resume_profile_data_id) REFERENCES resume_profile_data(id) ON DELETE CASCADE
);

CREATE INDEX idx_resume_projects_profile ON resume_projects (resume_profile_data_id);
