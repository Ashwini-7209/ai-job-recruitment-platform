package com.jobplatform.candidate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.LoginResponse;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.candidate.dto.CandidateProfileUpdateRequest;
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
class CandidateProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String recruiterToken;
    private String candidateEmail;

    @BeforeEach
    void setUp() throws Exception {
        candidateEmail = "candidate-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-" + System.nanoTime() + "@example.com";

        RegisterRequest candidateReg = RegisterRequest.builder()
                .fullName("Test Candidate")
                .email(candidateEmail)
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();

        RegisterRequest recruiterReg = RegisterRequest.builder()
                .fullName("Test Recruiter")
                .email(recruiterEmail)
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.RECRUITER)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(candidateReg)));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recruiterReg)));

        LoginRequest candidateLogin = LoginRequest.builder()
                .email(candidateEmail)
                .password("password123")
                .build();
        String candidateResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidateLogin)))
                .andReturn().getResponse().getContentAsString();
        candidateToken = objectMapper.readTree(candidateResponse).path("data").path("accessToken").asText();

        LoginRequest recruiterLogin = LoginRequest.builder()
                .email(recruiterEmail)
                .password("password123")
                .build();
        String recruiterResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruiterLogin)))
                .andReturn().getResponse().getContentAsString();
        recruiterToken = objectMapper.readTree(recruiterResponse).path("data").path("accessToken").asText();
    }

    @Test
    void getProfile_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Test Candidate"))
                .andExpect(jsonPath("$.data.email").value(candidateEmail));
    }

    @Test
    void getProfile_returnsProfileWithOptionalFields() throws Exception {
        CandidateProfileUpdateRequest updateRequest = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .location("New York")
                .headline("Software Engineer")
                .bio("Experienced developer")
                .yearsOfExperience(5)
                .build();

        mockMvc.perform(put("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("1234567890"))
                .andExpect(jsonPath("$.data.location").value("New York"))
                .andExpect(jsonPath("$.data.headline").value("Software Engineer"))
                .andExpect(jsonPath("$.data.bio").value("Experienced developer"))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(5));
    }

    @Test
    void updateProfile_updatesAllFields() throws Exception {
        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .location("New York")
                .headline("Senior Developer")
                .bio("Experienced developer with 10 years")
                .currentJobTitle("Tech Lead")
                .yearsOfExperience(10)
                .educationSummary("BS Computer Science")
                .skillsSummary("Java, Spring Boot")
                .linkedinUrl("https://linkedin.com/in/test")
                .githubUrl("https://github.com/test")
                .portfolioUrl("https://test.dev")
                .profileImageUrl("https://example.com/photo.jpg")
                .build();

        mockMvc.perform(put("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.phone").value("1234567890"))
                .andExpect(jsonPath("$.data.location").value("New York"))
                .andExpect(jsonPath("$.data.headline").value("Senior Developer"))
                .andExpect(jsonPath("$.data.bio").value("Experienced developer with 10 years"))
                .andExpect(jsonPath("$.data.currentJobTitle").value("Tech Lead"))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(10))
                .andExpect(jsonPath("$.data.educationSummary").value("BS Computer Science"))
                .andExpect(jsonPath("$.data.skillsSummary").value("Java, Spring Boot"))
                .andExpect(jsonPath("$.data.linkedinUrl").value("https://linkedin.com/in/test"))
                .andExpect(jsonPath("$.data.githubUrl").value("https://github.com/test"))
                .andExpect(jsonPath("$.data.portfolioUrl").value("https://test.dev"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/photo.jpg"));
    }

    @Test
    void updateProfile_updatesPartialFields() throws Exception {
        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .headline("Developer")
                .build();

        mockMvc.perform(put("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("1234567890"))
                .andExpect(jsonPath("$.data.headline").value("Developer"))
                .andExpect(jsonPath("$.data.location").doesNotExist());
    }

    @Test
    void updateProfile_validatesFieldLengths() throws Exception {
        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1".repeat(21))
                .build();

        mockMvc.perform(put("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getProfile_returns403_forRecruiter() throws Exception {
        mockMvc.perform(get("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/candidates/me/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_returnsEmptyBody_forEmptyRequest() throws Exception {
        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder().build();

        mockMvc.perform(put("/api/candidates/me/profile")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
