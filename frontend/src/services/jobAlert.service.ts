import { api } from './api';
import type { ApiResponse, PagedResponse } from '@/types/api';

export interface JobAlert {
  id: number;
  name: string;
  keywords?: string;
  location?: string;
  workplaceType?: string;
  employmentType?: string;
  minimumExperience?: number;
  skills?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  lastTriggeredAt?: string;
}

export interface CreateJobAlertRequest {
  name: string;
  keywords?: string;
  location?: string;
  workplaceType?: string;
  employmentType?: string;
  minimumExperience?: number;
  skills?: string;
  active?: boolean;
}

export interface UpdateJobAlertRequest {
  name?: string;
  keywords?: string;
  location?: string;
  workplaceType?: string;
  employmentType?: string;
  minimumExperience?: number;
  skills?: string;
  active?: boolean;
}

export const jobAlertService = {
  async getAlerts(page = 0, size = 20): Promise<ApiResponse<PagedResponse<JobAlert>>> {
    return api.get<PagedResponse<JobAlert>>(`/candidates/me/job-alerts?page=${page}&size=${size}`);
  },

  async getAlert(alertId: number): Promise<ApiResponse<JobAlert>> {
    return api.get<JobAlert>(`/candidates/me/job-alerts/${alertId}`);
  },

  async createAlert(data: CreateJobAlertRequest): Promise<ApiResponse<JobAlert>> {
    return api.post<JobAlert>('/candidates/me/job-alerts', data);
  },

  async updateAlert(alertId: number, data: UpdateJobAlertRequest): Promise<ApiResponse<JobAlert>> {
    return api.patch<JobAlert>(`/candidates/me/job-alerts/${alertId}`, data);
  },

  async deleteAlert(alertId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`/candidates/me/job-alerts/${alertId}`);
  },

  async toggleAlertStatus(alertId: number): Promise<ApiResponse<JobAlert>> {
    return api.patch<JobAlert>(`/candidates/me/job-alerts/${alertId}/status`);
  },
};
