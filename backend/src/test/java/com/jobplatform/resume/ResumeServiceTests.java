package com.jobplatform.resume;

import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.dto.ResumeResponse;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResumeServiceTests {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedCandidate;
    private User savedRecruiter;

    @BeforeEach
    void setUp() {
        User candidate = User.builder()
                .fullName("Test Candidate")
                .email("candidate-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        savedCandidate = userRepository.save(candidate);

        User recruiter = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedRecruiter = userRepository.save(recruiter);
    }

    private byte[] createPdfContent() {
        return new byte[]{(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46, 0x00, 0x00, 0x00, 0x00};
    }

    @Test
    void uploadResume_success() {
        byte[] data = createPdfContent();

        ResumeResponse response = resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getOriginalFileName()).isEqualTo("resume.pdf");
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void uploadResume_firstResumeAutoActive() {
        byte[] data = createPdfContent();

        ResumeResponse response = resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data);

        assertThat(response.getActive()).isTrue();
    }

    @Test
    void uploadResume_secondResumeNotAutoActive() {
        byte[] data = createPdfContent();
        resumeService.uploadResume(savedCandidate, "resume1.pdf", "application/pdf", data);

        ResumeResponse response = resumeService.uploadResume(savedCandidate, "resume2.pdf", "application/pdf", data);

        assertThat(response.getActive()).isFalse();
    }

    @Test
    void uploadResume_throwsException_forNonCandidate() {
        byte[] data = createPdfContent();

        assertThatThrownBy(() -> resumeService.uploadResume(savedRecruiter, "resume.pdf", "application/pdf", data))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void uploadResume_throwsException_forEmptyFile() {
        assertThatThrownBy(() -> resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", new byte[]{}))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadResume_throwsException_forOversizedFile() {
        byte[] data = new byte[11 * 1024 * 1024];

        assertThatThrownBy(() -> resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds");
    }

    @Test
    void uploadResume_throwsException_forUnsupportedType() {
        byte[] data = "test content".getBytes();

        assertThatThrownBy(() -> resumeService.uploadResume(savedCandidate, "resume.exe", "application/octet-stream", data))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported");
    }

    @Test
    void uploadResume_throwsException_forInvalidSignature() {
        byte[] data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00, 0x00, 0x00, 0x00};

        assertThatThrownBy(() -> resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void getCandidateResumes_returnsList() {
        byte[] data = createPdfContent();
        resumeService.uploadResume(savedCandidate, "resume1.pdf", "application/pdf", data);
        resumeService.uploadResume(savedCandidate, "resume2.pdf", "application/pdf", data);

        List<ResumeResponse> resumes = resumeService.getCandidateResumes(savedCandidate);

        assertThat(resumes).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void getCandidateResumes_throwsException_forNonCandidate() {
        assertThatThrownBy(() -> resumeService.getCandidateResumes(savedRecruiter))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void getResumeById_returnsResume() {
        byte[] data = createPdfContent();
        ResumeResponse uploaded = resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data);

        ResumeResponse response = resumeService.getResumeById(savedCandidate, uploaded.getId());

        assertThat(response.getId()).isEqualTo(uploaded.getId());
        assertThat(response.getOriginalFileName()).isEqualTo("resume.pdf");
    }

    @Test
    void getResumeById_throwsException_whenNotOwner() {
        byte[] data = createPdfContent();
        ResumeResponse uploaded = resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data);

        assertThatThrownBy(() -> resumeService.getResumeById(savedRecruiter, uploaded.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void getResumeById_throwsException_whenNotFound() {
        assertThatThrownBy(() -> resumeService.getResumeById(savedCandidate, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadResume_returnsInputStream() {
        byte[] data = createPdfContent();
        ResumeResponse uploaded = resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data);

        InputStream stream = resumeService.downloadResume(savedCandidate, uploaded.getId());

        assertThat(stream).isNotNull();
    }

    @Test
    void activateResume_success() {
        byte[] data = createPdfContent();
        ResumeResponse r1 = resumeService.uploadResume(savedCandidate, "resume1.pdf", "application/pdf", data);
        ResumeResponse r2 = resumeService.uploadResume(savedCandidate, "resume2.pdf", "application/pdf", data);

        ResumeResponse activated = resumeService.activateResume(savedCandidate, r2.getId());

        assertThat(activated.getActive()).isTrue();

        ResumeResponse r1After = resumeService.getResumeById(savedCandidate, r1.getId());
        assertThat(r1After.getActive()).isFalse();
    }

    @Test
    void activateResume_throwsException_forNonCandidate() {
        assertThatThrownBy(() -> resumeService.activateResume(savedRecruiter, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void deleteResume_success() {
        byte[] data = createPdfContent();
        ResumeResponse uploaded = resumeService.uploadResume(savedCandidate, "resume.pdf", "application/pdf", data);

        resumeService.deleteResume(savedCandidate, uploaded.getId());

        assertThatThrownBy(() -> resumeService.getResumeById(savedCandidate, uploaded.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteResume_activatesNewest_whenActiveDeleted() {
        byte[] data = createPdfContent();
        ResumeResponse r1 = resumeService.uploadResume(savedCandidate, "resume1.pdf", "application/pdf", data);
        ResumeResponse r2 = resumeService.uploadResume(savedCandidate, "resume2.pdf", "application/pdf", data);

        resumeService.activateResume(savedCandidate, r2.getId());
        resumeService.deleteResume(savedCandidate, r2.getId());

        ResumeResponse r1After = resumeService.getResumeById(savedCandidate, r1.getId());
        assertThat(r1After.getActive()).isTrue();
    }

    @Test
    void deleteResume_throwsException_forNonCandidate() {
        assertThatThrownBy(() -> resumeService.deleteResume(savedRecruiter, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }
}
