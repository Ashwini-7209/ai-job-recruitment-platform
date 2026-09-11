package com.jobplatform.admin;

import com.jobplatform.admin.audit.AuditLog;
import com.jobplatform.admin.audit.AuditLogRepository;
import com.jobplatform.admin.dto.AdminDashboardStats;
import com.jobplatform.admin.dto.AdminUserResponse;
import com.jobplatform.admin.dto.UpdateUserRoleRequest;
import com.jobplatform.admin.dto.UpdateUserStatusRequest;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.JobRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminUserService(UserRepository userRepository, ApplicationRepository applicationRepository,
                            JobRepository jobRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> getUsers(String query, UserRole role, Boolean enabled,
                                                      int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users = userRepository.searchUsersCombined(query, role, enabled, pageable);

        return PagedResponse.<AdminUserResponse>builder()
                .content(users.getContent().stream().map(this::mapToResponse).toList())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUserStatus(User admin, Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getId().equals(admin.getId())) {
            throw new BadRequestException("Cannot deactivate your own account");
        }

        if (!request.getEnabled() && user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRoleAndEnabled(UserRole.ADMIN, true);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot deactivate the last active administrator");
            }
        }

        user.setEnabled(request.getEnabled());
        User updated = userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .actor(admin)
                .action(request.getEnabled() ? "USER_ENABLED" : "USER_DISABLED")
                .entityType("USER")
                .entityId(userId)
                .description("User " + user.getEmail() + " was " + (request.getEnabled() ? "enabled" : "disabled"))
                .build());

        log.info("User status updated by admin {}: userId={} enabled={}", admin.getEmail(), userId, request.getEnabled());
        return mapToResponse(updated);
    }

    @Transactional
    public AdminUserResponse updateUserRole(User admin, Long userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getId().equals(admin.getId())) {
            throw new BadRequestException("Cannot change your own role");
        }

        if (user.getRole() == UserRole.ADMIN && request.getRole() != UserRole.ADMIN) {
            long adminCount = userRepository.countByRoleAndEnabled(UserRole.ADMIN, true);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot remove role from the last active administrator");
            }
        }

        UserRole oldRole = user.getRole();
        user.setRole(request.getRole());
        User updated = userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .actor(admin)
                .action("ROLE_CHANGED")
                .entityType("USER")
                .entityId(userId)
                .description("User " + user.getEmail() + " role changed from " + oldRole + " to " + request.getRole())
                .build());

        log.info("User role updated by admin {}: userId={} role={}", admin.getEmail(), userId, request.getRole());
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCandidates = userRepository.countByRole(UserRole.CANDIDATE);
        long totalRecruiters = userRepository.countByRole(UserRole.RECRUITER);
        long activeUsers = userRepository.countByEnabled(true);
        long totalJobs = jobRepository.count();
        long publishedJobs = jobRepository.countByStatus(com.jobplatform.job.enums.JobStatus.PUBLISHED);
        long totalApplications = applicationRepository.count();

        return AdminDashboardStats.builder()
                .totalUsers(totalUsers)
                .totalCandidates(totalCandidates)
                .totalRecruiters(totalRecruiters)
                .activeUsers(activeUsers)
                .totalJobs(totalJobs)
                .publishedJobs(publishedJobs)
                .totalApplications(totalApplications)
                .build();
    }

    private AdminUserResponse mapToResponse(User user) {
        long applicationCount = 0;
        long jobCount = 0;

        if (user.getRole() == UserRole.CANDIDATE) {
            applicationCount = applicationRepository.countByCandidate(user);
        } else if (user.getRole() == UserRole.RECRUITER) {
            jobCount = jobRepository.countByRecruiter(user);
        }

        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .applicationCount(applicationCount)
                .jobCount(jobCount)
                .build();
    }
}
