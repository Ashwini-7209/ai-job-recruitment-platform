package com.jobplatform.matching;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SkillNormalizerTests {

    @Test
    void normalize_exactMatch() {
        assertThat(SkillNormalizer.normalize("java")).isEqualTo("java");
    }

    @Test
    void normalize_caseInsensitive() {
        assertThat(SkillNormalizer.normalize("JAVA")).isEqualTo("java");
        assertThat(SkillNormalizer.normalize("Java")).isEqualTo("java");
        assertThat(SkillNormalizer.normalize("jAvA")).isEqualTo("java");
    }

    @Test
    void normalize_springVariations() {
        assertThat(SkillNormalizer.normalize("Spring Boot")).isEqualTo("spring boot");
        assertThat(SkillNormalizer.normalize("spring boot")).isEqualTo("spring boot");
        assertThat(SkillNormalizer.normalize("spring-boot")).isEqualTo("spring-boot");
        assertThat(SkillNormalizer.normalize("spring-framework")).isEqualTo("spring boot");
        assertThat(SkillNormalizer.normalize("Spring Framework")).isEqualTo("spring boot");
        assertThat(SkillNormalizer.normalize("springboot")).isEqualTo("spring boot");
    }

    @Test
    void normalize_javascriptVariations() {
        assertThat(SkillNormalizer.normalize("JavaScript")).isEqualTo("javascript");
        assertThat(SkillNormalizer.normalize("js")).isEqualTo("javascript");
    }

    @Test
    void normalize_typescriptVariations() {
        assertThat(SkillNormalizer.normalize("TypeScript")).isEqualTo("typescript");
        assertThat(SkillNormalizer.normalize("ts")).isEqualTo("typescript");
    }

    @Test
    void normalize_emptyOrNull() {
        assertThat(SkillNormalizer.normalize(null)).isEmpty();
        assertThat(SkillNormalizer.normalize("")).isEmpty();
        assertThat(SkillNormalizer.normalize("   ")).isEmpty();
    }

    @Test
    void normalizeAll_commaSeparated() {
        Set<String> result = SkillNormalizer.normalizeAll("Java, Spring Boot, MySQL");
        assertThat(result).containsExactlyInAnyOrder("java", "spring boot", "mysql");
    }

    @Test
    void normalizeAll_list() {
        Set<String> result = SkillNormalizer.normalizeAll(List.of("Java", "PYTHON", "Go"));
        assertThat(result).containsExactlyInAnyOrder("java", "python", "go");
    }

    @Test
    void normalizeAll_nullInput() {
        assertThat(SkillNormalizer.normalizeAll((String) null)).isEmpty();
        assertThat(SkillNormalizer.normalizeAll((List<String>) null)).isEmpty();
    }

    @Test
    void normalizeAll_blankValuesFiltered() {
        Set<String> result = SkillNormalizer.normalizeAll("Java,, ,Python");
        assertThat(result).containsExactlyInAnyOrder("java", "python");
    }

    @Test
    void findMatched_exactOverlap() {
        Set<String> candidate = Set.of("java", "spring boot", "mysql");
        Set<String> job = Set.of("java", "spring boot", "docker");
        Set<String> matched = SkillNormalizer.findMatched(candidate, job);
        assertThat(matched).containsExactlyInAnyOrder("java", "spring boot");
    }

    @Test
    void findMissing_skills() {
        Set<String> candidate = Set.of("java", "spring boot", "mysql");
        Set<String> job = Set.of("java", "spring boot", "docker");
        Set<String> missing = SkillNormalizer.findMissing(candidate, job);
        assertThat(missing).containsExactlyInAnyOrder("docker");
    }

    @Test
    void findMatched_noOverlap() {
        Set<String> candidate = Set.of("python", "django");
        Set<String> job = Set.of("java", "spring boot");
        Set<String> matched = SkillNormalizer.findMatched(candidate, job);
        assertThat(matched).isEmpty();
    }

    @Test
    void findMissing_emptyJobSkills() {
        Set<String> candidate = Set.of("java", "spring boot");
        Set<String> job = Set.of();
        Set<String> missing = SkillNormalizer.findMissing(candidate, job);
        assertThat(missing).isEmpty();
    }

    @Test
    void findMissing_emptyCandidateSkills() {
        Set<String> candidate = Set.of();
        Set<String> job = Set.of("java", "spring boot");
        Set<String> missing = SkillNormalizer.findMissing(candidate, job);
        assertThat(missing).containsExactlyInAnyOrder("java", "spring boot");
    }

    @Test
    void normalizeAll_normalizationConsistency() {
        Set<String> result = SkillNormalizer.normalizeAll("JAVA,java,Java,JS,JavaScript");
        assertThat(result).containsExactlyInAnyOrder("java", "javascript");
    }
}
