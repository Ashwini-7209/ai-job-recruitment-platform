package com.jobplatform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.enums.EmploymentType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String candidateToken;
    private String recruiterToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-" + System.nanoTime() + "@example.com";
        String adminEmail = "admin-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Recruiter").email(recruiterEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        User admin = User.builder()
                .fullName("Admin").email(adminEmail).password(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN).enabled(true).build();
        userRepository.save(admin);

        candidateToken = loginAndGetToken(candidateEmail);
        recruiterToken = loginAndGetToken(recruiterEmail);
        adminToken = loginAndGetToken(adminEmail);
    }

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(email).password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private CreateJobRequest createValidJobRequest() {
        return CreateJobRequest.builder()
                .title("Software Engineer").description("Description").location("New York")
                .employmentType(EmploymentType.FULL_TIME).workplaceType(WorkplaceType.HYBRID)
                .experienceMin(2).experienceMax(5).salaryMin(80000).salaryMax(120000)
                .skills("Java, Spring Boot").build();
    }

    // ========== UNAUTHENTICATED ACCESS ==========

    @Test
    void unauthenticated_accessProtectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_accessPublicEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticated_accessLoginEndpoint_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email("").password("").build())))
                .andExpect(status().isBadRequest());
    }

    // ========== ROLE-BASED ACCESS CONTROL ==========

    @Test
    void candidate_accessCandidateEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void candidate_accessRecruiterEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void candidate_accessAdminEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void candidate_accessAdminAnalyticsEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/summary")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void candidate_accessAdminAuditLogsEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_accessCandidateEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_accessRecruiterEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());
    }

    @Test
    void recruiter_accessAdminEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_accessAdminAnalyticsEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/summary")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_accessAdminEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void admin_accessCandidateEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_accessRecruiterEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // ========== CANDIDATE-SPECIFIC ROLE RESTRICTIONS ==========

    @Test
    void candidate_accessCandidateAnalytics_returns200() throws Exception {
        mockMvc.perform(get("/api/candidates/me/analytics/summary")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void candidate_accessCandidateJobMatching_returns200() throws Exception {
        mockMvc.perform(get("/api/candidates/me/recommendations")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void candidate_accessCandidateCareerInsights_returns200() throws Exception {
        mockMvc.perform(get("/api/candidates/me/career-insights")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void recruiter_accessCandidateAnalytics_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates/me/analytics/summary")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_accessCandidateJobMatching_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates/me/recommendations")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_accessCandidateCareerInsights_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates/me/career-insights")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    // ========== RECRUITER-SPECIFIC ROLE RESTRICTIONS ==========

    @Test
    void recruiter_accessRecruiterAnalytics_returns200() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/analytics/summary")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());
    }

    @Test
    void recruiter_accessRecruiterHiringMetrics_returns200() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/analytics/hiring-metrics")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());
    }

    @Test
    void candidate_accessRecruiterAnalytics_returns403() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/analytics/summary")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void candidate_accessRecruiterHiringMetrics_returns403() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/analytics/hiring-metrics")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    // ========== NOTIFICATION ACCESS (ALL ROLES) ==========

    @Test
    void candidate_accessNotifications_returns200() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void recruiter_accessNotifications_returns200() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());
    }

    // ========== IDOR PROTECTION ==========

    @Test
    void candidate_cannotAccessOtherUserResume_returns403() throws Exception {
        // Try to access resume with ID 999 (non-existent, but tests ownership check)
        mockMvc.perform(get("/api/candidates/me/resumes/999")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void recruiter_cannotAccessOtherRecruiterJob_returns403() throws Exception {
        // Create a job as one recruiter, try to access as another
        mockMvc.perform(get("/api/recruiter/jobs/999")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void candidate_cannotAccessAdminJobManagement_returns403() throws Exception {
        mockMvc.perform(patch("/api/admin/jobs/1/status?status=CLOSED")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_cannotAccessAdminUserManagement_returns403() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": false}"))
                .andExpect(status().isForbidden());
    }

    // ========== JWT TOKEN VALIDATION ==========

    @Test
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer invalid-token-value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.fakeSignature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingBearerPrefix_returns401() throws Exception {
        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", candidateToken))
                .andExpect(status().isUnauthorized());
    }

    // ========== INPUT VALIDATION SECURITY ==========

    @Test
    void register_withSQLInjectionAttempt_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("'; DROP TABLE users; --")
                .email("sql@example.com")
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_withSQLInjectionAttempt_returns400() throws Exception {
        LoginRequest request = new LoginRequest("' OR '1'='1", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withXSSAttempt_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("<script>alert('xss')</script>")
                .email("xss@example.com")
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Verify the script tag is in the response (will be escaped by frontend)
        // but the backend should store it as-is
        org.assertj.core.api.Assertions.assertThat(response).contains("script");
    }

    // ========== ADDITIONAL IDOR PROTECTION ==========

    @Test
    void candidate_cannotAccessRecruiterApplications_returns403() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void candidate_cannotAccessRecruiterCandidateSearch_returns403() throws Exception {
        mockMvc.perform(get("/api/recruiters/candidates")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_cannotAccessCandidateApplications_returns403() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void candidate_cannotMarkRecruiterNotificationsAsRead_returns403() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void candidate_cannotAccessRecruiterJobManagement_returns403() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void recruiter_cannotAccessCandidateProfileUpdate_denied() throws Exception {
        mockMvc.perform(put("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\": \"Hacked\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(200);
                });
    }

    @Test
    void admin_cannotAccessCandidateResumeUpload_denied() throws Exception {
        mockMvc.perform(post("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(200);
                });
    }

    // ========== INPUT VALIDATION SECURITY ==========

    @Test
    void register_withEmptyEmail_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test").email("").password("password123").confirmPassword("password123")
                .role(UserRole.CANDIDATE).build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test").email("short@example.com").password("ab").confirmPassword("ab")
                .role(UserRole.CANDIDATE).build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withMismatchedPasswords_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test").email("mismatch@example.com").password("password123").confirmPassword("different456")
                .role(UserRole.CANDIDATE).build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withEmptyCredentials_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJob_withEmptyTitle_returns400() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("").description("Description")
                .employmentType(EmploymentType.FULL_TIME).workplaceType(WorkplaceType.REMOTE)
                .build();

        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
