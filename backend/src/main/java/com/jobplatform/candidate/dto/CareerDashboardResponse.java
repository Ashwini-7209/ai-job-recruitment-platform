package com.jobplatform.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerDashboardResponse {

    private int profileCompletion;
    private List<String> strongestSkills;
    private List<String> matchingJobCategories;
    private List<SkillGap> skillGaps;
    private ApplicationActivity applicationActivity;
    private List<RecommendedJob> recommendedJobs;
    private List<UpcomingInterview> upcomingInterviews;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillGap {
        private String skill;
        private String reason;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationActivity {
        private int totalApplications;
        private int underReview;
        private int shortlisted;
        private int interviews;
        private int hired;
        private int responseRate;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedJob {
        private Long jobId;
        private String title;
        private String company;
        private String location;
        private int matchScore;
        private String reason;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingInterview {
        private Long interviewId;
        private String jobTitle;
        private String title;
        private String interviewType;
        private String scheduledStart;
        private String location;
    }
}
