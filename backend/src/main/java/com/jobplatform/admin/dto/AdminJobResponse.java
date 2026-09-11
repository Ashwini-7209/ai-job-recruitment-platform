package com.jobplatform.admin.dto;

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
public class AdminJobResponse {

    private Long id;
    private String title;
    private String recruiterName;
    private String recruiterEmail;
    private String location;
    private WorkplaceType workplaceType;
    private EmploymentType employmentType;
    private JobStatus status;
    private Integer experienceMin;
    private Integer experienceMax;
    private Long applicationCount;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime applicationDeadline;
}
