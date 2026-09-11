package com.jobplatform.matching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobData {

    private Long jobId;
    private String title;
    private String description;
    private String location;

    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();

    private Integer experienceMin;
    private Integer experienceMax;
}
