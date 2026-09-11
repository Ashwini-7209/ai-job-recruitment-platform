package com.jobplatform.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationNoteResponse {

    private Long id;
    private String note;
    private String recruiterName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
