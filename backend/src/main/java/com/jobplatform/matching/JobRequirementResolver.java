package com.jobplatform.matching;

import com.jobplatform.job.Job;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobRequirementResolver {

    public JobData resolve(Job job) {
        return JobData.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .requiredSkills(new ArrayList<>(SkillNormalizer.normalizeAll(job.getSkills())))
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .build();
    }
}
