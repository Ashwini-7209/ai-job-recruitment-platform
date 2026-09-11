package com.jobplatform.job;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String recruiterToken;
    private String candidateToken;
    private String candidateEmail;

    @BeforeEach
    void setUp() throws Exception {
        String recruiterEmail = "recruiter-" + System.nanoTime() + "@example.com";
        candidateEmail = "candidate-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Recruiter").email(recruiterEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        recruiterToken = loginAndGetToken(recruiterEmail);
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

    private void createPublishedJob() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + jobId + "/publish")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void createDraftJob() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                .header("Authorization", "Bearer " + recruiterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createValidJobRequest())));
    }

    @Test
    void getJobs_returnsPublishedJobs() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getJobs_excludesDraftJobs() throws Exception {
        createDraftJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getJobs_excludesClosedJobs() throws Exception {
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
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getJobById_returnsPublishedJob() throws Exception {
        createPublishedJob();

        String listResponse = mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Software Engineer"))
                .andExpect(jsonPath("$.data.description").value("We are looking for a software engineer"));
    }

    @Test
    void getJobById_returns404_forDraftJob() throws Exception {
        String createResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andReturn().getResponse().getContentAsString();
        Long jobId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_works() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "Software")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void search_noResults_forNonMatchingQuery() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "NonExistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void filter_byLocation() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("location", "New York")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void filter_byEmploymentType() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("employmentType", "FULL_TIME")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void filter_byWorkplaceType() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("workplaceType", "HYBRID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void pagination_works() throws Exception {
        for (int i = 0; i < 5; i++) {
            createPublishedJob();
        }

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void sorting_byNewest() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("sort", "newest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void sorting_byOldest() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("sort", "oldest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void candidateCannotCreateJob() throws Exception {
        mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidJobRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void combinedSearch_keywordAndLocation() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "Software")
                        .param("location", "New York")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void combinedSearch_keywordAndFilters() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "Engineer")
                        .param("workplaceType", "HYBRID")
                        .param("employmentType", "FULL_TIME")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void combinedSearch_noMatchingFilters() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "Software")
                        .param("location", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void filter_byExperienceRange() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("experienceMin", "2")
                        .param("experienceMax", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void filter_bySalaryRange() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("salaryMin", "70000")
                        .param("salaryMax", "130000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void sorting_byDeadline() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("sort", "deadline")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void sorting_bySalaryHigh() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("sort", "salary_high")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void maxPageSize_enforced() throws Exception {
        for (int i = 0; i < 60; i++) {
            createPublishedJob();
        }

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(50))
                .andExpect(jsonPath("$.data.size").value(50));
    }

    @Test
    void emptyQueryAndFilters_returnsAllPublished() throws Exception {
        createPublishedJob();
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void search_skillsMatch() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "Spring Boot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void sqlInjection_inQuery_noResults() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("q", "'; DROP TABLE jobs; --")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void sqlInjection_inLocation_noResults() throws Exception {
        createPublishedJob();

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .param("location", "'; DROP TABLE jobs; --")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
