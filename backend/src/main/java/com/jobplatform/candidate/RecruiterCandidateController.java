package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CandidateSearchResultResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiters/candidates")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterCandidateController {

    private final CandidateSearchService candidateSearchService;

    public RecruiterCandidateController(CandidateSearchService candidateSearchService) {
        this.candidateSearchService = candidateSearchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CandidateSearchResultResponse>>> searchCandidates(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name_asc") String sort) {

        User recruiter = CurrentUserUtil.getCurrentUser();
        PagedResponse<CandidateSearchResultResponse> response = candidateSearchService.searchCandidates(
                recruiter, q, location, skills, minExperience, maxExperience, jobTitle,
                page, size, sort);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
