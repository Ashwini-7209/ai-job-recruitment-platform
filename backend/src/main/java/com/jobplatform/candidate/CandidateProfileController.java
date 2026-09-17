package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CandidateProfileResponse;
import com.jobplatform.candidate.dto.CandidateProfileUpdateRequest;
import com.jobplatform.candidate.dto.ProfileCompletionResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
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
@RequestMapping("/api/candidates/me/profile")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    public CandidateProfileController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile() {
        User user = CurrentUserUtil.getCurrentUser();
        CandidateProfileResponse response = candidateProfileService.getProfileResponse(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @Valid @RequestBody CandidateProfileUpdateRequest request) {
        User user = CurrentUserUtil.getCurrentUser();
        CandidateProfileResponse response = candidateProfileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/completion")
    public ResponseEntity<ApiResponse<ProfileCompletionResponse>> getProfileCompletion() {
        User user = CurrentUserUtil.getCurrentUser();
        ProfileCompletionResponse response = candidateProfileService.getProfileCompletion(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
