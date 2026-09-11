package com.jobplatform.resume.parsing;

import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeProfileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeProfileDataRepository extends JpaRepository<ResumeProfileData, Long> {

    Optional<ResumeProfileData> findByResume(Resume resume);

    Optional<ResumeProfileData> findByCandidateId(Long candidateId);

    boolean existsByResumeId(Long resumeId);

    void deleteByResumeId(Long resumeId);
}
