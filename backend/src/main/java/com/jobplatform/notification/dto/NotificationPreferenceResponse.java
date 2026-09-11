package com.jobplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private Long id;
    private Boolean applicationStatus;
    private Boolean interviewUpdates;
    private Boolean jobAlerts;
    private Boolean systemNotifications;
}
