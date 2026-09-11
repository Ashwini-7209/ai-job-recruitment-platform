package com.jobplatform.admin;

import com.jobplatform.admin.dto.AdminDashboardStats;
import com.jobplatform.admin.dto.AdminUserResponse;
import com.jobplatform.admin.dto.UpdateUserRoleRequest;
import com.jobplatform.admin.dto.UpdateUserStatusRequest;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AdminUserResponse> response = adminUserService.getUsers(q, role, enabled, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(@PathVariable Long userId) {
        AdminUserResponse response = adminUserService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        User admin = CurrentUserUtil.getCurrentUser();
        AdminUserResponse response = adminUserService.updateUserStatus(admin, userId, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated", response));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        User admin = CurrentUserUtil.getCurrentUser();
        AdminUserResponse response = adminUserService.updateUserRole(admin, userId, request);
        return ResponseEntity.ok(ApiResponse.success("User role updated", response));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<AdminDashboardStats>> getDashboardStats() {
        AdminDashboardStats stats = adminUserService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
