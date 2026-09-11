package com.jobplatform.savedjob.dto;

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
public class SavedJobResponse {

    private Long savedJobId;
    private Long jobId;
    private String jobTitle;
    private String location;
    private EmploymentType employmentType;
    private WorkplaceType workplaceType;
    private Integer salaryMin;
    private Integer salaryMax;
    private String skills;
    private JobStatus jobStatus;
    private LocalDateTime deadline;
    private LocalDateTime savedAt;
    private boolean applied;
}
