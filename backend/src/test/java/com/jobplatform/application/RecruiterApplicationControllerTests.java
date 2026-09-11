package com.jobplatform.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.application.dto.UpdateApplicationStatusRequest;
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
class RecruiterApplicationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String recruiterToken;
    private String otherRecruiterToken;
    private Long jobId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-" + System.nanoTime() + "@example.com";
        String otherEmail = "other-recruiter-" + System.nanoTime() + "@example.com";

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
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));
    }

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(email).password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    @Test
    void getJobApplications_returnsApplications() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getJobApplications_returns403_forOtherRecruiter() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyApplications_returnsApplications() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getApplicationById_returnsApplication() throws Exception {
        String listResponse = mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Long applicationId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"));
    }

    @Test
    void getApplicationById_returns403_forOtherRecruiter() throws Exception {
        String listResponse = mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Long applicationId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId)
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApplicationStatus_success() throws Exception {
        String listResponse = mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Long applicationId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(com.jobplatform.application.enums.ApplicationStatus.UNDER_REVIEW).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));
    }

    @Test
    void updateApplicationStatus_returns400_forInvalidTransition() throws Exception {
        String listResponse = mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Long applicationId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(com.jobplatform.application.enums.ApplicationStatus.HIRED).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApplicationStatus_returns403_forOtherRecruiter() throws Exception {
        String listResponse = mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Long applicationId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateApplicationStatusRequest.builder()
                                .status(com.jobplatform.application.enums.ApplicationStatus.UNDER_REVIEW).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getJobApplicationStats_returnsStats() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/jobs/" + jobId + "/applications/stats")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalApplications").value(1))
                .andExpect(jsonPath("$.data.appliedCount").value(1));
    }
}
