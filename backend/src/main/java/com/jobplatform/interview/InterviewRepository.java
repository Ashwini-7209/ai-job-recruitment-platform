package com.jobplatform.interview;

import com.jobplatform.application.Application;
import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // Recruiter queries
    List<Interview> findByApplicationJobRecruiterAndScheduledStartAfterOrderByScheduledStartAsc(User recruiter, LocalDateTime now);

    Page<Interview> findByApplicationJobRecruiterOrderByScheduledStartDesc(User recruiter, Pageable pageable);

    Optional<Interview> findByIdAndApplicationJobRecruiter(Long id, User recruiter);

    long countByApplicationJobRecruiter(User recruiter);

    long countByApplicationJobRecruiterAndStatus(User recruiter, InterviewStatus status);

    long countByApplicationJobRecruiterAndScheduledStartAfter(User recruiter, LocalDateTime now);

    @Query("SELECT i FROM Interview i WHERE i.application.job.recruiter = :recruiter AND " +
           "i.scheduledStart >= :start AND i.scheduledStart <= :end " +
           "ORDER BY i.scheduledStart ASC")
    List<Interview> findUpcomingByRecruiter(@Param("recruiter") User recruiter,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    // Candidate queries
    List<Interview> findByApplicationCandidateAndScheduledStartAfterOrderByScheduledStartAsc(User candidate, LocalDateTime now);

    Page<Interview> findByApplicationCandidateOrderByScheduledStartDesc(User candidate, Pageable pageable);

    Optional<Interview> findByIdAndApplicationCandidate(Long id, User candidate);

    long countByApplicationCandidateAndScheduledStartAfter(User candidate, LocalDateTime now);

    @Query("SELECT i FROM Interview i WHERE i.application.candidate = :candidate AND " +
           "i.scheduledStart >= :start AND i.scheduledStart <= :end " +
           "ORDER BY i.scheduledStart ASC")
    List<Interview> findUpcomingByCandidate(@Param("candidate") User candidate,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    // Check for overlapping interviews
    boolean existsByApplicationAndScheduledStartLessThanEqualAndScheduledEndGreaterThanEqualAndStatusNot(
            Application application, LocalDateTime end, LocalDateTime start, InterviewStatus status);

    // Analytics queries
    long countByApplicationCandidate(User candidate);

    @Query("SELECT i.status, COUNT(i) FROM Interview i WHERE i.application.job.recruiter = :recruiter GROUP BY i.status")
    List<Object[]> countStatusDistributionByRecruiter(@Param("recruiter") User recruiter);

    @Query("SELECT i.status, COUNT(i) FROM Interview i GROUP BY i.status")
    List<Object[]> countAllStatusDistribution();
}
