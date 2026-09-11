import { api } from './api';
import type { PagedResponse } from '@/types/api';

export interface SavedJob {
  savedJobId: number;
  jobId: number;
  jobTitle: string;
  location: string | null;
  employmentType: string;
  workplaceType: string;
  salaryMin: number | null;
  salaryMax: number | null;
  skills: string | null;
  jobStatus: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
  deadline: string | null;
  savedAt: string;
  applied: boolean;
}

export interface JobSummaryWithSaved {
  id: number;
  title: string;
  location: string | null;
  employmentType: string;
  workplaceType: string;
  salaryMin: number | null;
  salaryMax: number | null;
  skills: string | null;
  applicationDeadline: string | null;
  createdAt: string;
  saved: boolean;
  applied: boolean;
}

export const savedJobService = {
  async saveJob(jobId: number): Promise<SavedJob> {
    const response = await api.post<SavedJob>(`/candidates/me/saved-jobs/${jobId}`);
    return response.data!;
  },

  async unsaveJob(jobId: number): Promise<void> {
    await api.delete(`/candidates/me/saved-jobs/${jobId}`);
  },

  async getSavedJobs(params?: {
    q?: string;
    location?: string;
    workplaceType?: string;
    employmentType?: string;
    jobStatus?: string;
    page?: number;
    size?: number;
  }): Promise<PagedResponse<SavedJob>> {
    const searchParams = new URLSearchParams();
    if (params?.q) searchParams.set('q', params.q);
    if (params?.location) searchParams.set('location', params.location);
    if (params?.workplaceType) searchParams.set('workplaceType', params.workplaceType);
    if (params?.employmentType) searchParams.set('employmentType', params.employmentType);
    if (params?.jobStatus) searchParams.set('jobStatus', params.jobStatus);
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));

    const query = searchParams.toString();
    const response = await api.get<PagedResponse<SavedJob>>(`/candidates/me/saved-jobs${query ? `?${query}` : ''}`);
    return response.data!;
  },

  async getSavedStatus(jobId: number): Promise<boolean> {
    const response = await api.get<boolean>(`/candidates/me/saved-jobs/${jobId}/status`);
    return response.data!;
  },

  async getJobListingWithSaved(params?: {
    q?: string;
    location?: string;
    employmentType?: string;
    workplaceType?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PagedResponse<JobSummaryWithSaved>> {
    const searchParams = new URLSearchParams();
    if (params?.q) searchParams.set('q', params.q);
    if (params?.location) searchParams.set('location', params.location);
    if (params?.employmentType) searchParams.set('employmentType', params.employmentType);
    if (params?.workplaceType) searchParams.set('workplaceType', params.workplaceType);
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    const response = await api.get<PagedResponse<JobSummaryWithSaved>>(`/candidate/jobs${query ? `?${query}` : ''}`);
    return response.data!;
  },
};
