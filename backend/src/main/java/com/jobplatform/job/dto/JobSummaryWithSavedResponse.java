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

    private boolean saved;
    private boolean applied;

    public static JobSummaryWithSavedResponse from(JobSummaryResponse base) {
        JobSummaryWithSavedResponse r = new JobSummaryWithSavedResponse();
        r.setId(base.getId());
        r.setTitle(base.getTitle());
        r.setLocation(base.getLocation());
        r.setEmploymentType(base.getEmploymentType());
        r.setWorkplaceType(base.getWorkplaceType());
        r.setSalaryMin(base.getSalaryMin());
        r.setSalaryMax(base.getSalaryMax());
        r.setSkills(base.getSkills());
        r.setApplicationDeadline(base.getApplicationDeadline());
        r.setCreatedAt(base.getCreatedAt());
        r.setSaved(false);
        r.setApplied(false);
        return r;
    }
}
