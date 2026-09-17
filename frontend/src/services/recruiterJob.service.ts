import { api } from './api';
import type { PagedResponse } from '@/types/api';

export interface CreateJobData {
  title: string;
  description: string;
  location?: string;
  employmentType: string;
  workplaceType: string;
  experienceMin?: number;
  experienceMax?: number;
  salaryMin?: number;
  salaryMax?: number;
  skills?: string;
  applicationDeadline?: string;
}

export interface UpdateJobData extends Partial<CreateJobData> {}

export interface RecruiterJob {
  id: number;
  recruiterId: number;
  recruiterName: string;
  title: string;
  description: string;
  location: string | null;
  employmentType: string;
  workplaceType: string;
  experienceMin: number | null;
  experienceMax: number | null;
  salaryMin: number | null;
  salaryMax: number | null;
  skills: string | null;
  status: string;
  applicationDeadline: string | null;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RecruiterJobSummary {
  id: number;
  jobId: number;
  title: string;
  recruiterName: string;
  location: string | null;
  employmentType: string;
  workplaceType: string;
  experienceMin: number | null;
  experienceMax: number | null;
  salaryMin: number | null;
  salaryMax: number | null;
  skills: string | null;
  status: string;
  publishedAt: string | null;
  applicationDeadline: string | null;
  createdAt: string;
}

export interface RecruiterJobStats {
  totalJobs: number;
  draftJobs: number;
  publishedJobs: number;
  closedJobs: number;
}

export const recruiterJobService = {
  async createJob(data: CreateJobData): Promise<RecruiterJob> {
    const response = await api.post<RecruiterJob>('/recruiter/jobs', data);
    return response.data!;
  },

  async getJobs(params?: { page?: number; size?: number; status?: string }): Promise<PagedResponse<RecruiterJobSummary>> {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.status) searchParams.set('status', params.status);
    const query = searchParams.toString();
    const response = await api.get<PagedResponse<RecruiterJobSummary>>(`/recruiter/jobs${query ? `?${query}` : ''}`);
    return response.data!;
  },

  async getJobById(jobId: number): Promise<RecruiterJob> {
    const response = await api.get<RecruiterJob>(`/recruiter/jobs/${jobId}`);
    return response.data!;
  },

  async updateJob(jobId: number, data: UpdateJobData): Promise<RecruiterJob> {
    const response = await api.put<RecruiterJob>(`/recruiter/jobs/${jobId}`, data);
    return response.data!;
  },

  async publishJob(jobId: number): Promise<RecruiterJob> {
    const response = await api.post<RecruiterJob>(`/recruiter/jobs/${jobId}/publish`);
    return response.data!;
  },

  async closeJob(jobId: number): Promise<RecruiterJob> {
    const response = await api.post<RecruiterJob>(`/recruiter/jobs/${jobId}/close`);
    return response.data!;
  },

  async deleteJob(jobId: number): Promise<void> {
    await api.delete(`/recruiter/jobs/${jobId}`);
  },

  async getJobStats(): Promise<RecruiterJobStats> {
    const response = await api.get<RecruiterJobStats>('/recruiter/jobs/stats');
    return response.data!;
  },

  async generateJobDescription(data: {
    title?: string;
    skills?: string;
    employmentType?: string;
    workplaceType?: string;
    experienceMin?: number;
    experienceMax?: number;
  }): Promise<string> {
    const response = await api.post<{ description: string }>(
      '/recruiter/jobs/generate-description',
      data
    );
    if (!response.success || !response.data) {
      throw new Error(response.message || 'Failed to generate description');
    }
    return response.data.description;
  },
};
