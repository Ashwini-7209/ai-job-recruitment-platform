package com.jobplatform.resume.parsing;

import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeCertification;
import com.jobplatform.resume.ResumeEducation;
import com.jobplatform.resume.ResumeExperience;
import com.jobplatform.resume.ResumeProfileData;
import com.jobplatform.resume.ResumeProject;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import com.jobplatform.resume.storage.FileStorageService;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeProfileDataRepository profileDataRepository;
    private final FileStorageService fileStorageService;
    private final ResumeTextExtractor textExtractor;
    private final ResumeAIParser aiParser;

    public ResumeParserService(
            ResumeRepository resumeRepository,
            ResumeProfileDataRepository profileDataRepository,
            FileStorageService fileStorageService,
            ResumeTextExtractor textExtractor,
            ResumeAIParser aiParser) {
        this.resumeRepository = resumeRepository;
        this.profileDataRepository = profileDataRepository;
        this.fileStorageService = fileStorageService;
        this.textExtractor = textExtractor;
        this.aiParser = aiParser;
    }

    @Transactional
    public ParseResult parseResume(User candidate, Long resumeId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can parse resumes");
        }

        Resume resume = resumeRepository.findByIdAndCandidate(resumeId, candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        if (resume.getParsingStatus() == ResumeParsingStatus.COMPLETED) {
            Optional<ResumeProfileData> existing = profileDataRepository.findByResume(resume);
            if (existing.isPresent()) {
                log.info("Resume already parsed, returning existing data: resumeId={}", resumeId);
                return buildResultFromExisting(resume, existing.get());
            }
        }

        resume.setParsingStatus(ResumeParsingStatus.PROCESSING);
        resume.setFailureReason(null);
        resumeRepository.save(resume);

        try {
            InputStream fileStream = fileStorageService.load(resume.getStoredFileName())
                    .orElseThrow(() -> new BadRequestException("Resume file not found on disk"));

            ExtractionResult extraction = textExtractor.extractText(fileStream, resume.getContentType());
            fileStream.close();

            if (extraction.getStatus() != ResumeParsingStatus.COMPLETED) {
                resume.setParsingStatus(extraction.getStatus());
                resume.setFailureReason(extraction.getFailureReason());
                resumeRepository.save(resume);

                return ParseResult.builder()
                        .resumeId(resumeId)
                        .parsingStatus(extraction.getStatus())
                        .failureReason(extraction.getFailureReason())
                        .build();
            }

            Optional<StructuredResumeData> structuredData = aiParser.parseResume(extraction.getExtractedText());

            if (structuredData.isEmpty()) {
                resume.setParsingStatus(ResumeParsingStatus.FAILED);
                resume.setFailureReason("AI parser failed to process the resume text");
                resumeRepository.save(resume);

                return ParseResult.builder()
                        .resumeId(resumeId)
                        .parsingStatus(ResumeParsingStatus.FAILED)
                        .failureReason("AI parser failed to process the resume text")
                        .build();
            }

            ResumeProfileData profileData = saveStructuredData(resume, candidate, structuredData.get());

            resume.setParsingStatus(ResumeParsingStatus.COMPLETED);
            resume.setParsedAt(LocalDateTime.now());
            resume.setFailureReason(null);
            resumeRepository.save(resume);

            log.info("Resume parsed successfully: resumeId={}, candidate={}", resumeId, candidate.getEmail());

            return buildResultFromExisting(resume, profileData);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Resume parsing failed: resumeId={}", resumeId, e);
            resume.setParsingStatus(ResumeParsingStatus.FAILED);
            resume.setFailureReason("Unexpected error during parsing");
            resumeRepository.save(resume);

            return ParseResult.builder()
                    .resumeId(resumeId)
                    .parsingStatus(ResumeParsingStatus.FAILED)
                    .failureReason("Unexpected error during parsing")
                    .build();
        }
    }

    @Transactional
    public void updateCandidateProfileFromResume(Long userId, ResumeProfileData profileData) {
        // This is called externally if needed - not auto-populating profile fields
        // Profile update from resume is opt-in, not automatic
    }

    @Transactional(readOnly = true)
    public Optional<ResumeProfileData> getResumeProfileData(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));
        return profileDataRepository.findByResume(resume);
    }

    private ResumeProfileData saveStructuredData(Resume resume, User candidate, StructuredResumeData data) {
        Optional<ResumeProfileData> existing = profileDataRepository.findByResume(resume);

        ResumeProfileData profileData;
        if (existing.isPresent()) {
            profileData = existing.get();
            profileData.getExperiences().clear();
            profileData.getEducation().clear();
            profileData.getCertifications().clear();
            profileData.getProjects().clear();
        } else {
            profileData = ResumeProfileData.builder()
                    .resume(resume)
                    .candidate(candidate)
                    .build();
        }

        if (data.getPersonalInfo() != null) {
            profileData.setFullName(data.getPersonalInfo().getFullName());
            profileData.setEmail(data.getPersonalInfo().getEmail());
            profileData.setPhone(data.getPersonalInfo().getPhone());
            profileData.setLocation(data.getPersonalInfo().getLocation());
        }
        profileData.setProfessionalSummary(data.getProfessionalSummary());
        profileData.setHeadline(data.getHeadline());
        profileData.setSkills(data.getSkills() != null ? String.join(", ", data.getSkills()) : null);
        profileData.setTotalYearsOfExperience(data.getTotalYearsOfExperience());
        profileData.setLanguages(data.getLanguages() != null ? String.join(", ", data.getLanguages()) : null);

        profileData = profileDataRepository.save(profileData);

        if (data.getExperience() != null) {
            for (StructuredResumeData.ExperienceData exp : data.getExperience()) {
                ResumeExperience experience = ResumeExperience.builder()
                        .resumeProfileData(profileData)
                        .company(exp.getCompany())
                        .jobTitle(exp.getJobTitle())
                        .location(exp.getLocation())
                        .startDate(exp.getStartDate())
                        .endDate(exp.getEndDate())
                        .current(exp.getCurrent() != null ? exp.getCurrent() : false)
                        .description(exp.getDescription())
                        .technologies(exp.getTechnologies() != null ? String.join(", ", exp.getTechnologies()) : null)
                        .build();
                profileData.getExperiences().add(experience);
            }
        }

        if (data.getEducation() != null) {
            for (StructuredResumeData.EducationData edu : data.getEducation()) {
                ResumeEducation education = ResumeEducation.builder()
                        .resumeProfileData(profileData)
                        .institution(edu.getInstitution())
                        .degree(edu.getDegree())
                        .fieldOfStudy(edu.getFieldOfStudy())
                        .startDate(edu.getStartDate())
                        .endDate(edu.getEndDate())
                        .grade(edu.getGrade())
                        .build();
                profileData.getEducation().add(education);
            }
        }

        if (data.getCertifications() != null) {
            for (StructuredResumeData.CertificationData cert : data.getCertifications()) {
                ResumeCertification certification = ResumeCertification.builder()
                        .resumeProfileData(profileData)
                        .name(cert.getName())
                        .issuingOrganization(cert.getIssuingOrganization())
                        .issueDate(cert.getIssueDate())
                        .expirationDate(cert.getExpirationDate())
                        .build();
                profileData.getCertifications().add(certification);
            }
        }

        if (data.getProjects() != null) {
            for (StructuredResumeData.ProjectData proj : data.getProjects()) {
                ResumeProject project = ResumeProject.builder()
                        .resumeProfileData(profileData)
                        .name(proj.getName())
                        .description(proj.getDescription())
                        .technologies(proj.getTechnologies() != null ? String.join(", ", proj.getTechnologies()) : null)
                        .url(proj.getUrl())
                        .build();
                profileData.getProjects().add(project);
            }
        }

        return profileDataRepository.save(profileData);
    }

    private ParseResult buildResultFromExisting(Resume resume, ResumeProfileData profileData) {
        return ParseResult.builder()
                .resumeId(resume.getId())
                .parsingStatus(resume.getParsingStatus())
                .parsedAt(resume.getParsedAt())
                .fullName(profileData.getFullName())
                .email(profileData.getEmail())
                .phone(profileData.getPhone())
                .location(profileData.getLocation())
                .professionalSummary(profileData.getProfessionalSummary())
                .headline(profileData.getHeadline())
                .skills(profileData.getSkills())
                .totalYearsOfExperience(profileData.getTotalYearsOfExperience())
                .languages(profileData.getLanguages())
                .build();
    }
}
