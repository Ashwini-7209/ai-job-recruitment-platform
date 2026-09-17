package com.jobplatform.notification;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.notification.dto.NotificationResponse;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = CurrentUserUtil.getCurrentUser();
        PagedResponse<NotificationResponse> notifications = notificationService.getNotifications(user, page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        User user = CurrentUserUtil.getCurrentUser();
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        User user = CurrentUserUtil.getCurrentUser();
        notificationService.markAsRead(user, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User user = CurrentUserUtil.getCurrentUser();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
