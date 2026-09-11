package com.jobplatform.analytics;

import com.jobplatform.analytics.dto.CandidateAnalyticsSummaryResponse;
import com.jobplatform.analytics.dto.CandidateApplicationStatusResponse;
import com.jobplatform.analytics.dto.TrendResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/candidates/me/analytics")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateAnalyticsController {

    private final CandidateAnalyticsService candidateAnalyticsService;

    public CandidateAnalyticsController(CandidateAnalyticsService candidateAnalyticsService) {
        this.candidateAnalyticsService = candidateAnalyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CandidateAnalyticsSummaryResponse>> getSummary() {
        User candidate = CurrentUserUtil.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(candidateAnalyticsService.getSummary(candidate)));
    }

    @GetMapping("/application-status")
    public ResponseEntity<ApiResponse<CandidateApplicationStatusResponse>> getApplicationStatusDistribution() {
        User candidate = CurrentUserUtil.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(candidateAnalyticsService.getApplicationStatusDistribution(candidate)));
    }

    @GetMapping("/application-trend")
    public ResponseEntity<ApiResponse<TrendResponse>> getApplicationTrend(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        User candidate = CurrentUserUtil.getCurrentUser();
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(3);
        LocalDate toDate = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(candidateAnalyticsService.getApplicationTrend(candidate, fromDate, toDate)));
    }
}
