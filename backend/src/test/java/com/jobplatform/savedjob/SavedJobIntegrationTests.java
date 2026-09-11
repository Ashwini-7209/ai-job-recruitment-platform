package com.jobplatform.savedjob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.application.dto.CreateApplicationRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SavedJobIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String otherCandidateToken;
    private String recruiterToken;
    private Long jobId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-sj-" + System.nanoTime() + "@example.com";
        String otherEmail = "other-sj-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-sj-" + System.nanoTime() + "@example.com";

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
    }

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(email).password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private Long createSecondJob() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateJobRequest.builder()
                                .title("Product Manager")
                                .description("Looking for a product manager")
                                .location("San Francisco")
                                .employmentType(EmploymentType.FULL_TIME)
                                .workplaceType(WorkplaceType.REMOTE)
                                .build())))
                .andReturn().getResponse().getContentAsString();
        Long secondJobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + secondJobId + "/publish")
                .header("Authorization", "Bearer " + recruiterToken));

        return secondJobId;
    }

    // Test 1: Candidate saves a job
    @Test
    void saveJob_candidateCanSave() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.jobId").value(jobId))
                .andExpect(jsonPath("$.data.jobTitle").value("Software Engineer"));
    }

    // Test 2: Candidate cannot save a non-existent job
    @Test
    void saveJob_nonExistentJobReturns404() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/99999")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    // Test 3: Duplicate save is prevented
    @Test
    void saveJob_duplicateSaveReturns400() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isBadRequest());
    }

    // Test 4: Candidate unsaves own job
    @Test
    void unsaveJob_candidateCanUnsave() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/candidates/me/saved-jobs/" + jobId + "/status")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    // Test 5: Candidate lists own saved jobs
    @Test
    void getSavedJobs_candidateCanList() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(get("/api/candidates/me/saved-jobs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].jobId").value(jobId));
    }

    // Test 6: Candidate cannot access another candidate's saved jobs
    @Test
    void getSavedJobs_cannotAccessOtherCandidates() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(get("/api/candidates/me/saved-jobs")
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Test 7: Candidate cannot delete another candidate's bookmark
    @Test
    void unsaveJob_cannotDeleteOtherCandidatesBookmark() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isOk());

        // Original candidate's bookmark should still exist
        mockMvc.perform(get("/api/candidates/me/saved-jobs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // Test 8: Saved job pagination works
    @Test
    void getSavedJobs_paginationWorks() throws Exception {
        Long secondJobId = createSecondJob();

        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken));
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + secondJobId)
                        .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(get("/api/candidates/me/saved-jobs?page=0&size=1")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    // Test 9: Search works on saved jobs
    @Test
    void getSavedJobs_searchWorks() throws Exception {
        Long secondJobId = createSecondJob();

        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken));
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + secondJobId)
                        .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(get("/api/candidates/me/saved-jobs?q=Software")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].jobTitle").value("Software Engineer"));
    }

    // Test 10: Saved status works
    @Test
    void getSavedStatus_returnsCorrectStatus() throws Exception {
        mockMvc.perform(get("/api/candidates/me/saved-jobs/" + jobId + "/status")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));

        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(get("/api/candidates/me/saved-jobs/" + jobId + "/status")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    // Test 11: CLOSED job behavior works (can still save)
    @Test
    void saveJob_closedJobCanBeSaved() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/close")
                .header("Authorization", "Bearer " + recruiterToken));

        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated());
    }

    // Test 12: Applied + saved job behavior works
    @Test
    void getSavedJobs_appliedJobShowsAppliedStatus() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));

        mockMvc.perform(get("/api/candidates/me/saved-jobs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].jobId").value(jobId))
                .andExpect(jsonPath("$.data.content[0].applied").value(true));
    }

    // Test 13: Recruiter cannot access candidate saved-job APIs
    @Test
    void recruiterCannotAccessSavedJobApis() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/candidates/me/saved-jobs")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/candidates/me/saved-jobs/" + jobId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    // Test 14: Existing job APIs still work
    @Test
    void existingJobApis_stillWork() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Software Engineer"));
    }

    // Test 15: Existing application APIs still work
    @Test
    void existingApplicationApis_stillWork() throws Exception {
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("APPLIED"));

        mockMvc.perform(get("/api/candidates/me/applications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // Test 16: Unauthenticated user cannot access saved-job APIs
    @Test
    void unauthenticatedUser_cannotAccess() throws Exception {
        mockMvc.perform(post("/api/candidates/me/saved-jobs/" + jobId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/candidates/me/saved-jobs"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/candidates/me/saved-jobs/" + jobId))
                .andExpect(status().isUnauthorized());
    }
}
