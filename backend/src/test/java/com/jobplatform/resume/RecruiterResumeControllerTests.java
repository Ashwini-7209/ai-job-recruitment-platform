package com.jobplatform.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.application.dto.CreateApplicationRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecruiterResumeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateAToken;
    private String candidateBToken;
    private String recruiterAToken;
    private String recruiterBToken;
    private Long recruiterAJobId;
    private Long recruiterBJobId;
    private Long applicationId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateAEmail = "candidate-a-" + System.nanoTime() + "@example.com";
        String candidateBEmail = "candidate-b-" + System.nanoTime() + "@example.com";
        String recruiterAEmail = "recruiter-a-" + System.nanoTime() + "@example.com";
        String recruiterBEmail = "recruiter-b-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Candidate A").email(candidateAEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Candidate B").email(candidateBEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Recruiter A").email(recruiterAEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Recruiter B").email(recruiterBEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        candidateAToken = loginAndGetToken(candidateAEmail);
        candidateBToken = loginAndGetToken(candidateBEmail);
        recruiterAToken = loginAndGetToken(recruiterAEmail);
        recruiterBToken = loginAndGetToken(recruiterBEmail);

        String jobAResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateJobRequest.builder()
                                .title("Job A").description("Description").employmentType(EmploymentType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).build())))
                .andReturn().getResponse().getContentAsString();
        recruiterAJobId = objectMapper.readTree(jobAResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + recruiterAJobId + "/publish")
                .header("Authorization", "Bearer " + recruiterAToken));

        String jobBResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateJobRequest.builder()
                                .title("Job B").description("Description").employmentType(EmploymentType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).build())))
                .andReturn().getResponse().getContentAsString();
        recruiterBJobId = objectMapper.readTree(jobBResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + recruiterBJobId + "/publish")
                .header("Authorization", "Bearer " + recruiterBToken));

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());
        mockMvc.perform(multipart("/api/candidates/me/resumes").file(file)
                .header("Authorization", "Bearer " + candidateAToken));

        String applyResponse = mockMvc.perform(post("/api/jobs/" + recruiterAJobId + "/applications")
                        .header("Authorization", "Bearer " + candidateAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())))
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

    private byte[] createPdfContent() {
        return new byte[]{(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46, 0x00, 0x00, 0x00, 0x00};
    }

    @Test
    void downloadApplicationResume_success() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId + "/resume")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk());
    }

    @Test
    void downloadApplicationResume_returns403_forOtherRecruiter() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId + "/resume")
                        .header("Authorization", "Bearer " + recruiterBToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downloadApplicationResume_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/999/resume")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadApplicationResume_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/applications/" + applicationId + "/resume"))
                .andExpect(status().isUnauthorized());
    }
}
