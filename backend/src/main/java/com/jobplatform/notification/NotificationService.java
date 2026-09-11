package com.jobplatform.notification;

import com.jobplatform.common.PagedResponse;
import com.jobplatform.notification.dto.NotificationResponse;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import com.jobplatform.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService notificationPreferenceService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPreferenceService notificationPreferenceService) {
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @Transactional
    public Notification createNotification(User user, NotificationType type, String title, String message,
                                           Long entityId, String entityType) {
        if (!notificationPreferenceService.isNotificationEnabled(user, type)) {
            log.debug("Notification skipped (disabled by preference): type={} user={}", type, user.getEmail());
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .entityId(entityId)
                .entityType(entityType)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.debug("Notification created: type={} user={}", type, user.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public boolean existsNotificationForJobAndAlert(User user, Long jobId, Long alertId) {
        return notificationRepository.existsByUserAndEntityIdAndType(user, jobId, NotificationType.JOB_ALERT_MATCH);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(User user, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return PagedResponse.<NotificationResponse>builder()
                .content(notifications.getContent().stream().map(this::mapToResponse).toList())
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .last(notifications.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(User user, Long notificationId) {
        int updated = notificationRepository.markAsReadByIdAndUser(notificationId, user);
        if (updated == 0) {
            throw new BadRequestException("Notification not found or already read");
        }
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .entityId(notification.getEntityId())
                .entityType(notification.getEntityType())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
