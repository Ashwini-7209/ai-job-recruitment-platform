package com.jobplatform.matching;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.matching.dto.CareerInsightsResponse;
import com.jobplatform.matching.dto.JobMatchResponse;
import com.jobplatform.matching.dto.RecommendationResponse;
import com.jobplatform.matching.dto.ResumeImprovementResponse;
import com.jobplatform.matching.dto.SkillGapResponse;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates/me")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateJobMatchingController {

    private final JobMatchingService jobMatchingService;
    private final JobRecommendationService jobRecommendationService;
    private final SkillGapService skillGapService;
    private final CareerInsightsService careerInsightsService;
    private final ResumeImprovementService resumeImprovementService;
    private final JobRepository jobRepository;

    public CandidateJobMatchingController(
            JobMatchingService jobMatchingService,
            JobRecommendationService jobRecommendationService,
            SkillGapService skillGapService,
            CareerInsightsService careerInsightsService,
            ResumeImprovementService resumeImprovementService,
            JobRepository jobRepository) {
        this.jobMatchingService = jobMatchingService;
        this.jobRecommendationService = jobRecommendationService;
        this.skillGapService = skillGapService;
        this.careerInsightsService = careerInsightsService;
        this.resumeImprovementService = resumeImprovementService;
        this.jobRepository = jobRepository;
    }

    @GetMapping("/jobs/{jobId}/match")
    public ResponseEntity<ApiResponse<JobMatchResponse>> getJobMatch(@PathVariable Long jobId) {
        User candidate = CurrentUserUtil.getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new com.jobplatform.exception.ResourceNotFoundException("Job", "id", jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new com.jobplatform.exception.ResourceNotFoundException("Job", "id", jobId);
        }

        JobMatchingService.JobMatchResult result = jobMatchingService.calculateMatch(candidate, job);

        return ResponseEntity.ok(ApiResponse.success("Match calculated successfully",
                JobMatchResponse.from(result)));
    }

    @GetMapping("/jobs/{jobId}/skill-gap")
    public ResponseEntity<ApiResponse<SkillGapResponse>> getSkillGap(@PathVariable Long jobId) {
        User candidate = CurrentUserUtil.getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new com.jobplatform.exception.ResourceNotFoundException("Job", "id", jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new com.jobplatform.exception.ResourceNotFoundException("Job", "id", jobId);
        }

        SkillGapResponse response = skillGapService.analyzeSkillGap(candidate, job);

        return ResponseEntity.ok(ApiResponse.success("Skill gap analysis completed", response));
    }

    @GetMapping("/career-insights")
    public ResponseEntity<ApiResponse<CareerInsightsResponse>> getCareerInsights() {
        User candidate = CurrentUserUtil.getCurrentUser();
        CareerInsightsResponse response = careerInsightsService.getCareerInsights(candidate);
        return ResponseEntity.ok(ApiResponse.success("Career insights generated", response));
    }

    @PostMapping("/resumes/{resumeId}/improvement-analysis")
    public ResponseEntity<ApiResponse<ResumeImprovementResponse>> analyzeResumeImprovement(
            @PathVariable Long resumeId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        ResumeImprovementResponse response = resumeImprovementService.analyzeResume(candidate, resumeId);
        return ResponseEntity.ok(ApiResponse.success("Resume analysis completed", response));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer minScore) {

        User candidate = CurrentUserUtil.getCurrentUser();

        size = Math.min(size, 50);

        JobRecommendationService.RecommendationResult result =
                jobRecommendationService.getRecommendations(candidate, page, size, minScore);

        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved",
                RecommendationResponse.from(result)));
    }
}
