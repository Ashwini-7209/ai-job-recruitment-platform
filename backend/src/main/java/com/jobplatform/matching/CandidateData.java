package com.jobplatform.matching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateData {

    private Long candidateId;

    private String headline;
    private String bio;
    private String currentJobTitle;
    private String location;

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    private Integer yearsOfExperience;

    private String professionalSummary;

    @Builder.Default
    private List<String> experienceTechnologies = new ArrayList<>();

    @Builder.Default
    private List<String> projectTechnologies = new ArrayList<>();

    private boolean resumeDataAvailable;
    private boolean profileDataAvailable;
}
