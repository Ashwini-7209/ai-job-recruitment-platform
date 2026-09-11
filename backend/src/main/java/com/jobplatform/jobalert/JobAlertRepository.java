package com.jobplatform.jobalert;

import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {

    Page<JobAlert> findByCandidateOrderByCreatedAtDesc(User candidate, Pageable pageable);

    List<JobAlert> findByCandidateAndActiveTrue(User candidate);

    @Query("SELECT ja FROM JobAlert ja WHERE ja.active = true")
    List<JobAlert> findAllActiveAlerts();

    long countByCandidateAndActiveTrue(User candidate);

    boolean existsByCandidateAndName(User candidate, String name);

    @Query("SELECT ja FROM JobAlert ja WHERE ja.active = true AND ja.candidate.id IN :candidateIds")
    List<JobAlert> findActiveAlertsByCandidateIds(@Param("candidateIds") List<Long> candidateIds);
}
