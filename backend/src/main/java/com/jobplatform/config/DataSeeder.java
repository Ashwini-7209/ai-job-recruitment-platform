package com.jobplatform.config;

import com.jobplatform.admin.audit.AuditLog;
import com.jobplatform.admin.audit.AuditLogRepository;
import com.jobplatform.application.Application;
import com.jobplatform.application.ApplicationNote;
import com.jobplatform.application.ApplicationNoteRepository;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.ApplicationStatusHistory;
import com.jobplatform.application.ApplicationStatusHistoryRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.candidate.CandidateProfile;
import com.jobplatform.candidate.CandidateProfileRepository;
import com.jobplatform.interview.Interview;
import com.jobplatform.interview.InterviewRepository;
import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.interview.enums.InterviewType;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.jobalert.JobAlert;
import com.jobplatform.jobalert.JobAlertRepository;
import com.jobplatform.notification.Notification;
import com.jobplatform.notification.NotificationPreference;
import com.jobplatform.notification.NotificationPreferenceRepository;
import com.jobplatform.notification.NotificationRepository;
import com.jobplatform.notification.NotificationType;
import com.jobplatform.recruiter.RecruiterProfile;
import com.jobplatform.recruiter.RecruiterProfileRepository;
import com.jobplatform.savedjob.SavedJob;
import com.jobplatform.savedjob.SavedJobRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final InterviewRepository interviewRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobAlertRepository jobAlertRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CandidateProfileRepository candidateProfileRepository,
                      RecruiterProfileRepository recruiterProfileRepository,
                      JobRepository jobRepository,
                      ApplicationRepository applicationRepository,
                      ApplicationStatusHistoryRepository applicationStatusHistoryRepository,
                      ApplicationNoteRepository applicationNoteRepository,
                      InterviewRepository interviewRepository,
                      SavedJobRepository savedJobRepository,
                      JobAlertRepository jobAlertRepository,
                      NotificationRepository notificationRepository,
                      NotificationPreferenceRepository notificationPreferenceRepository,
                      AuditLogRepository auditLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.applicationStatusHistoryRepository = applicationStatusHistoryRepository;
        this.applicationNoteRepository = applicationNoteRepository;
        this.interviewRepository = interviewRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobAlertRepository = jobAlertRepository;
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping seed.");
            return;
        }

        log.info("Seeding demo data...");

        // 1. Create Users
        User admin = createUser("Admin User", "admin@demo.com", "password123", UserRole.ADMIN);
        User recruiter1 = createUser("Sarah Johnson", "recruiter@demo.com", "password123", UserRole.RECRUITER);
        User recruiter2 = createUser("Michael Chen", "recruiter2@demo.com", "password123", UserRole.RECRUITER);
        User candidate1 = createUser("Alice Williams", "candidate@demo.com", "password123", UserRole.CANDIDATE);
        User candidate2 = createUser("James Brown", "candidate2@demo.com", "password123", UserRole.CANDIDATE);
        User candidate3 = createUser("Emily Davis", "candidate3@demo.com", "password123", UserRole.CANDIDATE);

        // 2. Create Profiles
        createCandidateProfile(candidate1, "Senior Software Engineer", "San Francisco, CA",
                "Full-stack developer with 5 years of experience in React and Java",
                "Senior Software Engineer", 5,
                "BS Computer Science, Stanford University",
                "Java, React, TypeScript, Spring Boot, PostgreSQL, AWS",
                "https://linkedin.com/in/alicewilliams",
                "https://github.com/alicewilliams");

        createCandidateProfile(candidate2, "Frontend Developer", "New York, NY",
                "Passionate frontend developer specializing in modern web technologies",
                "Frontend Developer", 3,
                "BS Software Engineering, MIT",
                "JavaScript, React, Vue.js, CSS, HTML, Redux",
                "https://linkedin.com/in/jamesbrown",
                "https://github.com/jamesbrown");

        createCandidateProfile(candidate3, "DevOps Engineer", "Austin, TX",
                "Cloud infrastructure and CI/CD specialist with expertise in AWS and Docker",
                "DevOps Engineer", 4,
                "MS Computer Science, Georgia Tech",
                "AWS, Docker, Kubernetes, Terraform, Jenkins, Python",
                "https://linkedin.com/in/emilydavis",
                "https://github.com/emilydavis");

        createRecruiterProfile(recruiter1, "HR Director", "TechCorp Inc.",
                "Leading talent acquisition for a Fortune 500 tech company",
                "https://techcorp.com", "San Francisco, CA",
                "https://linkedin.com/in/sarahjohnson");

        createRecruiterProfile(recruiter2, "Technical Recruiter", "InnovateLab",
                "Specializing in hiring top engineering talent for startups",
                "https://innovatelab.io", "New York, NY",
                "https://linkedin.com/in/michaelchen");

        // 3. Create Jobs
        Job job1 = createJob(recruiter1, "Senior Java Developer",
                "We are looking for an experienced Java developer to join our backend team. You will work on microservices architecture and cloud-native applications.",
                "San Francisco, CA", EmploymentType.FULL_TIME, WorkplaceType.HYBRID,
                5, 8, 120000, 160000, "Java, Spring Boot, Microservices, AWS, PostgreSQL",
                JobStatus.PUBLISHED);

        Job job2 = createJob(recruiter1, "React Frontend Developer",
                "Join our frontend team to build cutting-edge user interfaces using React and TypeScript.",
                "Remote", EmploymentType.FULL_TIME, WorkplaceType.REMOTE,
                3, 6, 100000, 140000, "React, TypeScript, Redux, CSS, HTML, JavaScript",
                JobStatus.PUBLISHED);

        Job job3 = createJob(recruiter2, "DevOps Engineer",
                "Help us build and maintain our cloud infrastructure on AWS. Experience with containerization required.",
                "New York, NY", EmploymentType.FULL_TIME, WorkplaceType.HYBRID,
                3, 5, 110000, 150000, "AWS, Docker, Kubernetes, Terraform, Jenkins, Linux",
                JobStatus.PUBLISHED);

        Job job4 = createJob(recruiter2, "Full Stack Developer",
                "Looking for a versatile developer who can work across the entire stack.",
                "Austin, TX", EmploymentType.FULL_TIME, WorkplaceType.ONSITE,
                2, 5, 90000, 130000, "Java, React, PostgreSQL, REST APIs, Git",
                JobStatus.PUBLISHED);

        Job job5 = createJob(recruiter1, "Junior Software Engineer",
                "Great opportunity for recent graduates to start their career in software development.",
                "San Francisco, CA", EmploymentType.FULL_TIME, WorkplaceType.ONSITE,
                0, 2, 70000, 90000, "Java, Python, SQL, Git, REST APIs",
                JobStatus.PUBLISHED);

        Job job6 = createJob(recruiter2, "Cloud Architect",
                "Design and implement scalable cloud solutions for enterprise clients.",
                "Remote", EmploymentType.CONTRACT, WorkplaceType.REMOTE,
                8, 12, 150000, 200000, "AWS, Azure, GCP, Terraform, Microservices, Architecture",
                JobStatus.PUBLISHED);

        Job job7 = createJob(recruiter1, "QA Automation Engineer",
                "Build and maintain automated test suites for our web applications.",
                "San Francisco, CA", EmploymentType.FULL_TIME, WorkplaceType.HYBRID,
                2, 4, 85000, 115000, "Selenium, JUnit, Java, JavaScript, CI/CD, API Testing",
                JobStatus.CLOSED);

        Job job8 = createJob(recruiter2, "Data Engineer",
                "Design and optimize data pipelines for our analytics platform.",
                "New York, NY", EmploymentType.FULL_TIME, WorkplaceType.HYBRID,
                4, 7, 130000, 170000, "Python, Spark, Kafka, SQL, AWS, Airflow",
                JobStatus.PUBLISHED);

        // 4. Create Applications
        Application app1 = createApplication(candidate1, job1, ApplicationStatus.SHORTLISTED,
                "I am very interested in this position. With my 5 years of Java experience, I believe I would be a great fit for your team.");

        Application app2 = createApplication(candidate1, job2, ApplicationStatus.APPLIED,
                "I have extensive experience with React and would love to contribute to your frontend team.");

        Application app3 = createApplication(candidate2, job2, ApplicationStatus.UNDER_REVIEW,
                "As a frontend developer, this role aligns perfectly with my skills and career goals.");

        Application app4 = createApplication(candidate2, job4, ApplicationStatus.REJECTED,
                "I am a full stack developer with experience in both Java and React.");

        Application app5 = createApplication(candidate3, job3, ApplicationStatus.HIRED,
                "With my 4 years of DevOps experience and AWS expertise, I am confident I can contribute significantly.");

        Application app6 = createApplication(candidate1, job5, ApplicationStatus.APPLIED,
                "I am interested in mentoring junior developers and this role offers that opportunity.");

        Application app7 = createApplication(candidate3, job6, ApplicationStatus.SHORTLISTED,
                "I have extensive cloud architecture experience and would love to work on enterprise solutions.");

        Application app8 = createApplication(candidate2, job1, ApplicationStatus.WITHDRAWN,
                "I applied but found a role that better matches my frontend specialization.");

        // 5. Create Application Status History
        createStatusHistory(app1, null, ApplicationStatus.APPLIED, recruiter1);
        createStatusHistory(app1, ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW, recruiter1);
        createStatusHistory(app1, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED, recruiter1);

        createStatusHistory(app3, null, ApplicationStatus.APPLIED, recruiter2);
        createStatusHistory(app3, ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW, recruiter2);

        createStatusHistory(app4, null, ApplicationStatus.APPLIED, recruiter2);
        createStatusHistory(app4, ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW, recruiter2);
        createStatusHistory(app4, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.REJECTED, recruiter2);

        createStatusHistory(app5, null, ApplicationStatus.APPLIED, recruiter2);
        createStatusHistory(app5, ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW, recruiter2);
        createStatusHistory(app5, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED, recruiter2);
        createStatusHistory(app5, ApplicationStatus.SHORTLISTED, ApplicationStatus.HIRED, recruiter2);

        // 6. Create Application Notes
        createApplicationNote(app1, recruiter1, "Strong candidate. Excellent Java skills and good communication.");
        createApplicationNote(app1, recruiter1, "Scheduled for technical interview next week.");
        createApplicationNote(app3, recruiter2, "Good portfolio. Needs to demonstrate more backend experience.");
        createApplicationNote(app5, recruiter2, "Outstanding DevOps skills. Perfect fit for the role.");

        // 7. Create Interviews
        createInterview(app1, "Technical Interview - Java", InterviewType.VIDEO,
                LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(3).withHour(11).withMinute(30),
                "https://zoom.us/j/123456789", recruiter1.getFullName(), InterviewStatus.SCHEDULED);

        createInterview(app1, "Behavioral Interview", InterviewType.VIDEO,
                LocalDateTime.now().plusDays(7).withHour(14).withMinute(0),
                LocalDateTime.now().plusDays(7).withHour(15).withMinute(0),
                "https://zoom.us/j/987654321", recruiter1.getFullName(), InterviewStatus.SCHEDULED);

        createInterview(app5, "Final Interview", InterviewType.IN_PERSON,
                LocalDateTime.now().plusDays(2).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(30),
                "InnovateLab HQ, 456 Innovation Ave, New York", recruiter2.getFullName(), InterviewStatus.SCHEDULED);

        createInterview(app3, "Initial Screening", InterviewType.PHONE,
                LocalDateTime.now().minusDays(5).withHour(11).withMinute(0),
                LocalDateTime.now().minusDays(5).withHour(11).withMinute(30),
                null, recruiter2.getFullName(), InterviewStatus.COMPLETED);

        // 8. Create Saved Jobs
        createSavedJob(candidate1, job3);
        createSavedJob(candidate1, job6);
        createSavedJob(candidate2, job1);
        createSavedJob(candidate2, job3);
        createSavedJob(candidate3, job1);
        createSavedJob(candidate3, job2);

        // 9. Create Job Alerts
        createJobAlert(candidate1, "Java Remote Jobs", "Java Spring Boot", "Remote", WorkplaceType.REMOTE, EmploymentType.FULL_TIME, 3);
        createJobAlert(candidate2, "Frontend Opportunities", "React TypeScript", null, null, EmploymentType.FULL_TIME, 2);
        createJobAlert(candidate3, "DevOps Cloud Roles", "AWS Docker Kubernetes", "Remote", WorkplaceType.REMOTE, null, 3);

        // 10. Create Notifications
        createNotification(candidate1, NotificationType.APPLICATION_STATUS_CHANGED,
                "Application Status Updated", "Your application for Senior Java Developer has been shortlisted.", app1.getId(), "APPLICATION");
        createNotification(candidate1, NotificationType.INTERVIEW_SCHEDULED,
                "Interview Scheduled", "Technical interview for Senior Java Developer scheduled for next week.", app1.getId(), "APPLICATION");
        createNotification(candidate2, NotificationType.APPLICATION_STATUS_CHANGED,
                "Application Update", "Your application for React Frontend Developer is now under review.", app3.getId(), "APPLICATION");
        createNotification(candidate3, NotificationType.APPLICATION_STATUS_CHANGED,
                "Congratulations!", "Your application for DevOps Engineer has been accepted! Welcome to the team.", app5.getId(), "APPLICATION");
        createNotification(candidate1, NotificationType.JOB_RECOMMENDATION,
                "New Job Match", "We found a Cloud Architect position that matches your skills.", job6.getId(), "JOB");
        createNotification(recruiter1, NotificationType.APPLICATION_RECEIVED,
                "New Application", "Alice Williams has applied for Senior Java Developer.", app1.getId(), "APPLICATION");
        createNotification(recruiter2, NotificationType.APPLICATION_RECEIVED,
                "New Application", "Emily Davis has applied for DevOps Engineer.", app5.getId(), "APPLICATION");

        // 11. Create Notification Preferences
        createNotificationPreferences(candidate1);
        createNotificationPreferences(candidate2);
        createNotificationPreferences(candidate3);
        createNotificationPreferences(recruiter1);
        createNotificationPreferences(recruiter2);

        // 12. Create Audit Logs
        createAuditLog(admin, "CREATE", "USER", candidate1.getId(), "Created candidate account for Alice Williams");
        createAuditLog(admin, "CREATE", "USER", candidate2.getId(), "Created candidate account for James Brown");
        createAuditLog(admin, "CREATE", "USER", recruiter1.getId(), "Created recruiter account for Sarah Johnson");
        createAuditLog(recruiter1, "CREATE", "JOB", job1.getId(), "Published job: Senior Java Developer");
        createAuditLog(recruiter1, "CREATE", "JOB", job2.getId(), "Published job: React Frontend Developer");
        createAuditLog(recruiter2, "CREATE", "JOB", job3.getId(), "Published job: DevOps Engineer");
        createAuditLog(recruiter2, "UPDATE", "APPLICATION", app5.getId(), "Changed application status to HIRED");

        log.info("Demo data seeded successfully!");
        log.info("=== DEMO CREDENTIALS ===");
        log.info("Admin:      admin@demo.com / password123");
        log.info("Recruiter:  recruiter@demo.com / password123");
        log.info("Recruiter2: recruiter2@demo.com / password123");
        log.info("Candidate:  candidate@demo.com / password123");
        log.info("Candidate2: candidate2@demo.com / password123");
        log.info("Candidate3: candidate3@demo.com / password123");
    }

    private User createUser(String fullName, String email, String password, UserRole role) {
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private void createCandidateProfile(User user, String headline, String location, String bio,
                                         String currentJobTitle, int yearsOfExperience,
                                         String educationSummary, String skillsSummary,
                                         String linkedinUrl, String githubUrl) {
        CandidateProfile profile = CandidateProfile.builder()
                .user(user)
                .headline(headline)
                .location(location)
                .bio(bio)
                .currentJobTitle(currentJobTitle)
                .yearsOfExperience(yearsOfExperience)
                .educationSummary(educationSummary)
                .skillsSummary(skillsSummary)
                .linkedinUrl(linkedinUrl)
                .githubUrl(githubUrl)
                .build();
        candidateProfileRepository.save(profile);
    }

    private void createRecruiterProfile(User user, String jobTitle, String companyName,
                                         String companyDescription, String companyWebsite,
                                         String companyLocation, String linkedinUrl) {
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(user)
                .jobTitle(jobTitle)
                .companyName(companyName)
                .companyDescription(companyDescription)
                .companyWebsite(companyWebsite)
                .companyLocation(companyLocation)
                .linkedinUrl(linkedinUrl)
                .build();
        recruiterProfileRepository.save(profile);
    }

    private Job createJob(User recruiter, String title, String description, String location,
                          EmploymentType employmentType, WorkplaceType workplaceType,
                          int experienceMin, int experienceMax,
                          int salaryMin, int salaryMax, String skills, JobStatus status) {
        Job job = Job.builder()
                .recruiter(recruiter)
                .title(title)
                .description(description)
                .location(location)
                .employmentType(employmentType)
                .workplaceType(workplaceType)
                .experienceMin(experienceMin)
                .experienceMax(experienceMax)
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .skills(skills)
                .status(status)
                .applicationDeadline(LocalDateTime.now().plusMonths(2))
                .build();
        if (status == JobStatus.PUBLISHED) {
            job.setPublishedAt(LocalDateTime.now().minusDays((long) (Math.random() * 14)));
        }
        return jobRepository.save(job);
    }

    private Application createApplication(User candidate, Job job, ApplicationStatus status, String coverLetter) {
        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .status(status)
                .coverLetter(coverLetter)
                .build();
        return applicationRepository.save(application);
    }

    private void createStatusHistory(Application application, ApplicationStatus oldStatus,
                                      ApplicationStatus newStatus, User changedBy) {
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(application)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();
        applicationStatusHistoryRepository.save(history);
    }

    private void createApplicationNote(Application application, User recruiter, String note) {
        ApplicationNote applicationNote = ApplicationNote.builder()
                .application(application)
                .recruiter(recruiter)
                .note(note)
                .build();
        applicationNoteRepository.save(applicationNote);
    }

    private void createInterview(Application application, String title, InterviewType type,
                                 LocalDateTime start, LocalDateTime end,
                                 String location, String interviewerName, InterviewStatus status) {
        Interview interview = Interview.builder()
                .application(application)
                .title(title)
                .interviewType(type)
                .scheduledStart(start)
                .scheduledEnd(end)
                .location(location)
                .interviewerName(interviewerName)
                .status(status)
                .build();
        interviewRepository.save(interview);
    }

    private void createSavedJob(User candidate, Job job) {
        SavedJob savedJob = SavedJob.builder()
                .candidate(candidate)
                .job(job)
                .build();
        savedJobRepository.save(savedJob);
    }

    private void createJobAlert(User candidate, String name, String keywords, String location,
                                WorkplaceType workplaceType, EmploymentType employmentType, int minExperience) {
        JobAlert alert = JobAlert.builder()
                .candidate(candidate)
                .name(name)
                .keywords(keywords)
                .location(location)
                .workplaceType(workplaceType)
                .employmentType(employmentType)
                .minimumExperience(minExperience)
                .active(true)
                .build();
        jobAlertRepository.save(alert);
    }

    private void createNotification(User user, NotificationType type, String title, String message,
                                     Long entityId, String entityType) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .entityId(entityId)
                .entityType(entityType)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    private void createNotificationPreferences(User user) {
        NotificationPreference preferences = NotificationPreference.builder()
                .user(user)
                .applicationStatus(true)
                .interviewUpdates(true)
                .jobAlerts(true)
                .systemNotifications(true)
                .build();
        notificationPreferenceRepository.save(preferences);
    }

    private void createAuditLog(User actor, String action, String entityType, Long entityId, String description) {
        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();
        auditLogRepository.save(auditLog);
    }
}
