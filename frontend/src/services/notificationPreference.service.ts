import { api } from './api';
import type { ApiResponse } from '@/types/api';

export interface NotificationPreference {
  id: number;
  applicationStatus: boolean;
  interviewUpdates: boolean;
  jobAlerts: boolean;
  systemNotifications: boolean;
}

export interface UpdateNotificationPreferenceRequest {
  applicationStatus?: boolean;
  interviewUpdates?: boolean;
  jobAlerts?: boolean;
  systemNotifications?: boolean;
}

export const notificationPreferenceService = {
  async getPreferences(): Promise<ApiResponse<NotificationPreference>> {
    return api.get<NotificationPreference>('/notifications/preferences');
  },

  async updatePreferences(data: UpdateNotificationPreferenceRequest): Promise<ApiResponse<NotificationPreference>> {
    return api.patch<NotificationPreference>('/notifications/preferences', data);
  },
};
