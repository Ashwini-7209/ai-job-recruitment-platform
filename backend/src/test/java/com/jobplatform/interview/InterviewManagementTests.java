package com.jobplatform.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.interview.dto.CreateInterviewRequest;
import com.jobplatform.interview.dto.UpdateInterviewRequest;
import com.jobplatform.interview.enums.InterviewType;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InterviewManagementTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String otherCandidateToken;
    private String recruiterToken;
    private String otherRecruiterToken;
    private Long jobId;
    private Long applicationId;
    private Long interviewId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-int-" + System.nanoTime() + "@example.com";
        String otherEmail = "other-int-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-int-" + System.nanoTime() + "@example.com";
        String otherRecEmail = "other-rec-int-" + System.nanoTime() + "@example.com";

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

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Other Recruiter").email(otherRecEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        candidateToken = loginAndGetToken(candidateEmail);
        otherCandidateToken = loginAndGetToken(otherEmail);
        recruiterToken = loginAndGetToken(recruiterEmail);
        otherRecruiterToken = loginAndGetToken(otherRecEmail);

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
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().coverLetter("I am interested").build())))
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

    private Long createInterview() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        String response = mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Technical Interview")
                                .interviewType(InterviewType.VIDEO)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .meetingLink("https://meet.google.com/abc-defg-hij")
                                .interviewerName("John Smith")
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("interviewId").asLong();
    }

    // Test 1: Recruiter creates interview for own application
    @Test
    void createInterview_recruiterCanCreateForOwnApplication() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Technical Interview")
                                .interviewType(InterviewType.VIDEO)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .meetingLink("https://meet.google.com/abc-defg-hij")
                                .interviewerName("John Smith")
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.interviewId").isNumber())
                .andExpect(jsonPath("$.data.title").value("Technical Interview"))
                .andExpect(jsonPath("$.data.interviewType").value("VIDEO"))
                .andExpect(jsonPath("$.data.candidateName").value("Test Candidate"))
                .andExpect(jsonPath("$.data.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    // Test 2: Recruiter cannot create interview for another recruiter's application
    @Test
    void createInterview_recruiterCannotCreateForOtherRecruiterApp() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + otherRecruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Should Fail")
                                .interviewType(InterviewType.VIDEO)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .build())))
                .andExpect(status().isBadRequest());
    }

    // Test 3: Candidate can view own interview
    @Test
    void getCandidateInterviews_candidateCanViewOwn() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/candidates/me/interviews")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Technical Interview"))
                .andExpect(jsonPath("$.data.content[0].interviewType").value("VIDEO"))
                .andExpect(jsonPath("$.data.content[0].status").value("SCHEDULED"));
    }

    // Test 4: Candidate cannot view another candidate's interview
    @Test
    void getCandidateInterviews_otherCandidateSeesNothing() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/candidates/me/interviews")
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Test 5: Recruiter can update interview
    @Test
    void updateInterview_recruiterCanUpdate() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(patch("/api/recruiters/me/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateInterviewRequest.builder()
                                .title("Updated Technical Interview")
                                .interviewerName("Jane Doe")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Technical Interview"))
                .andExpect(jsonPath("$.data.interviewerName").value("Jane Doe"));
    }

    // Test 6: Recruiter can reschedule interview
    @Test
    void rescheduleInterview_recruiterCanReschedule() throws Exception {
        interviewId = createInterview();

        LocalDateTime newStart = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        LocalDateTime newEnd = newStart.plusHours(1);

        mockMvc.perform(patch("/api/recruiters/me/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateInterviewRequest.builder()
                                .scheduledStart(newStart)
                                .scheduledEnd(newEnd)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESCHEDULED"));
    }

    // Test 7: Recruiter can cancel interview
    @Test
    void cancelInterview_recruiterCanCancel() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(patch("/api/recruiters/me/interviews/" + interviewId + "/cancel")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    // Test 8: Invalid time range is rejected
    @Test
    void createInterview_invalidTimeRangeRejected() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime end = start.minusHours(1);

        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Invalid Time")
                                .interviewType(InterviewType.VIDEO)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .build())))
                .andExpect(status().isBadRequest());
    }

    // Test 9: Invalid status transition is rejected
    @Test
    void updateStatus_invalidTransitionRejected() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(patch("/api/recruiters/me/interviews/" + interviewId + "/cancel")
                        .header("Authorization", "Bearer " + recruiterToken));

        mockMvc.perform(patch("/api/recruiters/me/interviews/" + interviewId + "/cancel")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isBadRequest());
    }

    // Test 10: Upcoming interviews are returned correctly
    @Test
    void getUpcomingInterviews_returnsCorrect() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/recruiters/me/interviews/upcoming")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Technical Interview"));
    }

    // Test 11: Candidate cannot create interviews
    @Test
    void createInterview_candidateCannotCreate() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Should Fail")
                                .interviewType(InterviewType.VIDEO)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .build())))
                .andExpect(status().isForbidden());
    }

    // Test 12: Candidate can view interview detail
    @Test
    void getCandidateInterviewDetail_candidateCanViewOwn() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/candidates/me/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.interviewId").value(interviewId))
                .andExpect(jsonPath("$.data.title").value("Technical Interview"))
                .andExpect(jsonPath("$.data.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.data.meetingLink").value("https://meet.google.com/abc-defg-hij"));
    }

    // Test 13: Candidate cannot view another candidate's interview
    @Test
    void getCandidateInterviewDetail_cannotViewOtherCandidate() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/candidates/me/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + otherCandidateToken))
                .andExpect(status().isNotFound());
    }

    // Test 14: Recruiter can get interview detail
    @Test
    void getRecruiterInterviewDetail_recruiterCanViewOwn() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/recruiters/me/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.interviewId").value(interviewId))
                .andExpect(jsonPath("$.data.candidateName").value("Test Candidate"))
                .andExpect(jsonPath("$.data.interviewerNotes").doesNotExist());
    }

    // Test 15: Recruiter cannot view another recruiter's interview
    @Test
    void getRecruiterInterviewDetail_cannotViewOtherRecruiter() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/recruiters/me/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + otherRecruiterToken))
                .andExpect(status().isNotFound());
    }

    // Test 16: Unauthenticated user cannot access interview APIs
    @Test
    void unauthenticatedUser_cannotAccess() throws Exception {
        mockMvc.perform(get("/api/candidates/me/interviews"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/recruiters/me/interviews"))
                .andExpect(status().isUnauthorized());
    }

    // Test 17: Candidate upcoming interviews count
    @Test
    void candidateInterviewStats_returnsCorrectCount() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/candidates/me/interview-stats")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    // Test 18: Recruiter upcoming interviews count
    @Test
    void recruiterInterviewStats_returnsCorrectCount() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/recruiters/me/interview-stats")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    // Test 19: Candidate upcoming interviews
    @Test
    void getCandidateUpcomingInterviews_returnsCorrect() throws Exception {
        interviewId = createInterview();

        mockMvc.perform(get("/api/candidates/me/interviews/upcoming")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Technical Interview"));
    }

    // Test 20: Invalid meeting time (past) is rejected
    @Test
    void createInterview_pastTimeRejected() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Past Interview")
                                .interviewType(InterviewType.PHONE)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .build())))
                .andExpect(status().isBadRequest());
    }
}
