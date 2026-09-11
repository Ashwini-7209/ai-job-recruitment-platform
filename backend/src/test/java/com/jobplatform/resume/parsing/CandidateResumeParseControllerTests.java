package com.jobplatform.resume.parsing;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CandidateResumeParseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;

    @BeforeEach
    void setUp() throws Exception {
        String email = "candidate-parse-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Candidate")
                        .email(email)
                        .password("password123")
                        .confirmPassword("password123")
                        .role(UserRole.CANDIDATE)
                        .build())));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(email)
                                .password("password123")
                                .build())))
                .andReturn().getResponse().getContentAsString();

        candidateToken = objectMapper.readTree(loginResponse).path("data").path("accessToken").asText();
    }

    @Test
    void parseResume_ownResume_returnsSuccess() throws Exception {
        byte[] pdfBytes = createSimplePdf("John Doe Software Engineer Java");

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);

        String uploadResponse = mockMvc.perform(multipart("/api/candidates/me/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + candidateToken))
                .andReturn().getResponse().getContentAsString();

        Long resumeId = objectMapper.readTree(uploadResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/candidates/me/resumes/" + resumeId + "/parse")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resumeId").value(resumeId));
    }

    @Test
    void parseResume_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/candidates/me/resumes/1/parse"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void parseResume_nonexistentResume_returns404() throws Exception {
        mockMvc.perform(post("/api/candidates/me/resumes/99999/parse")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void parseResume_recruiterCannotParse_returns400() throws Exception {
        String recruiterEmail = "recruiter-parse-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Recruiter")
                        .email(recruiterEmail)
                        .password("password123")
                        .confirmPassword("password123")
                        .role(UserRole.RECRUITER)
                        .build())));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(recruiterEmail)
                                .password("password123")
                                .build())))
                .andReturn().getResponse().getContentAsString();

        String recruiterToken = objectMapper.readTree(loginResponse).path("data").path("accessToken").asText();

        mockMvc.perform(post("/api/candidates/me/resumes/1/parse")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    private byte[] createSimplePdf(String text) {
        try {
            org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            document.addPage(page);

            org.apache.pdfbox.pdmodel.PDPageContentStream contentStream =
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText(text);
            contentStream.endText();
            contentStream.close();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            document.save(baos);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
