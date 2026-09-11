package com.jobplatform.recruiter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterProfileResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String jobTitle;
    private String department;
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private String companyLocation;
    private String linkedinUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
