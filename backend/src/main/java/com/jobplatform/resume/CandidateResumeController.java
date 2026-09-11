package com.jobplatform.resume;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.resume.dto.ResumeResponse;
import com.jobplatform.resume.parsing.ParseResult;
import com.jobplatform.resume.parsing.ResumeParserService;
import com.jobplatform.user.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/candidates/me/resumes")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateResumeController {

    private final ResumeService resumeService;
    private final ResumeParserService resumeParserService;

    public CandidateResumeController(ResumeService resumeService, ResumeParserService resumeParserService) {
        this.resumeService = resumeService;
        this.resumeParserService = resumeParserService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(
            @RequestParam("file") MultipartFile file) throws IOException {

        User candidate = CurrentUserUtil.getCurrentUser();
        ResumeResponse response = resumeService.uploadResume(
                candidate,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getMyResumes() {
        User candidate = CurrentUserUtil.getCurrentUser();
        List<ResumeResponse> resumes = resumeService.getCandidateResumes(candidate);
        return ResponseEntity.ok(ApiResponse.success(resumes));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeById(@PathVariable Long resumeId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        ResumeResponse response = resumeService.getResumeById(candidate, resumeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{resumeId}/download")
    public void downloadResume(@PathVariable Long resumeId, HttpServletResponse response) throws IOException {
        User candidate = CurrentUserUtil.getCurrentUser();
        InputStream inputStream = resumeService.downloadResume(candidate, resumeId);

        ResumeResponse resume = resumeService.getResumeById(candidate, resumeId);

        response.setContentType(resume.getContentType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resume.getOriginalFileName() + "\"");
        response.setContentLength(resume.getFileSize().intValue());

        inputStream.transferTo(response.getOutputStream());
        inputStream.close();
    }

    @PostMapping("/{resumeId}/activate")
    public ResponseEntity<ApiResponse<ResumeResponse>> activateResume(@PathVariable Long resumeId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        ResumeResponse response = resumeService.activateResume(candidate, resumeId);
        return ResponseEntity.ok(ApiResponse.success("Resume activated successfully", response));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable Long resumeId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        resumeService.deleteResume(candidate, resumeId);
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully", null));
    }

    @PostMapping("/{resumeId}/parse")
    public ResponseEntity<ApiResponse<ParseResult>> parseResume(@PathVariable Long resumeId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        ParseResult result = resumeParserService.parseResume(candidate, resumeId);
        return ResponseEntity.ok(ApiResponse.success("Resume parsing completed", result));
    }
}
