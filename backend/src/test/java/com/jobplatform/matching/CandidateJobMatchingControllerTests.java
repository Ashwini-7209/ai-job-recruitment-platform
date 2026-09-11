package com.jobplatform.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CandidateJobMatchingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String candidateToken;
    private Job publishedJob;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-match-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Candidate")
                        .email(candidateEmail)
                        .password("password123")
                        .confirmPassword("password123")
                        .role(UserRole.CANDIDATE)
                        .build())));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(candidateEmail)
                                .password("password123")
                                .build())))
                .andReturn().getResponse().getContentAsString();

        candidateToken = objectMapper.readTree(loginResponse).path("data").path("accessToken").asText();

        User recruiter = User.builder()
                .email("recruiter-match-" + System.nanoTime() + "@example.com")
                .fullName("Test Recruiter")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        recruiter = userRepository.save(recruiter);

        publishedJob = Job.builder()
                .title("Java Developer")
                .description("We need a Java developer with Spring Boot experience")
                .location("New York")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.HYBRID)
                .experienceMin(2)
                .experienceMax(5)
                .skills("Java,Spring Boot,MySQL")
                .status(JobStatus.PUBLISHED)
                .recruiter(recruiter)
                .build();
        publishedJob = jobRepository.save(publishedJob);
    }

    @Test
    void getJobMatch_ownMatch_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/" + publishedJob.getId() + "/match")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value(publishedJob.getId()))
                .andExpect(jsonPath("$.data.overallScore").isNumber())
                .andExpect(jsonPath("$.data.skillScore").isNumber())
                .andExpect(jsonPath("$.data.experienceScore").isNumber())
                .andExpect(jsonPath("$.data.matchedSkills").isArray())
                .andExpect(jsonPath("$.data.missingSkills").isArray())
                .andExpect(jsonPath("$.data.matchingReasons").isArray())
                .andExpect(jsonPath("$.data.potentialGaps").isArray());
    }

    @Test
    void getJobMatch_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/1/match"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getJobMatch_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/99999/match")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRecommendations_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/candidates/me/recommendations")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.matches").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.currentPage").value(0));
    }

    @Test
    void getRecommendations_withPagination() throws Exception {
        mockMvc.perform(get("/api/candidates/me/recommendations")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.currentPage").value(0));
    }

    @Test
    void getRecommendations_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/recommendations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecommendations_withMinScore() throws Exception {
        mockMvc.perform(get("/api/candidates/me/recommendations")
                        .param("minScore", "50")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getJobMatch_draftJob_returns404() throws Exception {
        User draftRecruiter = User.builder()
                .email("draft-recruiter-" + System.nanoTime() + "@example.com")
                .fullName("Draft Recruiter")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        draftRecruiter = userRepository.save(draftRecruiter);

        Job draftJob = Job.builder()
                .title("Draft Job")
                .description("Not published")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .status(JobStatus.DRAFT)
                .recruiter(draftRecruiter)
                .build();
        draftJob = jobRepository.save(draftJob);

        mockMvc.perform(get("/api/candidates/me/jobs/" + draftJob.getId() + "/match")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSkillGap_ownJob_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/" + publishedJob.getId() + "/skill-gap")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value(publishedJob.getId()))
                .andExpect(jsonPath("$.data.jobTitle").value("Java Developer"))
                .andExpect(jsonPath("$.data.jobRequiredSkills").isArray())
                .andExpect(jsonPath("$.data.candidateSkills").isArray())
                .andExpect(jsonPath("$.data.matchedSkills").isArray())
                .andExpect(jsonPath("$.data.missingSkills").isArray())
                .andExpect(jsonPath("$.data.prioritySuggestions").isArray())
                .andExpect(jsonPath("$.data.explanation").isString());
    }

    @Test
    void getSkillGap_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/1/skill-gap"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSkillGap_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/99999/skill-gap")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCareerInsights_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/candidates/me/career-insights")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.strengths").isArray())
                .andExpect(jsonPath("$.data.recommendedSkills").isArray())
                .andExpect(jsonPath("$.data.suggestedJobCategories").isArray())
                .andExpect(jsonPath("$.data.profileImprovements").isArray())
                .andExpect(jsonPath("$.data.resumeImprovements").isArray())
                .andExpect(jsonPath("$.data.generalCareerSuggestions").isArray())
                .andExpect(jsonPath("$.data.aiEnhanced").isBoolean());
    }

    @Test
    void getCareerInsights_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/career-insights"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analyzeResumeImprovement_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/candidates/me/resumes/1/improvement-analysis"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analyzeResumeImprovement_nonexistentResume_returnsError() throws Exception {
        mockMvc.perform(post("/api/candidates/me/resumes/99999/improvement-analysis")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getJobMatch_responseContainsNewFields() throws Exception {
        mockMvc.perform(get("/api/candidates/me/jobs/" + publishedJob.getId() + "/match")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aiUsed").isBoolean())
                .andExpect(jsonPath("$.data.aiExplanation").exists());
    }
}
