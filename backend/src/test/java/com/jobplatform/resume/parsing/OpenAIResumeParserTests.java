package com.jobplatform.resume.parsing;

import com.jobplatform.ai.ChatAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAIResumeParserTests {

    private ChatAIProvider chatAIProvider;
    private OpenAIResumeParser parser;

    @BeforeEach
    void setUp() {
        chatAIProvider = mock(ChatAIProvider.class);
        when(chatAIProvider.getProviderName()).thenReturn("openai");
        when(chatAIProvider.isAvailable()).thenReturn(true);
        parser = new OpenAIResumeParser(chatAIProvider);
    }

    @Test
    void getProviderName_returnsOpenai() {
        assertThat(parser.getProviderName()).isEqualTo("openai");
    }

    @Test
    void parseResume_validJson_returnsStructuredData() {
        String mockJson = """
                {"personalInfo":{"fullName":"John Doe","email":"john@example.com","phone":"555-1234","location":"New York"},"professionalSummary":"Experienced developer","headline":"Senior Java Developer","skills":["Java","Spring Boot","React"],"experience":[{"company":"TechCorp","jobTitle":"Senior Developer","location":"New York","startDate":"Jan 2020","endDate":"Present","current":true,"description":"Led team of 5","technologies":["Java","Spring"]}],"education":[{"institution":"MIT","degree":"BS","fieldOfStudy":"Computer Science","startDate":"2014","endDate":"2018","grade":"3.8"}],"certifications":[],"projects":[],"languages":["English","Spanish"],"totalYearsOfExperience":8}
                """;
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of(mockJson));

        Optional<StructuredResumeData> result = parser.parseResume("Sample resume text");

        assertThat(result).isPresent();
        StructuredResumeData data = result.get();
        assertThat(data.getPersonalInfo()).isNotNull();
        assertThat(data.getPersonalInfo().getFullName()).isEqualTo("John Doe");
        assertThat(data.getPersonalInfo().getEmail()).isEqualTo("john@example.com");
        assertThat(data.getHeadline()).isEqualTo("Senior Java Developer");
        assertThat(data.getSkills()).containsExactly("Java", "Spring Boot", "React");
        assertThat(data.getExperience()).hasSize(1);
        assertThat(data.getExperience().get(0).getCompany()).isEqualTo("TechCorp");
        assertThat(data.getEducation()).hasSize(1);
        assertThat(data.getEducation().get(0).getInstitution()).isEqualTo("MIT");
        assertThat(data.getLanguages()).containsExactly("English", "Spanish");
        assertThat(data.getTotalYearsOfExperience()).isEqualTo(8);
    }

    @Test
    void parseResume_malformedJson_returnsEmpty() {
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of("not valid json"));
        Optional<StructuredResumeData> result = parser.parseResume("Sample resume text");
        assertThat(result).isEmpty();
    }

    @Test
    void parseResume_missingFields_handlesGracefully() {
        String mockJson = """
                {"personalInfo":{},"skills":[],"experience":[],"education":[],"certifications":[],"projects":[],"languages":[]}
                """;
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of(mockJson));

        Optional<StructuredResumeData> result = parser.parseResume("Sample resume text");

        assertThat(result).isPresent();
        StructuredResumeData data = result.get();
        assertThat(data.getPersonalInfo()).isNotNull();
        assertThat(data.getPersonalInfo().getFullName()).isNull();
        assertThat(data.getSkills()).isEmpty();
    }

    @Test
    void parseResume_nullValues_handledCorrectly() {
        String mockJson = """
                {"personalInfo":{"fullName":null,"email":null},"skills":null,"experience":null,"education":null,"certifications":null,"projects":null,"languages":null,"totalYearsOfExperience":null}
                """;
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of(mockJson));

        Optional<StructuredResumeData> result = parser.parseResume("Sample resume text");

        assertThat(result).isPresent();
        StructuredResumeData data = result.get();
        assertThat(data.getSkills()).isEmpty();
        assertThat(data.getExperience()).isEmpty();
        assertThat(data.getTotalYearsOfExperience()).isNull();
    }

    @Test
    void parseResume_jsonWithMarkdownCodeBlock_stripsCodeBlock() {
        String mockJson = "```json\n{\"personalInfo\":{\"fullName\":\"Jane\"},\"skills\":[\"Python\"],\"experience\":[],\"education\":[],\"certifications\":[],\"projects\":[],\"languages\":[]}\n```";
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.of(mockJson));

        Optional<StructuredResumeData> result = parser.parseResume("Sample resume text");

        assertThat(result).isPresent();
        assertThat(result.get().getPersonalInfo().getFullName()).isEqualTo("Jane");
    }

    @Test
    void parseResume_providerUnavailable_returnsEmpty() {
        when(chatAIProvider.isAvailable()).thenReturn(false);
        OpenAIResumeParser unavailableParser = new OpenAIResumeParser(chatAIProvider);
        Optional<StructuredResumeData> result = unavailableParser.parseResume("Sample resume text");
        assertThat(result).isEmpty();
    }

    @Test
    void parseResume_providerReturnsEmpty_returnsEmpty() {
        when(chatAIProvider.chat(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());
        Optional<StructuredResumeData> result = parser.parseResume("Sample resume text");
        assertThat(result).isEmpty();
    }
}
