package com.jobplatform.resume;

import com.jobplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByCandidateOrderByCreatedAtDesc(User candidate);

    List<Resume> findByCandidateAndActiveTrue(User candidate);

    Optional<Resume> findByIdAndCandidate(Long id, User candidate);

    Optional<Resume> findByStoredFileName(String storedFileName);

    boolean existsByCandidateAndActiveTrue(User candidate);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Resume r SET r.active = false WHERE r.candidate = :candidate AND r.active = true")
    void deactivateAllByCandidate(User candidate);

    long countByCandidate(User candidate);
}
