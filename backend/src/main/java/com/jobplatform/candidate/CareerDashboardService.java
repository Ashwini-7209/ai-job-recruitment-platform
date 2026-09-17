package com.jobplatform.candidate;

import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.candidate.dto.CareerDashboardResponse;
import com.jobplatform.interview.InterviewRepository;
import com.jobplatform.interview.Interview;
import com.jobplatform.job.JobRepository;
import com.jobplatform.matching.JobRecommendationService;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CareerDashboardService {

    private static final Logger log = LoggerFactory.getLogger(CareerDashboardService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final CandidateProfileRepository candidateProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final JobRecommendationService jobRecommendationService;

    public CareerDashboardService(CandidateProfileRepository candidateProfileRepository,
                                  ApplicationRepository applicationRepository,
                                  InterviewRepository interviewRepository,
                                  JobRepository jobRepository,
                                  ResumeRepository resumeRepository,
                                  JobRecommendationService jobRecommendationService) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.jobRecommendationService = jobRecommendationService;
    }

    @Transactional(readOnly = true)
    public CareerDashboardResponse getCareerDashboard(User candidate) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(candidate.getId()).orElse(null);

        int profileCompletion = calculateProfileCompletion(profile, candidate);

        List<String> strongestSkills = extractStrongestSkills(profile);

        List<String> matchingJobCategories = determineMatchingCategories(profile);

        List<CareerDashboardResponse.SkillGap> skillGaps = identifySkillGaps(profile);

        CareerDashboardResponse.ApplicationActivity activity = buildApplicationActivity(candidate);

        List<CareerDashboardResponse.RecommendedJob> recommendedJobs = getRecommendedJobs(candidate);

        List<CareerDashboardResponse.UpcomingInterview> upcomingInterviews = getUpcomingInterviews(candidate);

        return CareerDashboardResponse.builder()
                .profileCompletion(profileCompletion)
                .strongestSkills(strongestSkills)
                .matchingJobCategories(matchingJobCategories)
                .skillGaps(skillGaps)
                .applicationActivity(activity)
                .recommendedJobs(recommendedJobs)
                .upcomingInterviews(upcomingInterviews)
                .build();
    }

    private int calculateProfileCompletion(CandidateProfile profile, User user) {
        if (profile == null) return 0;
        int total = 0;
        int filled = 0;

        total++; if (profile.getPhone() != null && !profile.getPhone().isBlank()) filled++;
        total++; if (profile.getLocation() != null && !profile.getLocation().isBlank()) filled++;
        total++; if (profile.getHeadline() != null && !profile.getHeadline().isBlank()) filled++;
        total++; if (profile.getBio() != null && !profile.getBio().isBlank()) filled++;
        total++; if (profile.getCurrentJobTitle() != null && !profile.getCurrentJobTitle().isBlank()) filled++;
        total++; if (profile.getYearsOfExperience() != null) filled++;
        total++; if (profile.getSkillsSummary() != null && !profile.getSkillsSummary().isBlank()) filled++;
        total++; if (profile.getEducationSummary() != null && !profile.getEducationSummary().isBlank()) filled++;
        total++; if (resumeRepository.existsByCandidateAndActiveTrue(user)) filled++;

        return total > 0 ? (int) (filled * 100.0 / total) : 0;
    }

    private List<String> extractStrongestSkills(CandidateProfile profile) {
        if (profile == null || profile.getSkillsSummary() == null || profile.getSkillsSummary().isBlank()) {
            return List.of();
        }
        String[] skills = profile.getSkillsSummary().split(",");
        List<String> result = new ArrayList<>();
        for (String skill : skills) {
            String trimmed = skill.trim();
            if (!trimmed.isEmpty() && result.size() < 8) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private List<String> determineMatchingCategories(CandidateProfile profile) {
        List<String> categories = new ArrayList<>();
        if (profile == null) return categories;

        String title = profile.getCurrentJobTitle();
        String skills = profile.getSkillsSummary();

        if (title != null) {
            String lower = title.toLowerCase();
            if (lower.contains("engineer") || lower.contains("developer") || lower.contains("programmer")) {
                categories.add("Software Engineering");
            }
            if (lower.contains("design") || lower.contains("ux") || lower.contains("ui")) {
                categories.add("Design");
            }
            if (lower.contains("data") || lower.contains("analytics") || lower.contains("scientist")) {
                categories.add("Data Science");
            }
            if (lower.contains("manager") || lower.contains("lead") || lower.contains("director")) {
                categories.add("Management");
            }
            if (lower.contains("marketing") || lower.contains("growth")) {
                categories.add("Marketing");
            }
            if (lower.contains("sales") || lower.contains("account")) {
                categories.add("Sales");
            }
        }

        if (skills != null) {
            String lower = skills.toLowerCase();
            if (lower.contains("java") || lower.contains("python") || lower.contains("javascript") || lower.contains("react") || lower.contains("node")) {
                if (!categories.contains("Software Engineering")) categories.add("Software Engineering");
            }
            if (lower.contains("figma") || lower.contains("sketch") || lower.contains("photoshop")) {
                if (!categories.contains("Design")) categories.add("Design");
            }
            if (lower.contains("sql") || lower.contains("tableau") || lower.contains("power bi")) {
                if (!categories.contains("Data Science")) categories.add("Data Science");
            }
        }

        if (categories.isEmpty()) {
            categories.add("General");
        }

        return categories;
    }

    private List<CareerDashboardResponse.SkillGap> identifySkillGaps(CandidateProfile profile) {
        List<CareerDashboardResponse.SkillGap> gaps = new ArrayList<>();
        if (profile == null) return gaps;

        String skills = profile.getSkillsSummary();
        Integer yoe = profile.getYearsOfExperience();

        if (skills == null || skills.isBlank()) {
            gaps.add(CareerDashboardResponse.SkillGap.builder()
                    .skill("Technical Skills")
                    .reason("Add your skills to improve job matching and career recommendations")
                    .build());
        }

        if (yoe == null || yoe < 2) {
            gaps.add(CareerDashboardResponse.SkillGap.builder()
                    .skill("Professional Experience")
                    .reason("Consider adding more work experience details to strengthen your profile")
                    .build());
        }

        if (profile.getEducationSummary() == null || profile.getEducationSummary().isBlank()) {
            gaps.add(CareerDashboardResponse.SkillGap.builder()
                    .skill("Education")
                    .reason("Adding your education background helps recruiters evaluate your qualifications")
                    .build());
        }

        if (profile.getLinkedinUrl() == null && profile.getGithubUrl() == null && profile.getPortfolioUrl() == null) {
            gaps.add(CareerDashboardResponse.SkillGap.builder()
                    .skill("Online Presence")
                    .reason("Adding professional links (LinkedIn, GitHub, portfolio) increases your visibility")
                    .build());
        }

        return gaps;
    }

    private CareerDashboardResponse.ApplicationActivity buildApplicationActivity(User candidate) {
        long total = applicationRepository.countByCandidate(candidate);
        long underReview = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.UNDER_REVIEW);
        long shortlisted = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.SHORTLISTED);
        long interview = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.INTERVIEW);
        long hired = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.HIRED);

        int responseRate = total > 0 ? (int) ((underReview + shortlisted + interview + hired) * 100 / total) : 0;

        return CareerDashboardResponse.ApplicationActivity.builder()
                .totalApplications((int) total)
                .underReview((int) underReview)
                .shortlisted((int) shortlisted)
                .interviews((int) interview)
                .hired((int) hired)
                .responseRate(responseRate)
                .build();
    }

    private List<CareerDashboardResponse.RecommendedJob> getRecommendedJobs(User candidate) {
        try {
            JobRecommendationService.RecommendationResult result = jobRecommendationService.getRecommendations(candidate, 0, 5, null);
            return result.getMatches().stream().map(match -> {
                String reason = match.getMatchingReasons() != null && !match.getMatchingReasons().isEmpty()
                        ? match.getMatchingReasons().get(0) : "Matches your profile";
                return CareerDashboardResponse.RecommendedJob.builder()
                        .jobId(match.getJobId())
                        .title(match.getJobTitle())
                        .company(null)
                        .location(null)
                        .matchScore(match.getOverallScore())
                        .reason(reason)
                        .build();
            }).toList();
        } catch (Exception e) {
            log.warn("Failed to get recommendations for career dashboard: {}", e.getMessage());
            return List.of();
        }
    }

    private List<CareerDashboardResponse.UpcomingInterview> getUpcomingInterviews(User candidate) {
        try {
            List<Interview> interviews = interviewRepository.findUpcomingByCandidate(
                    candidate,
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now().plusWeeks(2)
            );
            return interviews.stream().limit(5).map(i -> CareerDashboardResponse.UpcomingInterview.builder()
                    .interviewId(i.getId())
                    .jobTitle(i.getApplication().getJob().getTitle())
                    .title(i.getTitle())
                    .interviewType(i.getInterviewType().name())
                    .scheduledStart(i.getScheduledStart().format(DATE_FORMAT))
                    .location(i.getLocation())
                    .build()).toList();
        } catch (Exception e) {
            log.warn("Failed to get upcoming interviews: {}", e.getMessage());
            return List.of();
        }
    }
}
