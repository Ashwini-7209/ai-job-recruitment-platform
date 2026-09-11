package com.jobplatform.recruiter;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.recruiter.dto.RecruiterProfileResponse;
import com.jobplatform.recruiter.dto.RecruiterProfileUpdateRequest;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiters/me/profile")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;

    public RecruiterProfileController(RecruiterProfileService recruiterProfileService) {
        this.recruiterProfileService = recruiterProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> getProfile() {
        User user = CurrentUserUtil.getCurrentUser();
        RecruiterProfileResponse response = recruiterProfileService.getProfileResponse(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> updateProfile(
            @Valid @RequestBody RecruiterProfileUpdateRequest request) {
        User user = CurrentUserUtil.getCurrentUser();
        RecruiterProfileResponse response = recruiterProfileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
