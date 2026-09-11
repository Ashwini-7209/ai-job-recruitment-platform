package com.jobplatform.analytics;

import com.jobplatform.analytics.dto.AdminAnalyticsSummaryResponse;
import com.jobplatform.analytics.dto.AdminApplicationAnalyticsResponse;
import com.jobplatform.analytics.dto.AdminInterviewAnalyticsResponse;
import com.jobplatform.analytics.dto.AdminJobAnalyticsResponse;
import com.jobplatform.analytics.dto.TrendResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminAnalyticsSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getSummary()));
    }

    @GetMapping("/user-growth")
    public ResponseEntity<ApiResponse<TrendResponse>> getUserGrowth(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) UserRole roleFilter) {
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(6);
        LocalDate toDate = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getUserGrowth(fromDate, toDate, roleFilter)));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<AdminJobAnalyticsResponse>> getJobAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getJobAnalytics()));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<AdminApplicationAnalyticsResponse>> getApplicationAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getApplicationAnalytics()));
    }

    @GetMapping("/interviews")
    public ResponseEntity<ApiResponse<AdminInterviewAnalyticsResponse>> getInterviewAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getInterviewAnalytics()));
    }

    @GetMapping("/application-trend")
    public ResponseEntity<ApiResponse<TrendResponse>> getApplicationTrend(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(6);
        LocalDate toDate = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getApplicationTrend(fromDate, toDate)));
    }
}
