import { api } from './api';
import type { PagedResponse } from '@/types/api';

export interface JobListItem {
  jobId: number;
  title: string;
  companyName: string;
  location: string;
  employmentType: string;
  workplaceType: string;
  experienceMin: number | null;
  experienceMax: number | null;
  salaryMin: number | null;
  salaryMax: number | null;
  skills: string;
  description: string;
  status: string;
  createdAt: string;
  deadline: string | null;
  saved: boolean;
  applied: boolean;
}

export interface JobSearchParams {
  q?: string;
  location?: string;
  employmentType?: string;
  workplaceType?: string;
  experienceMin?: number;
  experienceMax?: number;
  salaryMin?: number;
  salaryMax?: number;
  page?: number;
  size?: number;
  sort?: string;
}

class JobService {
  async searchJobs(params?: JobSearchParams): Promise<PagedResponse<JobListItem>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParams.append(key, String(value));
        }
      });
    }
    const queryString = queryParams.toString();
    const url = `/candidate/jobs${queryString ? `?${queryString}` : ''}`;
    const response = await api.get<PagedResponse<JobListItem>>(url);
    return response.data!;
  }

  async getJobById(jobId: number): Promise<JobListItem> {
    const response = await api.get<JobListItem>(`/candidate/jobs/${jobId}`);
    return response.data!;
  }

  async getRecommendations(page = 0, size = 10, minScore?: number): Promise<PagedResponse<JobListItem>> {
    const queryParams = new URLSearchParams();
    queryParams.append('page', String(page));
    queryParams.append('size', String(size));
    if (minScore !== undefined) queryParams.append('minScore', String(minScore));
    const response = await api.get<PagedResponse<JobListItem>>(`/candidates/me/recommendations?${queryParams.toString()}`);
    return response.data!;
  }
}

export const jobService = new JobService();
