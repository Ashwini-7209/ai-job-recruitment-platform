package com.jobplatform.matching;

import com.jobplatform.ai.ChatAIProvider;
import com.jobplatform.ai.AIUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIMatchingServiceTests {

    @Mock
    private ChatAIProvider chatAIProvider;

    @Mock
    private AIUsageService usageService;

    private AIMatchingService aiMatchingService;
    private CandidateData candidateData;
    private JobData jobData;

    @BeforeEach
    void setUp() {
        aiMatchingService = new AIMatchingService(chatAIProvider, usageService);

        candidateData = CandidateData.builder()
                .candidateId(1L)
                .headline("Java Developer")
                .bio("Experienced")
                .skills(List.of("java", "spring boot"))
                .yearsOfExperience(3)
                .build();

        jobData = JobData.builder()
                .jobId(10L)
                .title("Senior Java Developer")
                .description("We need a Java developer")
                .requiredSkills(List.of("java", "spring boot", "docker"))
                .experienceMin(2)
                .experienceMax(5)
                .build();
    }

    @Test
    void evaluateMatch_providerAvailable_returnsResult() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("""
                        {"relevanceScore": 75, "matchingReasons": ["Strong Java match"], "potentialGaps": ["Docker missing"], "evidence": ["3 years Java experience"]}
                        """));

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getRelevanceScore()).isEqualTo(75);
    }

    @Test
    void evaluateMatch_providerUnavailable_returnsEmpty() {
        when(chatAIProvider.isAvailable()).thenReturn(false);

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluateMatch_rateLimited_returnsEmpty() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(false);

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluateMatch_providerTimeout_returnsEmpty() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluateMatch_malformedJson_returnsEmpty() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("not valid json at all"));

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void evaluateMatch_scoreBoundary_low() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("""
                        {"relevanceScore": -10, "matchingReasons": [], "potentialGaps": [], "evidence": []}
                        """));

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getRelevanceScore()).isBetween(0, 100);
    }

    @Test
    void evaluateMatch_scoreBoundary_high() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("""
                        {"relevanceScore": 150, "matchingReasons": [], "potentialGaps": [], "evidence": []}
                        """));

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getRelevanceScore()).isBetween(0, 100);
    }

    @Test
    void evaluateMatch_codeBlockStripped() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(usageService.canMakeRequest(1L)).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("```json\n{\"relevanceScore\": 80, \"matchingReasons\": [], \"potentialGaps\": [], \"evidence\": []}\n```"));

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getRelevanceScore()).isEqualTo(80);
    }

    @Test
    void evaluateMatch_nullUserId_works() {
        when(chatAIProvider.isAvailable()).thenReturn(true);
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("""
                        {"relevanceScore": 60, "matchingReasons": [], "potentialGaps": [], "evidence": []}
                        """));

        Optional<AIMatchingService.AISemanticResult> result =
                aiMatchingService.evaluateMatch(candidateData, jobData, null);

        assertThat(result).isPresent();
    }
}
