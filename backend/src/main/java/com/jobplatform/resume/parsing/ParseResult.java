package com.jobplatform.resume.parsing;

import com.jobplatform.resume.enums.ResumeParsingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseResult {

    private Long resumeId;
    private ResumeParsingStatus parsingStatus;
    private LocalDateTime parsedAt;
    private String failureReason;

    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String professionalSummary;
    private String headline;
    private String skills;
    private Integer totalYearsOfExperience;
    private String languages;
}
