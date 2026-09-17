package com.jobplatform.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.application.dto.ApplicationNoteRequest;
import com.jobplatform.application.dto.UpdateApplicationStatusRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecruiterApplicationWorkflowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String recruiterToken;
    private String otherRecruiterToken;
    private Long jobId;
    private Long applicationId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-wf-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-wf-" + System.nanoTime() + "@example.com";
        String otherEmail = "other-wf-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Recruiter").email(recruiterEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Other Recruiter").email(otherEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        candidateToken = loginAndGetToken(candidateEmail);
        recruiterToken = loginAndGetToken(recruiterEmail);
        otherRecruiterToken = loginAndGetToken(otherEmail);

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
                        .content(objectMapper.writeValueAsString(com.jobplatform.application.dto.CreateApplicationRequest.builder().coverLetter("I am interested").build())))
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

    @Test
    void getApplications_recruiterSeesOwnApplications() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getApplications_otherRecruiterSeesNothing() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getApplications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getApplicationDetail_recruiterCanView() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.coverLetter").value("I am interested"))
                .andExpect(jsonPath("$.data.candidate.fullName").value("Test Candidate"))
                .andExpect(jsonPath("$.data.statusHistory").isArray());
    }

    @Test
    void getApplicationDetail_otherRecruiterCannotView() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getApplicationDetail_candidateCannotAccess() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_appliedToUnderReview() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.UNDER_REVIEW).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));
    }

    @Test
    void updateStatus_underReviewToShortlisted() throws Exception {
        moveToUnderReview();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.SHORTLISTED).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHORTLISTED"));
    }

    @Test
    void updateStatus_underReviewToRejected() throws Exception {
        moveToUnderReview();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.REJECTED).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void updateStatus_shortlistedToHired() throws Exception {
        moveToUnderReview();
        moveToShortlisted();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.HIRED).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HIRED"));
    }

    @Test
    void updateStatus_invalidTransition_returns400() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.HIRED).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_otherRecruiterCannotUpdate() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.UNDER_REVIEW).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_candidateCannotUpdate() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(ApplicationStatus.UNDER_REVIEW).build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusHistory_recordedAfterTransition() throws Exception {
        moveToUnderReview();

        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusHistory").isArray())
                .andExpect(jsonPath("$.data.statusHistory.length()").value(1))
                .andExpect(jsonPath("$.data.statusHistory[0].oldStatus").value("APPLIED"))
                .andExpect(jsonPath("$.data.statusHistory[0].newStatus").value("UNDER_REVIEW"));
    }

    @Test
    void createNote_recruiterCanCreate() throws Exception {
        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ApplicationNoteRequest.builder()
                                .note("Strong candidate, good experience").build())))
                .andExpect(status().isCreated());
    }

    @Test
    void searchApplications_byCandidateName() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("q", "Test Candidate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void searchApplications_byNonExistentName() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("q", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void filterByStatus_applied() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("status", "APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void filterByStatus_underReview() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void filterByJobId() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("jobId", jobId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void filterByJobId_noMatch() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("jobId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void pagination_works() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5));
    }

    @Test
    void sorting_oldest() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("sort", "oldest"))
                .andExpect(status().isOk());
    }

    @Test
    void sorting_updated() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .param("sort", "updated"))
                .andExpect(status().isOk());
    }

    @Test
    void aggregateStats_recruiterSeesOwnStats() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/application-stats")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").value(1))
                .andExpect(jsonPath("$.data.appliedCount").value(1))
                .andExpect(jsonPath("$.data.totalJobs").value(1));
    }

    @Test
    void aggregateStats_otherRecruiterSeesZero() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/application-stats")
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").value(0));
    }

    @Test
    void jobStats_correct() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications/stats")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").value(1))
                .andExpect(jsonPath("$.data.appliedCount").value(1));
    }

    @Test
    void jobApplications_list() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void jobApplications_otherRecruiterCannotAccess() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notes_fullCrud() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ApplicationNoteRequest.builder()
                                .note("Strong candidate").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.note").value("Strong candidate"))
                .andReturn().getResponse().getContentAsString();

        Long noteId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(put("/api/recruiters/me/applications/" + applicationId + "/notes/" + noteId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ApplicationNoteRequest.builder()
                                .note("Updated note").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.note").value("Updated note"));

        mockMvc.perform(delete("/api/recruiters/me/applications/" + applicationId + "/notes/" + noteId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void notes_otherRecruiterCannotCreate() throws Exception {
        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ApplicationNoteRequest.builder()
                                .note("Should fail").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notes_otherRecruiterCannotRead() throws Exception {
        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ApplicationNoteRequest.builder()
                                .note("My note").build())));

        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notes_candidateCannotAccess() throws Exception {
        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/notes")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ApplicationNoteRequest.builder()
                                .note("Should fail").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void detailResponse_excludesSensitiveData() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidate.fullName").value("Test Candidate"))
                .andExpect(jsonPath("$.data.candidate.email").value(org.hamcrest.Matchers.notNullValue()));
    }

    private void moveToUnderReview() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                        .status(ApplicationStatus.UNDER_REVIEW).build())));
    }

    private void moveToShortlisted() throws Exception {
        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                        .status(ApplicationStatus.SHORTLISTED).build())));
    }
}
