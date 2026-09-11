package com.jobplatform.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CandidateApplicationTrackingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String otherCandidateToken;
    private String recruiterToken;
    private Long jobId;
    private Long applicationId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-tracking-" + System.nanoTime() + "@example.com";
        String otherEmail = "other-tracking-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-tracking-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Other Candidate").email(otherEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Recruiter").email(recruiterEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        candidateToken = loginAndGetToken(candidateEmail);
        otherCandidateToken = loginAndGetToken(otherEmail);
        recruiterToken = loginAndGetToken(recruiterEmail);

        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateJobRequest.builder()
                                .title("Software Engineer")
                                .description("We are looking for a software engineer")
                                .location("New York")
                                .employmentType(EmploymentType.FULL_TIME)
                                .workplaceType(WorkplaceType.HYBRID)
                                .build())))
                .andReturn().getResponse().getContentAsString();
        jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                .header("Authorization", "Bearer " + recruiterToken));

        String applyResponse = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().coverLetter("I am interested").build())))
                .andReturn().getResponse().getContentAsString();
        applicationId = objectMapper.readTree(applyResponse).path("data").path("id").asLong();
    }

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(email).password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    // Test 1: Candidate can list own applications
    @Test
    void listApplications_candidateSeesOwnApplications() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.data.content[0].location").value("New York"))
                .andExpect(jsonPath("$.data.content[0].employmentType").value("FULL_TIME"))
                .andExpect(jsonPath("$.data.content[0].workplaceType").value("HYBRID"))
                .andExpect(jsonPath("$.data.content[0].status").value("APPLIED"));
    }

    // Test 2: Candidate cannot list another candidate's applications
    @Test
    void listApplications_otherCandidateSeesNothing() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Test 3: Candidate can view own application detail
    @Test
    void getApplicationDetail_candidateCanViewOwn() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.data.location").value("New York"))
                .andExpect(jsonPath("$.data.employmentType").value("FULL_TIME"))
                .andExpect(jsonPath("$.data.workplaceType").value("HYBRID"))
                .andExpect(jsonPath("$.data.statusHistory").isArray())
                .andExpect(jsonPath("$.data.coverLetter").value("I am interested"));
    }

    // Test 4: Candidate cannot view another candidate's application
    @Test
    void getApplicationDetail_candidateCannotViewOther() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isBadRequest());
    }

    // Test 5: Candidate can withdraw eligible application
    @Test
    void withdrawApplication_candidateCanWithdraw() throws Exception {
        mockMvc.perform(patch("/api/candidates/me/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"))
                .andExpect(jsonPath("$.data.withdrawnAt").isNotEmpty());
    }

    // Test 6: Candidate cannot perform invalid withdrawal (after HIRED)
    @Test
    void withdrawApplication_cannotWithdrawAfterHired() throws Exception {
        moveToUnderReview();
        moveToShortlisted();
        moveToHired();

        mockMvc.perform(patch("/api/candidates/me/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isBadRequest());
    }

    // Test 7: Application statistics are correct
    @Test
    void applicationStats_correctCounts() throws Exception {
        mockMvc.perform(get("/api/candidates/me/application-stats")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").value(1))
                .andExpect(jsonPath("$.data.appliedCount").value(1))
                .andExpect(jsonPath("$.data.underReviewCount").value(0))
                .andExpect(jsonPath("$.data.shortlistedCount").value(0))
                .andExpect(jsonPath("$.data.rejectedCount").value(0))
                .andExpect(jsonPath("$.data.hiredCount").value(0))
                .andExpect(jsonPath("$.data.withdrawnCount").value(0));
    }

    // Test 8: Status filtering works
    @Test
    void listApplications_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("status", "APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Test 9: Pagination works
    @Test
    void listApplications_paginationWorks() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // Test 10: Recruiter cannot access candidate-private APIs
    @Test
    void recruiterCannotAccessCandidateApis() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/candidates/me/application-stats")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/candidates/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/candidates/me/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    // Test 11: Job search works
    @Test
    void listApplications_searchByJobTitle() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "Software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Test 12: Sorting works
    @Test
    void listApplications_sortingWorks() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("sort", "oldest"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("sort", "updated"))
                .andExpect(status().isOk());
    }

    // Test 13: Candidate cannot withdraw application belonging to other candidate
    @Test
    void withdrawApplication_cannotWithdrawOtherCandidateApp() throws Exception {
        mockMvc.perform(patch("/api/candidates/me/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isBadRequest());
    }

    // Test 14: Unauthenticated user cannot access candidate APIs
    @Test
    void unauthenticatedUser_cannotAccess() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/candidates/me/application-stats"))
                .andExpect(status().isUnauthorized());
    }

    // Test 15: Detail response includes job details
    @Test
    void getApplicationDetail_includesJobDetails() throws Exception {
        mockMvc.perform(get("/api/candidates/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobId").value(jobId))
                .andExpect(jsonPath("$.data.jobDescription").value("We are looking for a software engineer"))
                .andExpect(jsonPath("$.data.skills").isEmpty());
    }

    // Test 16: Status history recorded on status change
    @Test
    void applicationDetail_showsStatusHistoryAfterUpdate() throws Exception {
        moveToUnderReview();

        mockMvc.perform(get("/api/candidates/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data.statusHistory.length()").value(1))
                .andExpect(jsonPath("$.data.statusHistory[0].oldStatus").value("APPLIED"))
                .andExpect(jsonPath("$.data.statusHistory[0].newStatus").value("UNDER_REVIEW"));
    }

    private void moveToUnderReview() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(com.jobplatform.application.dto.UpdateApplicationStatusRequest.builder()
                        .status(ApplicationStatus.UNDER_REVIEW).build())));
    }

    private void moveToShortlisted() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(com.jobplatform.application.dto.UpdateApplicationStatusRequest.builder()
                        .status(ApplicationStatus.SHORTLISTED).build())));
    }

    private void moveToHired() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(com.jobplatform.application.dto.UpdateApplicationStatusRequest.builder()
                        .status(ApplicationStatus.HIRED).build())));
    }
}
