import { api } from './api';
import type { PagedResponse } from '@/types/api';

export interface ApplicationSummary {
  id: number;
  jobId: number;
  jobTitle: string;
  companyName: string;
  candidateName: string;
  candidateEmail: string;
  status: string;
  appliedAt: string;
  updatedAt: string;
}

export interface CandidateInfo {
  candidateId: number;
  fullName: string;
  email: string;
  headline: string | null;
  location: string | null;
  bio: string | null;
  currentJobTitle: string | null;
  yearsOfExperience: number | null;
  skillsSummary: string | null;
  educationSummary: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  parsedSkills: string | null;
  parsedYearsOfExperience: number | null;
  professionalSummary: string | null;
}

export interface StatusHistoryEntry {
  id: number;
  oldStatus: string | null;
  newStatus: string;
  changedByName: string;
  changedAt: string;
  reason: string | null;
}

export interface ApplicationNote {
  id: number;
  note: string;
  recruiterName: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationDetail {
  applicationId: number;
  jobId: number;
  jobTitle: string;
  status: string;
  coverLetter: string | null;
  appliedAt: string;
  updatedAt: string;
  resumeId: number | null;
  resumeFileName: string | null;
  resumeContentType: string | null;
  resumeFileSize: number | null;
  candidate: CandidateInfo;
  statusHistory: StatusHistoryEntry[];
  notes: ApplicationNote[];
}

export interface ApplicationStats {
  totalApplications: number;
  appliedCount: number;
  underReviewCount: number;
  shortlistedCount: number;
  rejectedCount: number;
  hiredCount: number;
  withdrawnCount: number;
}

export const recruiterApplicationService = {
  async getApplications(params?: {
    page?: number;
    size?: number;
    sort?: string;
    q?: string;
    jobId?: number;
    status?: string;
  }): Promise<PagedResponse<ApplicationSummary>> {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);
    if (params?.q) searchParams.set('q', params.q);
    if (params?.jobId) searchParams.set('jobId', String(params.jobId));
    if (params?.status) searchParams.set('status', params.status);
    const query = searchParams.toString();
    const response = await api.get<PagedResponse<ApplicationSummary>>(`/recruiters/me/applications${query ? `?${query}` : ''}`);
    return response.data!;
  },

  async getApplicationDetail(applicationId: number): Promise<ApplicationDetail> {
    const response = await api.get<ApplicationDetail>(`/recruiters/me/applications/${applicationId}`);
    return response.data!;
  },

  async updateStatus(applicationId: number, status: string): Promise<{ id: number; status: string }> {
    const response = await api.patch<{ id: number; status: string }>(`/recruiters/me/applications/${applicationId}/status`, { status });
    return response.data!;
  },

  async unhireApplication(applicationId: number, reason: string): Promise<{ id: number; status: string }> {
    const response = await api.patch<{ id: number; status: string }>(`/recruiters/me/applications/${applicationId}/unhire`, { reason });
    return response.data!;
  },

  async getStats(): Promise<ApplicationStats> {
    const response = await api.get<ApplicationStats>('/recruiters/me/application-stats');
    return response.data!;
  },

  async addNote(applicationId: number, note: string): Promise<ApplicationNote> {
    const response = await api.post<ApplicationNote>(`/recruiters/me/applications/${applicationId}/notes`, { note });
    return response.data!;
  },

  async getNotes(applicationId: number): Promise<ApplicationNote[]> {
    const response = await api.get<ApplicationNote[]>(`/recruiters/me/applications/${applicationId}/notes`);
    return response.data!;
  },

  async downloadResume(applicationId: number, filename: string): Promise<void> {
    await api.downloadBlob(`/recruiters/me/applications/${applicationId}/resume`, filename);
  },

  async getJobApplications(jobId: number, params?: { page?: number; size?: number; sort?: string }): Promise<PagedResponse<ApplicationSummary>> {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);
    const query = searchParams.toString();
    const response = await api.get<PagedResponse<ApplicationSummary>>(`/recruiters/me/jobs/${jobId}/applications${query ? `?${query}` : ''}`);
    return response.data!;
  },
};
