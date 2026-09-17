package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CareerDashboardResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates/me/career")
@PreAuthorize("hasRole('CANDIDATE')")
public class CareerDashboardController {

    private final CareerDashboardService careerDashboardService;

    public CareerDashboardController(CareerDashboardService careerDashboardService) {
        this.careerDashboardService = careerDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<CareerDashboardResponse>> getCareerDashboard() {
        User user = CurrentUserUtil.getCurrentUser();
        CareerDashboardResponse response = careerDashboardService.getCareerDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
