package com.jobplatform.interview.dto;

import com.jobplatform.interview.enums.InterviewType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInterviewRequest {

    @Size(max = 200)
    private String title;

    private InterviewType interviewType;

    @Future(message = "Interview must be scheduled for a future time")
    private LocalDateTime scheduledStart;

    private LocalDateTime scheduledEnd;

    @Size(max = 255)
    private String location;

    @Size(max = 500)
    private String meetingLink;

    @Size(max = 100)
    private String interviewerName;

    @Size(max = 5000)
    private String interviewerNotes;

    @Size(max = 5000)
    private String candidateNotes;
}
