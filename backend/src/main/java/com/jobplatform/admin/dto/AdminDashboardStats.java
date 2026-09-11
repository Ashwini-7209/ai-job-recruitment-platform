package com.jobplatform.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {

    private Long totalUsers;
    private Long totalCandidates;
    private Long totalRecruiters;
    private Long activeUsers;
    private Long totalJobs;
    private Long publishedJobs;
    private Long totalApplications;
}
