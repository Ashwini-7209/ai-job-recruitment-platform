package com.jobplatform.interview.dto;

import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.interview.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterInterviewResponse {

    private Long interviewId;
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long jobId;
    private String jobTitle;
    private String title;
    private InterviewType interviewType;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private String location;
    private String meetingLink;
    private String interviewerName;
    private String interviewerNotes;
    private String candidateNotes;
    private InterviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
