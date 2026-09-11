import { api } from './api';
import type { PagedResponse } from '@/types/api';

export type NotificationType =
  | 'APPLICATION_RECEIVED'
  | 'APPLICATION_STATUS_CHANGED'
  | 'INTERVIEW_SCHEDULED'
  | 'INTERVIEW_RESCHEDULED'
  | 'INTERVIEW_CANCELLED'
  | 'JOB_RECOMMENDATION'
  | 'GENERAL';

export interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  entityId: number | null;
  entityType: string | null;
  read: boolean;
  createdAt: string;
}

export const notificationService = {
  async getNotifications(params?: { page?: number; size?: number }): Promise<PagedResponse<Notification>> {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));

    const query = searchParams.toString();
    const response = await api.get<PagedResponse<Notification>>(`/notifications${query ? `?${query}` : ''}`);
    return response.data!;
  },

  async getUnreadCount(): Promise<number> {
    const response = await api.get<number>('/notifications/unread-count');
    return response.data!;
  },

  async markAsRead(notificationId: number): Promise<void> {
    await api.patch(`/notifications/${notificationId}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await api.patch('/notifications/read-all');
  },
};
