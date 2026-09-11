ALTER TABLE applications ADD COLUMN submitted_resume_id BIGINT NULL;

ALTER TABLE applications ADD CONSTRAINT fk_applications_resume FOREIGN KEY (submitted_resume_id) REFERENCES resumes(id) ON DELETE SET NULL;

CREATE INDEX idx_applications_resume ON applications (submitted_resume_id);
