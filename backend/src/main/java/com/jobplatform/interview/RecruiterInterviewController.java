package com.jobplatform.interview;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.interview.dto.CreateInterviewRequest;
import com.jobplatform.interview.dto.RecruiterInterviewResponse;
import com.jobplatform.interview.dto.UpdateInterviewRequest;
import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiters/me")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterInterviewController {

    private final InterviewService interviewService;

    public RecruiterInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<RecruiterInterviewResponse>> createInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterInterviewResponse response = interviewService.createInterview(recruiter, applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview scheduled successfully", response));
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<List<RecruiterInterviewResponse>>> getApplicationInterviews(
            @PathVariable Long applicationId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        List<RecruiterInterviewResponse> response = interviewService.getInterviewsByApplication(recruiter, applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/interviews")
    public ResponseEntity<ApiResponse<PagedResponse<RecruiterInterviewResponse>>> getMyInterviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        PagedResponse<RecruiterInterviewResponse> response = interviewService.getRecruiterInterviews(recruiter, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/interviews/upcoming")
    public ResponseEntity<ApiResponse<List<RecruiterInterviewResponse>>> getUpcomingInterviews() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        List<RecruiterInterviewResponse> response = interviewService.getUpcomingRecruiterInterviews(recruiter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/interviews/{interviewId}")
    public ResponseEntity<ApiResponse<RecruiterInterviewResponse>> getInterviewById(
            @PathVariable Long interviewId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterInterviewResponse response = interviewService.getRecruiterInterviewById(recruiter, interviewId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/interviews/{interviewId}")
    public ResponseEntity<ApiResponse<RecruiterInterviewResponse>> updateInterview(
            @PathVariable Long interviewId,
            @Valid @RequestBody UpdateInterviewRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterInterviewResponse response = interviewService.updateInterview(recruiter, interviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Interview updated successfully", response));
    }

    @PatchMapping("/interviews/{interviewId}/cancel")
    public ResponseEntity<ApiResponse<RecruiterInterviewResponse>> cancelInterview(
            @PathVariable Long interviewId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterInterviewResponse response = interviewService.cancelInterview(recruiter, interviewId);
        return ResponseEntity.ok(ApiResponse.success("Interview cancelled successfully", response));
    }

    @PatchMapping("/interviews/{interviewId}/status")
    public ResponseEntity<ApiResponse<RecruiterInterviewResponse>> updateInterviewStatus(
            @PathVariable Long interviewId,
            @RequestParam InterviewStatus status) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterInterviewResponse response = interviewService.updateInterviewStatus(recruiter, interviewId, status);
        return ResponseEntity.ok(ApiResponse.success("Interview status updated", response));
    }

    @GetMapping("/interview-stats")
    public ResponseEntity<ApiResponse<Long>> getUpcomingCount() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        long count = interviewService.getRecruiterUpcomingCount(recruiter);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
