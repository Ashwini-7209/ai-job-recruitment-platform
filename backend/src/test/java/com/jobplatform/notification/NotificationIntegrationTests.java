package com.jobplatform.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.interview.dto.CreateInterviewRequest;
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
class NotificationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String candidateToken;
    private String recruiterToken;
    private Long jobId;
    private Long applicationId;

    @BeforeEach
    void setUp() throws Exception {
        String candidateEmail = "candidate-notif-" + System.nanoTime() + "@example.com";
        String recruiterEmail = "recruiter-notif-" + System.nanoTime() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Candidate").email(candidateEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Test Recruiter").email(recruiterEmail).password("password123").confirmPassword("password123").role(UserRole.RECRUITER).build())));

        candidateToken = loginAndGetToken(candidateEmail);
        recruiterToken = loginAndGetToken(recruiterEmail);

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
    }

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder().email(email).password("password123").build())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    // Test 1: Recruiter gets notification when candidate applies
    @Test
    void applyToJob_recruiterReceivesNotification() throws Exception {
        String applyResponse = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().coverLetter("I am interested").build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        applicationId = objectMapper.readTree(applyResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("APPLICATION_RECEIVED"))
                .andExpect(jsonPath("$.data.content[0].title").value("New Application Received"))
                .andExpect(jsonPath("$.data.content[0].read").value(false));
    }

    // Test 2: Candidate gets notification on status update
    @Test
    void updateStatus_candidateReceivesNotification() throws Exception {
        String applyResponse = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())))
                .andReturn().getResponse().getContentAsString();
        applicationId = objectMapper.readTree(applyResponse).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/recruiters/me/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"UNDER_REVIEW\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("APPLICATION_STATUS_CHANGED"))
                .andExpect(jsonPath("$.data.content[0].title").value("Application Status Updated"));
    }

    // Test 3: Candidate gets notification on interview scheduled
    @Test
    void scheduleInterview_candidateReceivesNotification() throws Exception {
        String applyResponse = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())))
                .andReturn().getResponse().getContentAsString();
        applicationId = objectMapper.readTree(applyResponse).path("data").path("id").asLong();

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
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("INTERVIEW_SCHEDULED"))
                .andExpect(jsonPath("$.data.content[0].title").value("Interview Scheduled"));
    }

    // Test 4: Unread count is correct
    @Test
    void getUnreadCount_returnsCorrectCount() throws Exception {
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    // Test 5: Mark notification as read
    @Test
    void markAsRead_notificationIsMarkedRead() throws Exception {
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));

        String listResponse = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andReturn().getResponse().getContentAsString();
        Long notificationId = objectMapper.readTree(listResponse).path("data").path("content").get(0).path("id").asLong();

        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }

    // Test 6: Mark all as read
    @Test
    void markAllAsRead_allNotificationsMarkedRead() throws Exception {
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));

        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }

    // Test 7: Candidate sees no notifications for other user's events
    @Test
    void getNotifications_userOnlySeesOwn() throws Exception {
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));

        String otherEmail = "other-candidate-notif-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .fullName("Other Candidate").email(otherEmail).password("password123").confirmPassword("password123").role(UserRole.CANDIDATE).build())));
        String otherToken = loginAndGetToken(otherEmail);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Test 8: Candidate gets notification on interview cancelled
    @Test
    void cancelInterview_candidateReceivesNotification() throws Exception {
        String applyResponse = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())))
                .andReturn().getResponse().getContentAsString();
        applicationId = objectMapper.readTree(applyResponse).path("data").path("id").asLong();

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        String interviewResponse = mockMvc.perform(post("/api/recruiters/me/applications/" + applicationId + "/interviews")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateInterviewRequest.builder()
                                .title("Technical Interview")
                                .interviewType(InterviewType.VIDEO)
                                .scheduledStart(start)
                                .scheduledEnd(end)
                                .build())))
                .andReturn().getResponse().getContentAsString();
        Long interviewId = objectMapper.readTree(interviewResponse).path("data").path("interviewId").asLong();

        mockMvc.perform(patch("/api/recruiters/me/interviews/" + interviewId + "/cancel")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].type").value("INTERVIEW_CANCELLED"))
                .andExpect(jsonPath("$.data.content[0].title").value("Interview Cancelled"));
    }

    // Test 9: Recruiter gets notification on application withdrawn
    @Test
    void withdrawApplication_recruiterReceivesNotification() throws Exception {
        String applyResponse = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())))
                .andReturn().getResponse().getContentAsString();
        applicationId = objectMapper.readTree(applyResponse).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/candidates/me/applications/" + applicationId + "/withdraw")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].type").value("APPLICATION_STATUS_CHANGED"))
                .andExpect(jsonPath("$.data.content[0].title").value("Application Withdrawn"));
    }

    // Test 10: Notifications are ordered by newest first
    @Test
    void getNotifications_orderedByNewestFirst() throws Exception {
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().build())));

        String secondJobResponse = mockMvc.perform(post("/api/recruiter/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateJobRequest.builder()
                                .title("Product Manager")
                                .description("Looking for a product manager")
                                .location("San Francisco")
                                .employmentType(EmploymentType.FULL_TIME)
                                .workplaceType(WorkplaceType.REMOTE)
                                .build())))
                .andReturn().getResponse().getContentAsString();
        Long secondJobId = objectMapper.readTree(secondJobResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/recruiter/jobs/" + secondJobId + "/publish")
                .header("Authorization", "Bearer " + recruiterToken));

        mockMvc.perform(post("/api/jobs/" + secondJobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateApplicationRequest.builder().coverLetter("Second application").build())));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    // Test 11: Unauthenticated user cannot access notifications
    @Test
    void unauthenticatedUser_cannotAccessNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isUnauthorized());
    }
}
