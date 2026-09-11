package com.jobplatform.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByEnabled(Boolean enabled, Pageable pageable);

    long countByRole(UserRole role);

    long countByEnabled(Boolean enabled);

    long countByRoleAndEnabled(UserRole role, Boolean enabled);

    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> searchUsersCombined(
            @Param("query") String query,
            @Param("role") UserRole role,
            @Param("enabled") Boolean enabled,
            Pageable pageable);

    @Query("SELECT FUNCTION('DATE', u.createdAt) as date, COUNT(u) FROM User u " +
           "WHERE u.createdAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY date")
    List<Object[]> countByDateRange(@Param("from") java.time.LocalDateTime from,
                                     @Param("to") java.time.LocalDateTime to);

    @Query("SELECT FUNCTION('DATE', u.createdAt) as date, COUNT(u) FROM User u " +
           "WHERE u.createdAt BETWEEN :from AND :to AND u.role = :roleFilter " +
           "GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY date")
    List<Object[]> countByDateRangeAndRole(@Param("from") java.time.LocalDateTime from,
                                            @Param("to") java.time.LocalDateTime to,
                                            @Param("roleFilter") UserRole roleFilter);
}
