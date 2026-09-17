package com.jobplatform.application;

import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.job.Job;
import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByCandidateAndJob(User candidate, Job job);

    @EntityGraph(attributePaths = {"job", "job.recruiter", "submittedResume"})
    @Query("SELECT a FROM Application a WHERE a.id = :id")
    Optional<Application> findByIdWithJobAndResume(@Param("id") Long id);

    boolean existsByCandidateAndJob(User candidate, Job job);

    Page<Application> findByCandidateOrderByAppliedAtDesc(User candidate, Pageable pageable);

    Page<Application> findByCandidateAndStatusIn(User candidate, java.util.List<ApplicationStatus> statuses, Pageable pageable);

    Page<Application> findByJobRecruiterOrderByAppliedAtDesc(User recruiter, Pageable pageable);

    Page<Application> findByJobAndJobRecruiterOrderByAppliedAtDesc(Job job, User recruiter, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    long countByJobAndStatus(Job job, ApplicationStatus status);

    long countByJob(Job job);

    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter AND (" +
           "LOWER(a.candidate.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.job.title) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Application> searchRecruiterApplications(@Param("recruiter") User recruiter, @Param("query") String query, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter AND " +
           "(:jobId IS NULL OR a.job.id = :jobId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:query IS NULL OR LOWER(a.candidate.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.candidate.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.job.title) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Application> searchRecruiterApplicationsFiltered(
            @Param("recruiter") User recruiter,
            @Param("query") String query,
            @Param("jobId") Long jobId,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    List<Application> findByJobRecruiter(User recruiter);

    long countByJobRecruiter(User recruiter);

    long countByJobRecruiterAndStatus(User recruiter, ApplicationStatus status);

    // Candidate-scoped queries
    long countByCandidate(User candidate);

    long countByCandidateAndStatus(User candidate, ApplicationStatus status);

    @Query("SELECT a.job.id FROM Application a WHERE a.candidate = :candidate AND a.job.id IN :jobIds")
    List<Long> findJobIdsByCandidateAndJobIds(@Param("candidate") User candidate, @Param("jobIds") List<Long> jobIds);

    @Query("SELECT a FROM Application a WHERE a.candidate = :candidate AND " +
           "(:query IS NULL OR LOWER(a.job.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.job.location) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Application> searchCandidateApplications(
            @Param("candidate") User candidate,
            @Param("query") String query,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    @Query("SELECT FUNCTION('DATE', a.appliedAt) as date, COUNT(a) FROM Application a " +
           "WHERE a.candidate = :candidate AND a.appliedAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE', a.appliedAt) ORDER BY date")
    List<Object[]> countByCandidateAndDateRange(@Param("candidate") User candidate,
                                                 @Param("from") java.time.LocalDateTime from,
                                                 @Param("to") java.time.LocalDateTime to);

    @Query("SELECT FUNCTION('DATE', a.appliedAt) as date, COUNT(a) FROM Application a " +
           "WHERE a.job.recruiter = :recruiter AND a.appliedAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE', a.appliedAt) ORDER BY date")
    List<Object[]> countByRecruiterAndDateRange(@Param("recruiter") User recruiter,
                                                 @Param("from") java.time.LocalDateTime from,
                                                 @Param("to") java.time.LocalDateTime to);

    @Query("SELECT FUNCTION('DATE', a.appliedAt) as date, COUNT(a) FROM Application a " +
           "WHERE a.appliedAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE', a.appliedAt) ORDER BY date")
    List<Object[]> countByDateRange(@Param("from") java.time.LocalDateTime from,
                                     @Param("to") java.time.LocalDateTime to);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.candidate = :candidate GROUP BY a.status")
    List<Object[]> countStatusDistributionByCandidate(@Param("candidate") User candidate);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter GROUP BY a.status")
    List<Object[]> countStatusDistributionByRecruiter(@Param("recruiter") User recruiter);

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countAllStatusDistribution();

    @Query("SELECT a FROM Application a WHERE " +
           "(:jobId IS NULL OR a.job.id = :jobId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:candidateName IS NULL OR :candidateName = '' OR " +
           "LOWER(a.candidate.fullName) LIKE LOWER(CONCAT('%', :candidateName, '%')))")
    Page<Application> searchApplicationsCombined(
            @Param("jobId") Long jobId,
            @Param("status") ApplicationStatus status,
            @Param("candidateName") String candidateName,
            Pageable pageable);
}
