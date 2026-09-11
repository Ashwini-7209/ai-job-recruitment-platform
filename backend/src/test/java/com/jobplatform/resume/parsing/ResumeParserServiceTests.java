package com.jobplatform.resume.parsing;

import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.resume.ResumeProfileData;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import com.jobplatform.resume.storage.FileStorageService;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeParserServiceTests {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeProfileDataRepository profileDataRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ResumeTextExtractor textExtractor;

    @Mock
    private ResumeAIParser aiParser;

    @InjectMocks
    private ResumeParserService resumeParserService;

    private User candidate;
    private Resume resume;

    @BeforeEach
    void setUp() {
        candidate = User.builder()
                .id(1L)
                .email("candidate@example.com")
                .role(UserRole.CANDIDATE)
                .build();

        resume = Resume.builder()
                .id(10L)
                .candidate(candidate)
                .storedFileName("test-resume.pdf")
                .contentType("application/pdf")
                .parsingStatus(ResumeParsingStatus.NOT_PROCESSED)
                .build();
    }

    @Test
    void parseResume_notCandidate_throwsException() {
        User recruiter = User.builder().id(2L).role(UserRole.RECRUITER).build();

        assertThatThrownBy(() -> resumeParserService.parseResume(recruiter, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void parseResume_resumeNotFound_throwsException() {
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeParserService.parseResume(candidate, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void parseResume_alreadyCompleted_returnsExistingData() {
        resume.setParsingStatus(ResumeParsingStatus.COMPLETED);
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.of(resume));

        ResumeProfileData existingData = ResumeProfileData.builder()
                .resume(resume)
                .candidate(candidate)
                .fullName("John Doe")
                .build();
        when(profileDataRepository.findByResume(resume))
                .thenReturn(Optional.of(existingData));

        ParseResult result = resumeParserService.parseResume(candidate, 10L);

        assertThat(result.getParsingStatus()).isEqualTo(ResumeParsingStatus.COMPLETED);
        assertThat(result.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void parseResume_scanRequired_returnsScanRequired() throws Exception {
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.of(resume));

        InputStream stream = new ByteArrayInputStream("test".getBytes());
        when(fileStorageService.load("test-resume.pdf")).thenReturn(Optional.of(stream));

        ExtractionResult extractionResult = ExtractionResult.builder()
                .status(ResumeParsingStatus.SCAN_REQUIRED)
                .failureReason("PDF contains no extractable text")
                .build();
        when(textExtractor.extractText(any(), eq("application/pdf")))
                .thenReturn(extractionResult);

        ParseResult result = resumeParserService.parseResume(candidate, 10L);

        assertThat(result.getParsingStatus()).isEqualTo(ResumeParsingStatus.SCAN_REQUIRED);
        assertThat(result.getFailureReason()).contains("no extractable text");
    }

    @Test
    void parseResume_aiParserFails_returnsFailed() throws Exception {
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.of(resume));

        InputStream stream = new ByteArrayInputStream("test".getBytes());
        when(fileStorageService.load("test-resume.pdf")).thenReturn(Optional.of(stream));

        ExtractionResult extractionResult = ExtractionResult.builder()
                .status(ResumeParsingStatus.COMPLETED)
                .extractedText("Sample resume text")
                .build();
        when(textExtractor.extractText(any(), eq("application/pdf")))
                .thenReturn(extractionResult);

        when(aiParser.parseResume("Sample resume text")).thenReturn(Optional.empty());

        ParseResult result = resumeParserService.parseResume(candidate, 10L);

        assertThat(result.getParsingStatus()).isEqualTo(ResumeParsingStatus.FAILED);
        assertThat(result.getFailureReason()).contains("AI parser failed");
    }

    @Test
    void parseResume_successfulParsing_returnsStructuredData() throws Exception {
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.of(resume));

        InputStream stream = new ByteArrayInputStream("test".getBytes());
        when(fileStorageService.load("test-resume.pdf")).thenReturn(Optional.of(stream));

        ExtractionResult extractionResult = ExtractionResult.builder()
                .status(ResumeParsingStatus.COMPLETED)
                .extractedText("Sample resume text")
                .build();
        when(textExtractor.extractText(any(), eq("application/pdf")))
                .thenReturn(extractionResult);

        StructuredResumeData structuredData = StructuredResumeData.builder()
                .personalInfo(StructuredResumeData.PersonalInfo.builder()
                        .fullName("John Doe")
                        .email("john@example.com")
                        .build())
                .skills(java.util.List.of("Java", "Spring"))
                .experience(java.util.List.of())
                .education(java.util.List.of())
                .certifications(java.util.List.of())
                .projects(java.util.List.of())
                .build();
        when(aiParser.parseResume("Sample resume text")).thenReturn(Optional.of(structuredData));

        when(profileDataRepository.findByResume(resume)).thenReturn(Optional.empty());
        when(profileDataRepository.save(any(ResumeProfileData.class)))
                .thenAnswer(invocation -> {
                    ResumeProfileData data = invocation.getArgument(0);
                    data.setId(1L);
                    return data;
                });
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        ParseResult result = resumeParserService.parseResume(candidate, 10L);

        assertThat(result.getParsingStatus()).isEqualTo(ResumeParsingStatus.COMPLETED);
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void parseResume_reparse_replacesExistingData() throws Exception {
        resume.setParsingStatus(ResumeParsingStatus.FAILED);
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.of(resume));

        InputStream stream = new ByteArrayInputStream("test".getBytes());
        when(fileStorageService.load("test-resume.pdf")).thenReturn(Optional.of(stream));

        ExtractionResult extractionResult = ExtractionResult.builder()
                .status(ResumeParsingStatus.COMPLETED)
                .extractedText("Sample resume text")
                .build();
        when(textExtractor.extractText(any(), eq("application/pdf")))
                .thenReturn(extractionResult);

        StructuredResumeData structuredData = StructuredResumeData.builder()
                .personalInfo(StructuredResumeData.PersonalInfo.builder()
                        .fullName("Jane Smith")
                        .build())
                .skills(java.util.List.of("Python"))
                .experience(java.util.List.of())
                .education(java.util.List.of())
                .certifications(java.util.List.of())
                .projects(java.util.List.of())
                .build();
        when(aiParser.parseResume("Sample resume text")).thenReturn(Optional.of(structuredData));

        ResumeProfileData existingData = ResumeProfileData.builder()
                .resume(resume)
                .candidate(candidate)
                .fullName("Old Name")
                .build();
        when(profileDataRepository.findByResume(resume)).thenReturn(Optional.of(existingData));
        when(profileDataRepository.save(any(ResumeProfileData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        ParseResult result = resumeParserService.parseResume(candidate, 10L);

        assertThat(result.getParsingStatus()).isEqualTo(ResumeParsingStatus.COMPLETED);
        assertThat(result.getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    void parseResume_fileNotFound_throwsException() throws Exception {
        when(resumeRepository.findByIdAndCandidate(10L, candidate))
                .thenReturn(Optional.of(resume));
        when(fileStorageService.load("test-resume.pdf")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeParserService.parseResume(candidate, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not found on disk");
    }
}
