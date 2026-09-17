package com.jobplatform.candidate;

import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    Optional<CandidateProfile> findByUser(User user);

    Optional<CandidateProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT cp FROM CandidateProfile cp WHERE cp.user.enabled = true " +
           "AND (:query IS NULL OR :query = '' OR " +
           "LOWER(cp.user.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(cp.headline) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(cp.currentJobTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(cp.skillsSummary) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(cp.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:skills IS NULL OR :skills = '' OR LOWER(cp.skillsSummary) LIKE LOWER(CONCAT('%', :skills, '%'))) " +
           "AND (:minExperience IS NULL OR cp.yearsOfExperience >= :minExperience) " +
           "AND (:maxExperience IS NULL OR cp.yearsOfExperience <= :maxExperience) " +
           "AND (:jobTitle IS NULL OR :jobTitle = '' OR LOWER(cp.currentJobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%')))")
    Page<CandidateProfile> searchCandidates(
            @Param("query") String query,
            @Param("location") String location,
            @Param("skills") String skills,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("jobTitle") String jobTitle,
            Pageable pageable);
}
