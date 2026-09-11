package com.jobplatform.admin;

import com.jobplatform.admin.dto.AdminApplicationResponse;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.PagedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/applications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    public AdminApplicationController(AdminApplicationService adminApplicationService) {
        this.adminApplicationService = adminApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminApplicationResponse>>> getApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String candidateName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AdminApplicationResponse> response = adminApplicationService.getApplications(
                jobId, status, candidateName, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
