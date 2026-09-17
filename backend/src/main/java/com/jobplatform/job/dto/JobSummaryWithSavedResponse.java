package com.jobplatform.job.dto;

import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.WorkplaceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobSummaryWithSavedResponse extends JobSummaryResponse {

    private String description;
    private boolean saved;
    private boolean applied;

    public static JobSummaryWithSavedResponse from(JobSummaryResponse base) {
        JobSummaryWithSavedResponse r = new JobSummaryWithSavedResponse();
        r.setId(base.getId());
        r.setJobId(base.getJobId());
        r.setTitle(base.getTitle());
        r.setRecruiterName(base.getRecruiterName());
        r.setCompanyName(base.getCompanyName());
        r.setLocation(base.getLocation());
        r.setEmploymentType(base.getEmploymentType());
        r.setWorkplaceType(base.getWorkplaceType());
        r.setExperienceMin(base.getExperienceMin());
        r.setExperienceMax(base.getExperienceMax());
        r.setSalaryMin(base.getSalaryMin());
        r.setSalaryMax(base.getSalaryMax());
        r.setSkills(base.getSkills());
        r.setStatus(base.getStatus());
        r.setPublishedAt(base.getPublishedAt());
        r.setApplicationDeadline(base.getApplicationDeadline());
        r.setCreatedAt(base.getCreatedAt());
        r.setSaved(false);
        r.setApplied(false);
        return r;
    }
}
