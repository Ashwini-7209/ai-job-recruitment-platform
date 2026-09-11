package com.jobplatform.jobalert.dto;

import com.jobplatform.job.enums.EmploymentType;
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
public class JobAlertResponse {

    private Long id;
    private String name;
    private String keywords;
    private String location;
    private WorkplaceType workplaceType;
    private EmploymentType employmentType;
    private Integer minimumExperience;
    private String skills;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTriggeredAt;
}
