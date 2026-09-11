package com.jobplatform.matching;

import com.jobplatform.candidate.CandidateProfile;
import com.jobplatform.candidate.CandidateProfileRepository;
import com.jobplatform.job.Job;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeProfileData;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import com.jobplatform.resume.parsing.ResumeProfileDataRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobMatchingServiceTests {

    @Mock
    private CandidateDataResolver candidateDataResolver;

    @Mock
    private JobRequirementResolver jobRequirementResolver;

    @Mock
    private RuleBasedMatchingEngine matchingEngine;

    @Mock
    private AIMatchingService aiMatchingService;

    @InjectMocks
    private JobMatchingService jobMatchingService;

    private User candidate;
    private Job job;

    @BeforeEach
    void setUp() {
        candidate = User.builder()
                .id(1L)
                .email("candidate@example.com")
                .role(UserRole.CANDIDATE)
                .fullName("Test Candidate")
                .build();

        job = Job.builder()
                .id(10L)
                .title("Java Developer")
                .description("Develop Java applications")
                .status(JobStatus.PUBLISHED)
                .skills("Java,Spring Boot,MySQL")
                .experienceMin(2)
                .experienceMax(5)
                .recruiter(User.builder().id(2L).role(UserRole.RECRUITER).build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void calculateMatch_deterministicOnly() {
        CandidateData candidateData = CandidateData.builder()
                .skills(List.of("java", "spring boot"))
                .yearsOfExperience(3)
                .build();
        JobData jobData = JobData.builder()
                .requiredSkills(List.of("java", "spring boot", "mysql"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(jobRequirementResolver.resolve(job)).thenReturn(jobData);
        when(matchingEngine.calculate(candidateData, jobData))
                .thenReturn(RuleBasedMatchingEngine.DeterministicScore.builder()
                        .overallScore(65)
                        .skillScore(67)
                        .experienceScore(100)
                        .profileScore(50)
                        .matchedSkills(List.of("java", "spring boot"))
                        .missingSkills(List.of("mysql"))
                        .matchingReasons(List.of("2 of 3 skills matched"))
                        .potentialGaps(List.of("Missing: mysql"))
                        .build());
        when(aiMatchingService.evaluateMatch(candidateData, jobData, 1L)).thenReturn(Optional.empty());

        JobMatchingService.JobMatchResult result = jobMatchingService.calculateMatch(candidate, job);

        assertThat(result.getOverallScore()).isEqualTo(65);
        assertThat(result.getSemanticScore()).isNull();
        assertThat(result.getMatchedSkills()).containsExactlyInAnyOrder("java", "spring boot");
        assertThat(result.getMissingSkills()).containsExactlyInAnyOrder("mysql");
    }

    @Test
    void calculateMatch_withAI() {
        CandidateData candidateData = CandidateData.builder()
                .skills(List.of("java", "spring boot"))
                .yearsOfExperience(3)
                .build();
        JobData jobData = JobData.builder()
                .requiredSkills(List.of("java", "spring boot"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(jobRequirementResolver.resolve(job)).thenReturn(jobData);
        when(matchingEngine.calculate(candidateData, jobData))
                .thenReturn(RuleBasedMatchingEngine.DeterministicScore.builder()
                        .overallScore(70)
                        .skillScore(100)
                        .experienceScore(100)
                        .profileScore(50)
                        .matchedSkills(List.of("java", "spring boot"))
                        .missingSkills(List.of())
                        .matchingReasons(List.of("All skills matched"))
                        .potentialGaps(List.of())
                        .build());
        when(aiMatchingService.evaluateMatch(candidateData, jobData, 1L))
                .thenReturn(Optional.of(AIMatchingService.AISemanticResult.builder()
                        .relevanceScore(80)
                        .matchingReasons(List.of("Strong match"))
                        .potentialGaps(List.of())
                        .evidence(List.of("Experience aligns"))
                        .build()));

        JobMatchingService.JobMatchResult result = jobMatchingService.calculateMatch(candidate, job);

        assertThat(result.getSemanticScore()).isEqualTo(80);
        assertThat(result.getOverallScore()).isBetween(0, 100);
        assertThat(result.getAiEvidence()).contains("Experience aligns");
    }

    @Test
    void calculateMatch_aiFails_fallsBackToDeterministic() {
        CandidateData candidateData = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(3)
                .build();
        JobData jobData = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(jobRequirementResolver.resolve(job)).thenReturn(jobData);
        when(matchingEngine.calculate(candidateData, jobData))
                .thenReturn(RuleBasedMatchingEngine.DeterministicScore.builder()
                        .overallScore(75)
                        .skillScore(100)
                        .experienceScore(100)
                        .profileScore(50)
                        .matchedSkills(List.of("java"))
                        .missingSkills(List.of())
                        .matchingReasons(List.of("Match found"))
                        .potentialGaps(List.of())
                        .build());
        when(aiMatchingService.evaluateMatch(candidateData, jobData, 1L))
                .thenThrow(new RuntimeException("AI unavailable"));

        JobMatchingService.JobMatchResult result = jobMatchingService.calculateMatch(candidate, job);

        assertThat(result.getOverallScore()).isEqualTo(75);
        assertThat(result.getSemanticScore()).isNull();
    }

    @Test
    void calculateMatch_aiReturnsEmpty_fallsBackToDeterministic() {
        CandidateData candidateData = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(3)
                .build();
        JobData jobData = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(jobRequirementResolver.resolve(job)).thenReturn(jobData);
        when(matchingEngine.calculate(candidateData, jobData))
                .thenReturn(RuleBasedMatchingEngine.DeterministicScore.builder()
                        .overallScore(80)
                        .skillScore(100)
                        .experienceScore(100)
                        .profileScore(80)
                        .matchedSkills(List.of("java"))
                        .missingSkills(List.of())
                        .matchingReasons(List.of("Match"))
                        .potentialGaps(List.of())
                        .build());
        when(aiMatchingService.evaluateMatch(candidateData, jobData, 1L)).thenReturn(Optional.empty());

        JobMatchingService.JobMatchResult result = jobMatchingService.calculateMatch(candidate, job);

        assertThat(result.getOverallScore()).isEqualTo(80);
        assertThat(result.getSemanticScore()).isNull();
    }
}
