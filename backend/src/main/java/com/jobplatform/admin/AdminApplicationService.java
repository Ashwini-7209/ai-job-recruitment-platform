package com.jobplatform.admin;

import com.jobplatform.admin.dto.AdminApplicationResponse;
import com.jobplatform.application.Application;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AdminApplicationService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final ApplicationRepository applicationRepository;

    public AdminApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminApplicationResponse> getApplications(Long jobId, ApplicationStatus status,
                                                                     String candidateName, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "appliedAt"));

        Page<Application> applications = applicationRepository.searchApplicationsCombined(
                jobId, status, candidateName, pageable);

        return PagedResponse.<AdminApplicationResponse>builder()
                .content(applications.getContent().stream().map(this::mapToResponse).toList())
                .page(applications.getNumber())
                .size(applications.getSize())
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .last(applications.isLast())
                .build();
    }

    private AdminApplicationResponse mapToResponse(Application application) {
        return AdminApplicationResponse.builder()
                .id(application.getId())
                .candidateName(application.getCandidate().getFullName())
                .candidateEmail(application.getCandidate().getEmail())
                .jobTitle(application.getJob().getTitle())
                .recruiterName(application.getJob().getRecruiter().getFullName())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
