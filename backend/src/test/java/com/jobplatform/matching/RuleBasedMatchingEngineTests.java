package com.jobplatform.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedMatchingEngineTests {

    private RuleBasedMatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RuleBasedMatchingEngine();
    }

    @Test
    void calculate_perfectMatch() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java", "spring boot", "mysql", "docker"))
                .yearsOfExperience(5)
                .headline("Senior Java Developer")
                .bio("Experienced developer")
                .resumeDataAvailable(true)
                .profileDataAvailable(true)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "spring boot", "mysql", "docker"))
                .experienceMin(3)
                .experienceMax(7)
                .build();

        var result = engine.calculate(candidate, job);

        assertThat(result.getSkillScore()).isEqualTo(100);
        assertThat(result.getExperienceScore()).isEqualTo(100);
        assertThat(result.getMatchedSkills()).hasSize(4);
        assertThat(result.getMissingSkills()).isEmpty();
        assertThat(result.getOverallScore()).isGreaterThanOrEqualTo(70);
    }

    @Test
    void calculate_partialSkillOverlap() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java", "spring boot"))
                .yearsOfExperience(3)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "spring boot", "mysql", "docker"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        var result = engine.calculate(candidate, job);

        assertThat(result.getSkillScore()).isEqualTo(50);
        assertThat(result.getMatchedSkills()).containsExactlyInAnyOrder("java", "spring boot");
        assertThat(result.getMissingSkills()).containsExactlyInAnyOrder("mysql", "docker");
    }

    @Test
    void calculate_noSkillOverlap() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("python", "django"))
                .yearsOfExperience(3)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "spring boot"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        var result = engine.calculate(candidate, job);

        assertThat(result.getSkillScore()).isEqualTo(0);
        assertThat(result.getMatchedSkills()).isEmpty();
        assertThat(result.getMissingSkills()).containsExactlyInAnyOrder("java", "spring boot");
    }

    @Test
    void calculate_emptyCandidateSkills() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of())
                .yearsOfExperience(3)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "spring boot"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        var result = engine.calculate(candidate, job);

        assertThat(result.getSkillScore()).isEqualTo(0);
        assertThat(result.getMissingSkills()).hasSize(2);
    }

    @Test
    void calculate_emptyJobSkills() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java", "spring boot"))
                .yearsOfExperience(3)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of())
                .build();

        var result = engine.calculate(candidate, job);

        assertThat(result.getSkillScore()).isEqualTo(70);
        assertThat(result.getMatchedSkills()).isEmpty();
        assertThat(result.getMissingSkills()).isEmpty();
    }

    @Test
    void calculate_experienceWithinRange() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(4)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(2)
                .experienceMax(6)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isEqualTo(100);
    }

    @Test
    void calculate_experienceBelowMinimum() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(1)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(3)
                .experienceMax(6)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isLessThan(100);
        assertThat(result.getPotentialGaps()).isNotEmpty();
    }

    @Test
    void calculate_experienceAboveMaximum() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(10)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(2)
                .experienceMax(5)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isGreaterThanOrEqualTo(60);
    }

    @Test
    void calculate_missingCandidateExperience() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(null)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(3)
                .experienceMax(6)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isEqualTo(25);
    }

    @Test
    void calculate_missingJobExperienceRequirement() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(5)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(null)
                .experienceMax(null)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isEqualTo(70);
    }

    @Test
    void calculate_invalidExperienceValues() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(-1)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(null)
                .experienceMax(null)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getOverallScore()).isBetween(0, 100);
    }

    @Test
    void calculate_scoreAlwaysBetween0And100() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of())
                .yearsOfExperience(null)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "spring", "docker", "kubernetes", "terraform"))
                .experienceMin(10)
                .experienceMax(15)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getOverallScore()).isBetween(0, 100);
    }

    @Test
    void calculate_matchingReasonsPopulated() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java", "spring boot"))
                .yearsOfExperience(4)
                .profileDataAvailable(true)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "spring boot"))
                .experienceMin(3)
                .experienceMax(6)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getMatchingReasons()).isNotEmpty();
    }

    @Test
    void calculate_potentialGapsPopulated() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(1)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java", "docker"))
                .experienceMin(3)
                .experienceMax(6)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getPotentialGaps()).isNotEmpty();
    }

    @Test
    void calculate_bothSkillsAndExperienceNull() {
        CandidateData candidate = CandidateData.builder().build();
        JobData job = JobData.builder().build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getOverallScore()).isBetween(0, 100);
    }

    @Test
    void calculate_experienceDeficitOneYear() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(2)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(3)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isEqualTo(70);
    }

    @Test
    void calculate_experienceDeficitTwoYears() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(1)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(3)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isEqualTo(50);
    }

    @Test
    void calculate_experienceOnlyMaxSpecified() {
        CandidateData candidate = CandidateData.builder()
                .skills(List.of("java"))
                .yearsOfExperience(4)
                .build();

        JobData job = JobData.builder()
                .requiredSkills(List.of("java"))
                .experienceMin(null)
                .experienceMax(5)
                .build();

        var result = engine.calculate(candidate, job);
        assertThat(result.getExperienceScore()).isEqualTo(90);
    }
}
