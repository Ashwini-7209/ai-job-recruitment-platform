package com.jobplatform.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
