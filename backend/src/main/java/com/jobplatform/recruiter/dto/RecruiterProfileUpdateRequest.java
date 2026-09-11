package com.jobplatform.recruiter.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterProfileUpdateRequest {

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    @Size(max = 255, message = "Company website must not exceed 255 characters")
    private String companyWebsite;

    @Size(max = 2000, message = "Company description must not exceed 2000 characters")
    private String companyDescription;

    @Size(max = 100, message = "Company location must not exceed 100 characters")
    private String companyLocation;

    @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
    private String linkedinUrl;
}
