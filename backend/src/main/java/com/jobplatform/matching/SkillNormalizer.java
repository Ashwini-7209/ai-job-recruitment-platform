package com.jobplatform.matching;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SkillNormalizer {

    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("spring boot", "spring boot");
        ALIASES.put("spring-framework", "spring boot");
        ALIASES.put("spring framework", "spring boot");
        ALIASES.put("springboot", "spring boot");

        ALIASES.put("javascript", "javascript");
        ALIASES.put("js", "javascript");

        ALIASES.put("typescript", "typescript");
        ALIASES.put("ts", "typescript");

        ALIASES.put("postgres", "postgresql");
        ALIASES.put("postgres db", "postgresql");

        ALIASES.put("k8s", "kubernetes");
        ALIASES.put("reactjs", "react");
        ALIASES.put("react.js", "react");

        ALIASES.put("nodejs", "node.js");
        ALIASES.put("node", "node.js");

        ALIASES.put("vuejs", "vue.js");
        ALIASES.put("vue", "vue.js");

        ALIASES.put("angularjs", "angular");
        ALIASES.put("angular 2+", "angular");

        ALIASES.put("c sharp", "c#");
        ALIASES.put("c#", "c#");

        ALIASES.put("golang", "go");

        ALIASES.put("python3", "python");
        ALIASES.put("python 3", "python");

        ALIASES.put("tf", "tensorflow");
        ALIASES.put("pytorch", "pytorch");
        ALIASES.put("py-torch", "pytorch");

        ALIASES.put("ci/cd", "ci/cd");
        ALIASES.put("cicd", "ci/cd");

        ALIASES.put("rest", "rest api");
        ALIASES.put("restful", "rest api");
        ALIASES.put("rest apis", "rest api");

        ALIASES.put("graphql", "graphql");

        ALIASES.put("microsoft azure", "azure");
        ALIASES.put("aws", "aws");
        ALIASES.put("amazon web services", "aws");

        ALIASES.put("gcp", "gcp");
        ALIASES.put("google cloud", "gcp");
    }

    private SkillNormalizer() {
    }

    public static String normalize(String skill) {
        if (skill == null || skill.isBlank()) {
            return "";
        }
        String trimmed = skill.strip().toLowerCase();
        return ALIASES.getOrDefault(trimmed, trimmed);
    }

    public static Set<String> normalizeAll(List<String> skills) {
        if (skills == null) {
            return Set.of();
        }
        return skills.stream()
                .map(SkillNormalizer::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public static Set<String> normalizeAll(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.isBlank()) {
            return Set.of();
        }
        return normalizeAll(Arrays.asList(commaSeparated.split(",")));
    }

    public static Set<String> findMatched(Set<String> candidateSkills, Set<String> jobSkills) {
        Set<String> matched = new java.util.HashSet<>(candidateSkills);
        matched.retainAll(jobSkills);
        return matched;
    }

    public static Set<String> findMissing(Set<String> candidateSkills, Set<String> jobSkills) {
        Set<String> missing = new java.util.HashSet<>(jobSkills);
        missing.removeAll(candidateSkills);
        return missing;
    }
}
