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
public class ProfileCompletionResponse {

    private int overallPercentage;
    private List<SectionCompletion> sections;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionCompletion {
        private String section;
        private String label;
        private boolean completed;
        private int weight;
        private List<String> missingFields;
    }
}
