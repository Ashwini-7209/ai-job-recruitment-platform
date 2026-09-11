package com.jobplatform.savedjob;

import com.jobplatform.job.Job;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    Optional<SavedJob> findByCandidateAndJob(User candidate, Job job);

    boolean existsByCandidateAndJob(User candidate, Job job);

    void deleteByCandidateAndJob(User candidate, Job job);

    Page<SavedJob> findByCandidateOrderByCreatedAtDesc(User candidate, Pageable pageable);

    @Query("SELECT sj FROM SavedJob sj WHERE sj.candidate = :candidate AND " +
           "(:query IS NULL OR :query = '' OR LOWER(sj.job.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sj.job.location) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(sj.job.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:workplaceType IS NULL OR sj.job.workplaceType = :workplaceType) " +
           "AND (:employmentType IS NULL OR sj.job.employmentType = :employmentType) " +
           "AND (:jobStatus IS NULL OR sj.job.status = :jobStatus)")
    Page<SavedJob> searchSavedJobsCombined(
            @Param("candidate") User candidate,
            @Param("query") String query,
            @Param("location") String location,
            @Param("workplaceType") WorkplaceType workplaceType,
            @Param("employmentType") EmploymentType employmentType,
            @Param("jobStatus") JobStatus jobStatus,
            Pageable pageable);

    @Query("SELECT sj FROM SavedJob sj WHERE sj.candidate = :candidate AND " +
           "(:query IS NULL OR LOWER(sj.job.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sj.job.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<SavedJob> searchSavedJobs(@Param("candidate") User candidate, @Param("query") String query, Pageable pageable);

    @Query("SELECT sj.job.id FROM SavedJob sj WHERE sj.candidate = :candidate AND sj.job.id IN :jobIds")
    List<Long> findJobIdsByCandidateAndJobIds(@Param("candidate") User candidate, @Param("jobIds") List<Long> jobIds);

    long countByCandidate(User candidate);
}
