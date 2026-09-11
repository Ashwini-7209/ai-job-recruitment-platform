package com.jobplatform.interview;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.interview.dto.CandidateInterviewResponse;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidates/me")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateInterviewController {

    private final InterviewService interviewService;

    public CandidateInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/interviews")
    public ResponseEntity<ApiResponse<PagedResponse<CandidateInterviewResponse>>> getMyInterviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        User candidate = CurrentUserUtil.getCurrentUser();
        PagedResponse<CandidateInterviewResponse> response = interviewService.getCandidateInterviews(candidate, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/interviews/upcoming")
    public ResponseEntity<ApiResponse<List<CandidateInterviewResponse>>> getUpcomingInterviews() {
        User candidate = CurrentUserUtil.getCurrentUser();
        List<CandidateInterviewResponse> response = interviewService.getUpcomingCandidateInterviews(candidate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/interviews/{interviewId}")
    public ResponseEntity<ApiResponse<CandidateInterviewResponse>> getInterviewById(
            @PathVariable Long interviewId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        CandidateInterviewResponse response = interviewService.getCandidateInterviewById(candidate, interviewId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/interview-stats")
    public ResponseEntity<ApiResponse<Long>> getUpcomingCount() {
        User candidate = CurrentUserUtil.getCurrentUser();
        long count = interviewService.getCandidateUpcomingCount(candidate);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
