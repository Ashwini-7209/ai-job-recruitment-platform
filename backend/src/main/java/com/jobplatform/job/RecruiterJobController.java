package com.jobplatform.job;

import com.jobplatform.ai.ChatAIProvider;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.dto.JobResponse;
import com.jobplatform.job.dto.JobSummaryResponse;
import com.jobplatform.job.dto.RecruiterJobStats;
import com.jobplatform.job.dto.UpdateJobRequest;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recruiter/jobs")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterJobController {

    private static final Logger log = LoggerFactory.getLogger(RecruiterJobController.class);

    private final JobService jobService;
    private final ChatAIProvider chatAIProvider;

    public RecruiterJobController(JobService jobService, ChatAIProvider chatAIProvider) {
        this.jobService = jobService;
        this.chatAIProvider = chatAIProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody CreateJobRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.createJob(recruiter, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Job created successfully", response));
    }

    @PostMapping("/generate-description")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateJobDescription(
            @RequestBody Map<String, Object> request) {
        String title = (String) request.getOrDefault("title", "");
        String skills = (String) request.getOrDefault("skills", "");
        String employmentType = (String) request.getOrDefault("employmentType", "");
        String workplaceType = (String) request.getOrDefault("workplaceType", "");
        Object experienceMinObj = request.get("experienceMin");
        Object experienceMaxObj = request.get("experienceMax");
        int experienceMin = experienceMinObj instanceof Number ? ((Number) experienceMinObj).intValue() : 0;
        int experienceMax = experienceMaxObj instanceof Number ? ((Number) experienceMaxObj).intValue() : 0;

        String systemPrompt = """
                You are an expert technical recruiter and job description writer. \
                Generate a professional, clear, and compelling job description based on the provided details. \
                Include sections for role overview, responsibilities, and requirements. \
                Keep it concise but thorough. Use formatting with line breaks for readability.""";

        String userPrompt = String.format("""
                Generate a job description for the following position:

                Job Title: %s
                Required Skills: %s
                Employment Type: %s
                Workplace Type: %s
                Experience Range: %d-%d years

                Write a professional job description that would attract qualified candidates.""",
                title, skills, employmentType, workplaceType, experienceMin, experienceMax);

        if (!chatAIProvider.isAvailable()) {
            log.warn("AI provider not available for job description generation");
            return ResponseEntity.ok(ApiResponse.error("AI service is not available. Please write the description manually."));
        }

        var result = chatAIProvider.chat(systemPrompt, userPrompt, 1500);
        if (result.isEmpty()) {
            log.warn("AI failed to generate job description");
            return ResponseEntity.ok(ApiResponse.error("Unable to generate description. Please write manually."));
        }

        Map<String, String> response = Map.of("description", result.get());
        return ResponseEntity.ok(ApiResponse.success("Description generated successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<JobSummaryResponse>>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        PagedResponse<JobSummaryResponse> response = jobService.getRecruiterJobs(recruiter, page, size, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.getRecruiterJobById(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.updateJob(recruiter, id, request);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<JobResponse>> publishJob(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.publishJob(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success("Job published successfully", response));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.closeJob(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success("Job closed successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        jobService.deleteJob(recruiter, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RecruiterJobStats>> getStats() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterJobStats stats = jobService.getRecruiterStats(recruiter);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
