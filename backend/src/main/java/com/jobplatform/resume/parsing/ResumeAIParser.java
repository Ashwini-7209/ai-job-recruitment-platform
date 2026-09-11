package com.jobplatform.resume.parsing;

import java.util.Optional;

public interface ResumeAIParser {

    Optional<StructuredResumeData> parseResume(String resumeText);

    String getProviderName();
}
