package com.jobplatform.jobalert.dto;

import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.WorkplaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobAlertRequest {

    @NotBlank(message = "Alert name is required")
    @Size(max = 100, message = "Alert name must not exceed 100 characters")
    private String name;

    @Size(max = 200, message = "Keywords must not exceed 200 characters")
    private String keywords;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    private WorkplaceType workplaceType;

    private EmploymentType employmentType;

    private Integer minimumExperience;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;

    private Boolean active;
}
