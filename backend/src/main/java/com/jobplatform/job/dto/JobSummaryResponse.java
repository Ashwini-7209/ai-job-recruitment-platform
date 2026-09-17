package com.jobplatform.job.dto;

import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryResponse {

    private Long id;
    private Long jobId;
    private String title;
    private String recruiterName;
    private String companyName;
    private String location;
    private EmploymentType employmentType;
    private WorkplaceType workplaceType;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private String skills;
    private JobStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
}
