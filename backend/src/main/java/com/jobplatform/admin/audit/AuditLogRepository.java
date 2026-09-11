package com.jobplatform.admin.audit;

import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByActorOrderByCreatedAtDesc(User actor, Pageable pageable);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:actorId IS NULL OR al.actor.id = :actorId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:entityType IS NULL OR al.entityType = :entityType) " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findFiltered(
            @Param("actorId") Long actorId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            Pageable pageable);
}
