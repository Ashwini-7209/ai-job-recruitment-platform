package com.jobplatform.recruiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.recruiter.dto.RecruiterProfileUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecruiterProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String recruiterToken;
    private String candidateToken;
    private String recruiterEmail;

    @BeforeEach
    void setUp() throws Exception {
        recruiterEmail = "recruiter-" + System.nanoTime() + "@example.com";
        String candidateEmail = "candidate-" + System.nanoTime() + "@example.com";

        RegisterRequest recruiterReg = RegisterRequest.builder()
                .fullName("Test Recruiter")
                .email(recruiterEmail)
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.RECRUITER)
                .build();

        RegisterRequest candidateReg = RegisterRequest.builder()
                .fullName("Test Candidate")
                .email(candidateEmail)
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recruiterReg)));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidateReg)));

        LoginRequest recruiterLogin = LoginRequest.builder()
                .email(recruiterEmail)
                .password("password123")
                .build();
        String recruiterResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruiterLogin)))
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(recruiterResponse).path("data").path("accessToken").asText();

        LoginRequest candidateLogin = LoginRequest.builder()
                .email(candidateEmail)
                .password("password123")
                .build();
        String candidateResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidateLogin)))
                .andReturn().getResponse().getContentAsString();
        candidateToken = objectMapper.readTree(candidateResponse).path("data").path("accessToken").asText();
    }

    @Test
    void getProfile_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Test Recruiter"))
                .andExpect(jsonPath("$.data.email").value(recruiterEmail));
    }

    @Test
    void getProfile_returnsProfileWithOptionalFields() throws Exception {
        RecruiterProfileUpdateRequest updateRequest = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .jobTitle("HR Manager")
                .companyName("Tech Corp")
                .companyLocation("San Francisco")
                .build();

        mockMvc.perform(put("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("0987654321"))
                .andExpect(jsonPath("$.data.jobTitle").value("HR Manager"))
                .andExpect(jsonPath("$.data.companyName").value("Tech Corp"))
                .andExpect(jsonPath("$.data.companyLocation").value("San Francisco"));
    }

    @Test
    void updateProfile_updatesAllFields() throws Exception {
        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .jobTitle("Senior Recruiter")
                .department("Engineering")
                .companyName("Tech Corp")
                .companyWebsite("https://techcorp.com")
                .companyDescription("Leading tech company")
                .companyLocation("San Francisco")
                .linkedinUrl("https://linkedin.com/in/recruiter")
                .build();

        mockMvc.perform(put("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"))
                .andExpect(jsonPath("$.data.jobTitle").value("Senior Recruiter"))
                .andExpect(jsonPath("$.data.department").value("Engineering"))
                .andExpect(jsonPath("$.data.companyName").value("Tech Corp"))
                .andExpect(jsonPath("$.data.companyWebsite").value("https://techcorp.com"))
                .andExpect(jsonPath("$.data.companyDescription").value("Leading tech company"))
                .andExpect(jsonPath("$.data.companyLocation").value("San Francisco"))
                .andExpect(jsonPath("$.data.linkedinUrl").value("https://linkedin.com/in/recruiter"));
    }

    @Test
    void updateProfile_updatesPartialFields() throws Exception {
        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .companyName("Tech Corp")
                .build();

        mockMvc.perform(put("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("0987654321"))
                .andExpect(jsonPath("$.data.companyName").value("Tech Corp"))
                .andExpect(jsonPath("$.data.jobTitle").doesNotExist());
    }

    @Test
    void updateProfile_validatesFieldLengths() throws Exception {
        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("1".repeat(21))
                .build();

        mockMvc.perform(put("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getProfile_returns403_forCandidate() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/recruiters/me/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_returnsEmptyBody_forEmptyRequest() throws Exception {
        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder().build();

        mockMvc.perform(put("/api/recruiters/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
