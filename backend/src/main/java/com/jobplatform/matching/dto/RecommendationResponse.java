package com.jobplatform.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private List<JobMatchResponse> matches;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public static RecommendationResponse from(com.jobplatform.matching.JobRecommendationService.RecommendationResult result) {
        return RecommendationResponse.builder()
                .matches(result.getMatches().stream()
                        .map(JobMatchResponse::from)
                        .toList())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .currentPage(result.getCurrentPage())
                .pageSize(result.getPageSize())
                .build();
    }
}
