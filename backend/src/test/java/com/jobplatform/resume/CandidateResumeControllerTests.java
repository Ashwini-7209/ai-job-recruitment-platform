package com.jobplatform.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CandidateResumeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        candidateToken = loginAndGetToken(candidateEmail);
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
    void uploadResume_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());

        mockMvc.perform(multipart("/api/candidates/me/resumes").file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.originalFileName").value("resume.pdf"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void uploadResume_returns401_whenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());

        mockMvc.perform(multipart("/api/candidates/me/resumes").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyResumes_returnsResumes() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());
        mockMvc.perform(multipart("/api/candidates/me/resumes").file(file)
                .header("Authorization", "Bearer " + candidateToken));

        mockMvc.perform(get("/api/candidates/me/resumes")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getResumeById_returnsResume() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());
        String uploadResponse = mockMvc.perform(multipart("/api/candidates/me/resumes").file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andReturn().getResponse().getContentAsString();
        Long resumeId = objectMapper.readTree(uploadResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/candidates/me/resumes/" + resumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalFileName").value("resume.pdf"));
    }

    @Test
    void downloadResume_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());
        String uploadResponse = mockMvc.perform(multipart("/api/candidates/me/resumes").file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andReturn().getResponse().getContentAsString();
        Long resumeId = objectMapper.readTree(uploadResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/candidates/me/resumes/" + resumeId + "/download")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void activateResume_success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "resume1.pdf", "application/pdf", createPdfContent());
        String r1Response = mockMvc.perform(multipart("/api/candidates/me/resumes").file(file1)
                        .header("Authorization", "Bearer " + candidateToken))
                .andReturn().getResponse().getContentAsString();
        Long r1Id = objectMapper.readTree(r1Response).path("data").path("id").asLong();

        MockMultipartFile file2 = new MockMultipartFile(
                "file", "resume2.pdf", "application/pdf", createPdfContent());
        String r2Response = mockMvc.perform(multipart("/api/candidates/me/resumes").file(file2)
                        .header("Authorization", "Bearer " + candidateToken))
                .andReturn().getResponse().getContentAsString();
        Long r2Id = objectMapper.readTree(r2Response).path("data").path("id").asLong();

        mockMvc.perform(post("/api/candidates/me/resumes/" + r2Id + "/activate")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(get("/api/candidates/me/resumes/" + r1Id)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void deleteResume_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", createPdfContent());
        String uploadResponse = mockMvc.perform(multipart("/api/candidates/me/resumes").file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andReturn().getResponse().getContentAsString();
        Long resumeId = objectMapper.readTree(uploadResponse).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/candidates/me/resumes/" + resumeId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
