package com.jobplatform.resume;

import com.jobplatform.application.Application;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.dto.ResumeResponse;
import com.jobplatform.user.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/recruiters/me/applications")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterResumeController {

    private final ApplicationRepository applicationRepository;
    private final ResumeService resumeService;

    public RecruiterResumeController(ApplicationRepository applicationRepository, ResumeService resumeService) {
        this.applicationRepository = applicationRepository;
        this.resumeService = resumeService;
    }

    @GetMapping("/{applicationId}/resume")
    public void downloadApplicationResume(@PathVariable Long applicationId, HttpServletResponse response) throws IOException {
        User recruiter = CurrentUserUtil.getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to access this application");
        }

        if (application.getSubmittedResume() == null) {
            throw new BadRequestException("No resume associated with this application");
        }

        Resume resume = application.getSubmittedResume();

        InputStream inputStream = resumeService.getResumeStream(resume.getStoredFileName())
                .orElseThrow(() -> new BadRequestException("Resume file not found on disk"));

        response.setContentType(resume.getContentType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resume.getOriginalFileName() + "\"");
        response.setContentLength(resume.getFileSize().intValue());

        inputStream.transferTo(response.getOutputStream());
        inputStream.close();
    }
}
