package com.jobplatform.matching;

import com.jobplatform.matching.dto.CareerInsightsResponse;
import com.jobplatform.matching.dto.SkillGapResponse;
import com.jobplatform.matching.dto.ResumeImprovementResponse;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIEnhancementServiceTests {

    @Mock
    private CandidateDataResolver candidateDataResolver;

    @Mock
    private AIMatchingService aiMatchingService;

    @Mock
    private SkillGapService skillGapService;

    @InjectMocks
    private CareerInsightsService careerInsightsService;

    private User candidate;
    private CandidateData candidateData;

    @BeforeEach
    void setUp() {
        candidate = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.CANDIDATE)
                .fullName("Test User")
                .build();

        candidateData = CandidateData.builder()
                .candidateId(1L)
                .headline("Java Developer")
                .skills(List.of("java", "spring boot", "mysql"))
                .yearsOfExperience(5)
                .professionalSummary("Experienced developer")
                .resumeDataAvailable(true)
                .profileDataAvailable(true)
                .build();
    }

    @Test
    void careerInsights_deterministicFallback_returnsData() {
        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(aiMatchingService.generateCareerAdvice(any(), anyLong())).thenReturn(Optional.empty());

        CareerInsightsResponse response = careerInsightsService.getCareerInsights(candidate);

        assertThat(response).isNotNull();
        assertThat(response.getStrengths()).isNotEmpty();
        assertThat(response.getRecommendedSkills()).isNotEmpty();
        assertThat(response.getSuggestedJobCategories()).isNotEmpty();
        assertThat(response.getProfileImprovements()).isNotEmpty();
        assertThat(response.getResumeImprovements()).isNotEmpty();
        assertThat(response.getGeneralCareerSuggestions()).isNotEmpty();
        assertThat(response.isAiEnhanced()).isFalse();
    }

    @Test
    void careerInsights_aiAvailable_usesAi() {
        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(aiMatchingService.generateCareerAdvice(any(), eq(1L)))
                .thenReturn(Optional.of(AIMatchingService.CareerAdviceResult.builder()
                        .strengths(List.of("Strong Java skills"))
                        .recommendedSkills(List.of("Docker", "Kubernetes"))
                        .suggestedJobCategories(List.of("Backend Development"))
                        .profileImprovements(List.of("Add more details"))
                        .generalCareerSuggestions(List.of("Consider leadership roles"))
                        .build()));

        CareerInsightsResponse response = careerInsightsService.getCareerInsights(candidate);

        assertThat(response.isAiEnhanced()).isTrue();
        assertThat(response.getStrengths()).contains("Strong Java skills");
        assertThat(response.getRecommendedSkills()).contains("Docker", "Kubernetes");
        assertThat(response.getSuggestedJobCategories()).contains("Backend Development");
    }

    @Test
    void careerInsights_aiFails_fallsBackToDeterministic() {
        when(candidateDataResolver.resolve(candidate)).thenReturn(candidateData);
        when(aiMatchingService.generateCareerAdvice(any(), eq(1L)))
                .thenThrow(new RuntimeException("AI unavailable"));

        CareerInsightsResponse response = careerInsightsService.getCareerInsights(candidate);

        assertThat(response).isNotNull();
        assertThat(response.isAiEnhanced()).isFalse();
        assertThat(response.getStrengths()).isNotEmpty();
    }

    @Test
    void skillGap_service_returnsData() {
        SkillGapResponse expected = SkillGapResponse.builder()
                .jobId(10L)
                .jobTitle("Java Developer")
                .jobRequiredSkills(List.of("java", "spring boot", "docker"))
                .candidateSkills(List.of("java", "spring boot"))
                .matchedSkills(List.of("java", "spring boot"))
                .missingSkills(List.of("docker"))
                .partiallyMatchedSkills(List.of())
                .prioritySuggestions(List.of("Consider strengthening docker"))
                .explanation("You match 2 of 3 required skills")
                .aiEnhanced(false)
                .build();

        when(skillGapService.analyzeSkillGap(any(), any())).thenReturn(expected);

        SkillGapResponse response = skillGapService.analyzeSkillGap(candidate, null);

        assertThat(response.getJobId()).isEqualTo(10L);
        assertThat(response.getMatchedSkills()).containsExactly("java", "spring boot");
        assertThat(response.getMissingSkills()).containsExactly("docker");
        assertThat(response.isAiEnhanced()).isFalse();
    }
}
