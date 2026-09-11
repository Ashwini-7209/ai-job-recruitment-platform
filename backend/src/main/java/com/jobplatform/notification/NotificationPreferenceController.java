package com.jobplatform.notification;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.notification.dto.NotificationPreferenceResponse;
import com.jobplatform.notification.dto.UpdateNotificationPreferenceRequest;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/preferences")
@PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    public NotificationPreferenceController(NotificationPreferenceService notificationPreferenceService) {
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences() {
        User user = CurrentUserUtil.getCurrentUser();
        NotificationPreferenceResponse response = notificationPreferenceService.getPreferences(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        User user = CurrentUserUtil.getCurrentUser();
        NotificationPreferenceResponse response = notificationPreferenceService.updatePreferences(user, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", response));
    }
}
