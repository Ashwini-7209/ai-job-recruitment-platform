package com.jobplatform.resume.parsing;

import com.jobplatform.resume.enums.ResumeParsingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {

    private ResumeParsingStatus status;
    private String extractedText;
    private String failureReason;
}
