package com.jobplatform.application;

import com.jobplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {

    List<ApplicationNote> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    Optional<ApplicationNote> findByIdAndRecruiter(Long id, User recruiter);

    List<ApplicationNote> findByApplicationIdAndRecruiterOrderByCreatedAtDesc(Long applicationId, User recruiter);

    void deleteByIdAndRecruiter(Long id, User recruiter);

    long countByApplicationId(Long applicationId);
}
