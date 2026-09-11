package com.jobplatform.notification;

import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.notification.dto.NotificationPreferenceResponse;
import com.jobplatform.notification.dto.UpdateNotificationPreferenceRequest;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(NotificationPreferenceService.class);

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceService(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(User user) {
        NotificationPreference preference = getOrCreatePreferences(user);
        return mapToResponse(preference);
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(User user, UpdateNotificationPreferenceRequest request) {
        NotificationPreference preference = getOrCreatePreferences(user);

        if (request.getApplicationStatus() != null) {
            preference.setApplicationStatus(request.getApplicationStatus());
        }
        if (request.getInterviewUpdates() != null) {
            preference.setInterviewUpdates(request.getInterviewUpdates());
        }
        if (request.getJobAlerts() != null) {
            preference.setJobAlerts(request.getJobAlerts());
        }
        if (request.getSystemNotifications() != null) {
            preference.setSystemNotifications(request.getSystemNotifications());
        }

        NotificationPreference updated = notificationPreferenceRepository.save(preference);
        log.info("Notification preferences updated: user={}", user.getEmail());
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(User user, NotificationType type) {
        NotificationPreference preference = notificationPreferenceRepository.findByUser(user).orElse(null);
        if (preference == null) {
            return true;
        }

        return switch (type) {
            case APPLICATION_RECEIVED, APPLICATION_STATUS_CHANGED -> preference.getApplicationStatus();
            case INTERVIEW_SCHEDULED, INTERVIEW_RESCHEDULED, INTERVIEW_CANCELLED -> preference.getInterviewUpdates();
            case JOB_ALERT_MATCH -> preference.getJobAlerts();
            case GENERAL, JOB_RECOMMENDATION -> preference.getSystemNotifications();
        };
    }

    private NotificationPreference getOrCreatePreferences(User user) {
        return notificationPreferenceRepository.findByUser(user)
                .orElseGet(() -> {
                    NotificationPreference newPreference = NotificationPreference.builder()
                            .user(user)
                            .applicationStatus(true)
                            .interviewUpdates(true)
                            .jobAlerts(true)
                            .systemNotifications(true)
                            .build();
                    return notificationPreferenceRepository.save(newPreference);
                });
    }

    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .applicationStatus(preference.getApplicationStatus())
                .interviewUpdates(preference.getInterviewUpdates())
                .jobAlerts(preference.getJobAlerts())
                .systemNotifications(preference.getSystemNotifications())
                .build();
    }
}
