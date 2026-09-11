import { api } from './api';
import type { ApiResponse, PagedResponse } from '@/types/api';

export interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  role: 'CANDIDATE' | 'RECRUITER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
  applicationCount?: number;
  jobCount?: number;
}

export interface AdminJob {
  id: number;
  title: string;
  recruiterName: string;
  recruiterEmail: string;
  location?: string;
  workplaceType?: string;
  employmentType?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
  experienceMin?: number;
  experienceMax?: number;
  applicationCount?: number;
  createdAt: string;
  publishedAt?: string;
  applicationDeadline?: string;
}

export interface AdminApplication {
  id: number;
  candidateName: string;
  candidateEmail: string;
  jobTitle: string;
  recruiterName: string;
  status: string;
  appliedAt: string;
  updatedAt: string;
}

export interface AuditLog {
  id: number;
  actorId: number;
  actorName: string;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId: number;
  description?: string;
  createdAt: string;
}

export interface AdminDashboardStats {
  totalUsers: number;
  totalCandidates: number;
  totalRecruiters: number;
  activeUsers: number;
  totalJobs: number;
  publishedJobs: number;
  totalApplications: number;
}

export const adminService = {
  async getDashboardStats(): Promise<ApiResponse<AdminDashboardStats>> {
    return api.get<AdminDashboardStats>('/admin/users/dashboard-stats');
  },

  async getUsers(params: {
    q?: string;
    role?: string;
    enabled?: boolean;
    page?: number;
    size?: number;
  } = {}): Promise<ApiResponse<PagedResponse<AdminUser>>> {
    const searchParams = new URLSearchParams();
    if (params.q) searchParams.set('q', params.q);
    if (params.role) searchParams.set('role', params.role);
    if (params.enabled !== undefined) searchParams.set('enabled', String(params.enabled));
    searchParams.set('page', String(params.page ?? 0));
    searchParams.set('size', String(params.size ?? 20));
    return api.get<PagedResponse<AdminUser>>(`/admin/users?${searchParams.toString()}`);
  },

  async getUser(userId: number): Promise<ApiResponse<AdminUser>> {
    return api.get<AdminUser>(`/admin/users/${userId}`);
  },

  async updateUserStatus(userId: number, enabled: boolean): Promise<ApiResponse<AdminUser>> {
    return api.patch<AdminUser>(`/admin/users/${userId}/status`, { enabled });
  },

  async updateUserRole(userId: number, role: string): Promise<ApiResponse<AdminUser>> {
    return api.patch<AdminUser>(`/admin/users/${userId}/role`, { role });
  },

  async getJobs(params: {
    q?: string;
    status?: string;
    page?: number;
    size?: number;
  } = {}): Promise<ApiResponse<PagedResponse<AdminJob>>> {
    const searchParams = new URLSearchParams();
    if (params.q) searchParams.set('q', params.q);
    if (params.status) searchParams.set('status', params.status);
    searchParams.set('page', String(params.page ?? 0));
    searchParams.set('size', String(params.size ?? 20));
    return api.get<PagedResponse<AdminJob>>(`/admin/jobs?${searchParams.toString()}`);
  },

  async updateJobStatus(jobId: number, status: string): Promise<ApiResponse<AdminJob>> {
    return api.patch<AdminJob>(`/admin/jobs/${jobId}/status?status=${status}`);
  },

  async getApplications(params: {
    jobId?: number;
    status?: string;
    candidateName?: string;
    page?: number;
    size?: number;
  } = {}): Promise<ApiResponse<PagedResponse<AdminApplication>>> {
    const searchParams = new URLSearchParams();
    if (params.jobId) searchParams.set('jobId', String(params.jobId));
    if (params.status) searchParams.set('status', params.status);
    if (params.candidateName) searchParams.set('candidateName', params.candidateName);
    searchParams.set('page', String(params.page ?? 0));
    searchParams.set('size', String(params.size ?? 20));
    return api.get<PagedResponse<AdminApplication>>(`/admin/applications?${searchParams.toString()}`);
  },

  async getAuditLogs(params: {
    actorId?: number;
    action?: string;
    entityType?: string;
    page?: number;
    size?: number;
  } = {}): Promise<ApiResponse<PagedResponse<AuditLog>>> {
    const searchParams = new URLSearchParams();
    if (params.actorId) searchParams.set('actorId', String(params.actorId));
    if (params.action) searchParams.set('action', params.action);
    if (params.entityType) searchParams.set('entityType', params.entityType);
    searchParams.set('page', String(params.page ?? 0));
    searchParams.set('size', String(params.size ?? 20));
    return api.get<PagedResponse<AuditLog>>(`/admin/audit-logs?${searchParams.toString()}`);
  },
};
