package com.jobplatform.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.dto.UpdateJobRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecruiterJobControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String recruiterToken;
    private String otherRecruiterToken;
    private String candidateToken;

    @BeforeEach
    void setUp() throws Exception {
        String recruiterEmail = "recruiter-" + System.nanoTime() + "@example.com";
        String otherEmail = "other-recruiter-" + System.nanoTime() + "@example.com";
        String candidateEmail = "candidate-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Recruiter").email(recruiterEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Other Recruiter").email(otherEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        recruiterToken = loginAndGetToken(recruiterEmail);
        otherRecruiterToken = loginAndGetToken(otherEmail);
        candidateToken = loginAndGetToken(candidateEmail);
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
                .title("Software Engineer")
                .description("We are looking for a software engineer")
                .location("New York")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.HYBRID)
                .experienceMin(2)
                .experienceMax(5)
                .salaryMin(80000)
                .salaryMax(120000)
                .skills("Java, Spring Boot, React")
                .build();
    }

    @Test
    void createJob_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Software Engineer"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void createJob_returns403_forCandidate() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJob_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createJob_validatesRequiredFields() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateJobRequest.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMyJobs_returnsJobs() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())));

        mockMvc.perform(get("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getMyJobs_onlyReturnsOwnJobs() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())));

        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())));

        mockMvc.perform(get("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getJobById_returnsJob() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/recruiter/jobs/" + jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Software Engineer"));
    }

    @Test
    void getJobById_returns404_whenNotOwner() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/recruiter/jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateJob_updatesFields() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        UpdateJobRequest updateRequest = UpdateJobRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/api/recruiter/jobs/" + jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void publishJob_success() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void publishJob_returns400_whenNotDraft() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void closeJob_success() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/close")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    void deleteJob_success() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/recruiter/jobs/" + jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteJob_returns400_whenPublished() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(delete("/api/recruiter/jobs/" + jobId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_returnsCorrectCounts() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())));

        mockMvc.perform(get("/api/recruiter/jobs/stats")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalJobs").value(1))
                .andExpect(jsonPath("$.data.draftJobs").value(1));
    }
}
