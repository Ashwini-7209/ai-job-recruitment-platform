package com.jobplatform.health;

import com.jobplatform.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatus>> health() {
        return ResponseEntity.ok(ApiResponse.success(new HealthStatus("UP")));
    }

}
