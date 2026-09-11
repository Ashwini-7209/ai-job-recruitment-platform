package com.jobplatform.analytics;

import com.jobplatform.analytics.dto.RecruiterAnalyticsSummaryResponse;
import com.jobplatform.analytics.dto.RecruiterFunnelResponse;
import com.jobplatform.analytics.dto.RecruiterHiringAnalyticsResponse;
import com.jobplatform.analytics.dto.RecruiterJobAnalyticsResponse;
import com.jobplatform.analytics.dto.TrendResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/recruiters/me/analytics")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterAnalyticsController {

    private final RecruiterAnalyticsService recruiterAnalyticsService;

    public RecruiterAnalyticsController(RecruiterAnalyticsService recruiterAnalyticsService) {
        this.recruiterAnalyticsService = recruiterAnalyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<RecruiterAnalyticsSummaryResponse>> getSummary() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(recruiterAnalyticsService.getSummary(recruiter)));
    }

    @GetMapping("/funnel")
    public ResponseEntity<ApiResponse<RecruiterFunnelResponse>> getFunnel() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(recruiterAnalyticsService.getFunnel(recruiter)));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PagedResponse<RecruiterJobAnalyticsResponse>>> getJobAnalytics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(recruiterAnalyticsService.getJobAnalytics(recruiter, page, size)));
    }

    @GetMapping("/application-trend")
    public ResponseEntity<ApiResponse<TrendResponse>> getApplicationTrend(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(3);
        LocalDate toDate = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(recruiterAnalyticsService.getApplicationTrend(recruiter, fromDate, toDate)));
    }

    @GetMapping("/hiring-metrics")
    public ResponseEntity<ApiResponse<RecruiterHiringAnalyticsResponse>> getHiringMetrics() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(recruiterAnalyticsService.getHiringMetrics(recruiter)));
    }
}
