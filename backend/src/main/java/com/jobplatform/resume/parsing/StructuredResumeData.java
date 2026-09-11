package com.jobplatform.resume.parsing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredResumeData {

    private PersonalInfo personalInfo;
    private String professionalSummary;
    private String headline;
    private List<String> skills;
    private List<ExperienceData> experience;
    private List<EducationData> education;
    private List<CertificationData> certifications;
    private List<ProjectData> projects;
    private List<String> languages;
    private Integer totalYearsOfExperience;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfo {
        private String fullName;
        private String email;
        private String phone;
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceData {
        private String company;
        private String jobTitle;
        private String location;
        private String startDate;
        private String endDate;
        private Boolean current;
        private String description;
        private List<String> technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationData {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private String startDate;
        private String endDate;
        private String grade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationData {
        private String name;
        private String issuingOrganization;
        private String issueDate;
        private String expirationDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectData {
        private String name;
        private String description;
        private List<String> technologies;
        private String url;
    }
}
