package com.jobplatform.job;

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

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByRecruiter(User recruiter, Pageable pageable);

    Page<Job> findByRecruiterAndStatus(User recruiter, JobStatus status, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    Page<Job> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String title, String location, Pageable pageable);

    long countByRecruiter(User recruiter);

    long countByRecruiterAndStatus(User recruiter, JobStatus status);

    long countByStatus(JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' AND (" +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Job> searchPublishedJobs(@Param("query") String query, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' " +
           "AND (:location IS NULL OR LOWER(j.location) = LOWER(:location)) " +
           "AND (:employmentType IS NULL OR j.employmentType = :employmentType) " +
           "AND (:workplaceType IS NULL OR j.workplaceType = :workplaceType) " +
           "AND (:experienceMin IS NULL OR j.experienceMax >= :experienceMin) " +
           "AND (:experienceMax IS NULL OR j.experienceMin <= :experienceMax) " +
           "AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin) " +
           "AND (:salaryMax IS NULL OR j.salaryMin <= :salaryMax)")
    Page<Job> findPublishedJobsWithFilters(
            @Param("location") String location,
            @Param("employmentType") EmploymentType employmentType,
            @Param("workplaceType") WorkplaceType workplaceType,
            @Param("experienceMin") Integer experienceMin,
            @Param("experienceMax") Integer experienceMax,
            @Param("salaryMin") Integer salaryMin,
            @Param("salaryMax") Integer salaryMax,
            Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'PUBLISHED' " +
           "AND (:query IS NULL OR :query = '' OR " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.recruiter.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:employmentType IS NULL OR j.employmentType = :employmentType) " +
           "AND (:workplaceType IS NULL OR j.workplaceType = :workplaceType) " +
           "AND (:experienceMin IS NULL OR j.experienceMax >= :experienceMin) " +
           "AND (:experienceMax IS NULL OR j.experienceMin <= :experienceMax) " +
           "AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin) " +
           "AND (:salaryMax IS NULL OR j.salaryMin <= :salaryMax) " +
           "AND (:skills IS NULL OR :skills = '' OR LOWER(j.skills) LIKE LOWER(CONCAT('%', :skills, '%'))) " +
           "AND (:companyName IS NULL OR :companyName = '' OR LOWER(j.recruiter.fullName) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "AND (:postedAfter IS NULL OR j.publishedAt >= :postedAfter)")
    Page<Job> searchPublishedJobsCombined(
            @Param("query") String query,
            @Param("location") String location,
            @Param("employmentType") EmploymentType employmentType,
            @Param("workplaceType") WorkplaceType workplaceType,
            @Param("experienceMin") Integer experienceMin,
            @Param("experienceMax") Integer experienceMax,
            @Param("salaryMin") Integer salaryMin,
            @Param("salaryMax") Integer salaryMax,
            @Param("skills") String skills,
            @Param("companyName") String companyName,
            @Param("postedAfter") java.time.LocalDateTime postedAfter,
            Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.recruiter = :recruiter ORDER BY j.createdAt DESC")
    Page<Job> findByRecruiterOrdered(@Param("recruiter") User recruiter, Pageable pageable);
}
