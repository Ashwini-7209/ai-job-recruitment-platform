package com.jobplatform.resume.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.ai.ChatAIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OpenAIResumeParser implements ResumeAIParser {

    private static final Logger log = LoggerFactory.getLogger(OpenAIResumeParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatAIProvider chatAIProvider;

    public OpenAIResumeParser(ChatAIProvider chatAIProvider) {
        this.chatAIProvider = chatAIProvider;
    }

    @Override
    public Optional<StructuredResumeData> parseResume(String resumeText) {
        if (!chatAIProvider.isAvailable()) {
            log.warn("AI provider not available for resume parsing");
            return Optional.empty();
        }

        String prompt = buildPrompt(resumeText);

        Optional<String> responseOpt = chatAIProvider.chat(
                "You are an expert resume parser.", prompt, 4000);

        if (responseOpt.isEmpty()) {
            return Optional.empty();
        }

        return parseResponse(responseOpt.get());
    }

    @Override
    public String getProviderName() {
        return chatAIProvider.getProviderName();
    }

    private String buildPrompt(String resumeText) {
        return """
                You are an expert resume parser. Extract structured information from the following resume text.

                IMPORTANT RULES:
                - Extract ONLY information that is explicitly present in the resume
                - Do NOT invent or hallucinate any information
                - Use null or empty values when information is not available
                - Normalize skills to consistent casing
                - Return ONLY valid JSON, no other text

                Return a JSON object with this exact structure:
                {
                  "personalInfo": {
                    "fullName": "string or null",
                    "email": "string or null",
                    "phone": "string or null",
                    "location": "string or null"
                  },
                  "professionalSummary": "string or null",
                  "headline": "string or null",
                  "skills": ["skill1", "skill2"],
                  "experience": [
                    {
                      "company": "string or null",
                      "jobTitle": "string or null",
                      "location": "string or null",
                      "startDate": "string or null",
                      "endDate": "string or null",
                      "current": false,
                      "description": "string or null",
                      "technologies": ["tech1", "tech2"]
                    }
                  ],
                  "education": [
                    {
                      "institution": "string or null",
                      "degree": "string or null",
                      "fieldOfStudy": "string or null",
                      "startDate": "string or null",
                      "endDate": "string or null",
                      "grade": "string or null"
                    }
                  ],
                  "certifications": [
                    {
                      "name": "string or null",
                      "issuingOrganization": "string or null",
                      "issueDate": "string or null",
                      "expirationDate": "string or null"
                    }
                  ],
                  "projects": [
                    {
                      "name": "string or null",
                      "description": "string or null",
                      "technologies": ["tech1"],
                      "url": "string or null"
                    }
                  ],
                  "languages": ["language1"],
                  "totalYearsOfExperience": null
                }

                Resume text:
                ---
                %s
                ---
                """.formatted(resumeText);
    }

    private Optional<StructuredResumeData> parseResponse(String response) {
        try {
            String content = stripMarkdownFences(response);
            JsonNode data = objectMapper.readTree(content);

            StructuredResumeData result = StructuredResumeData.builder()
                    .personalInfo(parsePersonalInfo(data.path("personalInfo")))
                    .professionalSummary(getNullOrString(data, "professionalSummary"))
                    .headline(getNullOrString(data, "headline"))
                    .skills(parseStringList(data.path("skills")))
                    .experience(parseExperienceList(data.path("experience")))
                    .education(parseEducationList(data.path("education")))
                    .certifications(parseCertificationList(data.path("certifications")))
                    .projects(parseProjectList(data.path("projects")))
                    .languages(parseStringList(data.path("languages")))
                    .totalYearsOfExperience(data.path("totalYearsOfExperience").isNull() ?
                            null : data.path("totalYearsOfExperience").asInt())
                    .build();

            return Optional.of(result);
        } catch (Exception e) {
            log.error("Failed to parse AI resume response", e);
            return Optional.empty();
        }
    }

    private String stripMarkdownFences(String text) {
        String stripped = text.strip();
        if (stripped.startsWith("```json")) {
            stripped = stripped.substring(7);
        }
        if (stripped.endsWith("```")) {
            stripped = stripped.substring(0, stripped.length() - 3);
        }
        return stripped.strip();
    }

    private StructuredResumeData.PersonalInfo parsePersonalInfo(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return StructuredResumeData.PersonalInfo.builder()
                .fullName(getNullOrString(node, "fullName"))
                .email(getNullOrString(node, "email"))
                .phone(getNullOrString(node, "phone"))
                .location(getNullOrString(node, "location"))
                .build();
    }

    private List<StructuredResumeData.ExperienceData> parseExperienceList(JsonNode node) {
        List<StructuredResumeData.ExperienceData> list = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            list.add(StructuredResumeData.ExperienceData.builder()
                    .company(getNullOrString(item, "company"))
                    .jobTitle(getNullOrString(item, "jobTitle"))
                    .location(getNullOrString(item, "location"))
                    .startDate(getNullOrString(item, "startDate"))
                    .endDate(getNullOrString(item, "endDate"))
                    .current(item.path("current").asBoolean(false))
                    .description(getNullOrString(item, "description"))
                    .technologies(parseStringList(item.path("technologies")))
                    .build());
        }
        return list;
    }

    private List<StructuredResumeData.EducationData> parseEducationList(JsonNode node) {
        List<StructuredResumeData.EducationData> list = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            list.add(StructuredResumeData.EducationData.builder()
                    .institution(getNullOrString(item, "institution"))
                    .degree(getNullOrString(item, "degree"))
                    .fieldOfStudy(getNullOrString(item, "fieldOfStudy"))
                    .startDate(getNullOrString(item, "startDate"))
                    .endDate(getNullOrString(item, "endDate"))
                    .grade(getNullOrString(item, "grade"))
                    .build());
        }
        return list;
    }

    private List<StructuredResumeData.CertificationData> parseCertificationList(JsonNode node) {
        List<StructuredResumeData.CertificationData> list = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            list.add(StructuredResumeData.CertificationData.builder()
                    .name(getNullOrString(item, "name"))
                    .issuingOrganization(getNullOrString(item, "issuingOrganization"))
                    .issueDate(getNullOrString(item, "issueDate"))
                    .expirationDate(getNullOrString(item, "expirationDate"))
                    .build());
        }
        return list;
    }

    private List<StructuredResumeData.ProjectData> parseProjectList(JsonNode node) {
        List<StructuredResumeData.ProjectData> list = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            list.add(StructuredResumeData.ProjectData.builder()
                    .name(getNullOrString(item, "name"))
                    .description(getNullOrString(item, "description"))
                    .technologies(parseStringList(item.path("technologies")))
                    .url(getNullOrString(item, "url"))
                    .build());
        }
        return list;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            if (!item.isNull() && !item.isMissingNode()) {
                String val = item.asText("").strip();
                if (!val.isEmpty()) {
                    list.add(val);
                }
            }
        }
        return list;
    }

    private String getNullOrString(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asText("").strip();
        return text.isEmpty() ? null : text;
    }
}
