import { api } from './api';
import type { RecruiterInterviewDetail } from '@/types/recruiter';
import type { CandidateInterviewDetail } from '@/types/candidate';
import type { PagedResponse } from '@/types/api';

export interface CreateInterviewData {
  title: string;
  interviewType: 'VIDEO' | 'PHONE' | 'IN_PERSON';
  scheduledStart: string;
  scheduledEnd: string;
  location?: string;
  meetingLink?: string;
  interviewerName?: string;
  interviewerNotes?: string;
  candidateNotes?: string;
}

export interface UpdateInterviewData {
  title?: string;
  interviewType?: 'VIDEO' | 'PHONE' | 'IN_PERSON';
  scheduledStart?: string;
  scheduledEnd?: string;
  location?: string;
  meetingLink?: string;
  interviewerName?: string;
  interviewerNotes?: string;
  candidateNotes?: string;
}

export const interviewService = {
  // Recruiter methods
  async createInterview(applicationId: number, data: CreateInterviewData): Promise<RecruiterInterviewDetail> {
    const response = await api.post<RecruiterInterviewDetail>(
      `/recruiters/me/applications/${applicationId}/interviews`,
      data
    );
    return response.data!;
  },

  async getRecruiterInterviews(params?: { page?: number; size?: number; sort?: string }): Promise<PagedResponse<RecruiterInterviewDetail>> {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    const response = await api.get<PagedResponse<RecruiterInterviewDetail>>(
      `/recruiters/me/interviews${query ? `?${query}` : ''}`
    );
    return response.data!;
  },

  async getUpcomingRecruiterInterviews(): Promise<RecruiterInterviewDetail[]> {
    const response = await api.get<RecruiterInterviewDetail[]>('/recruiters/me/interviews/upcoming');
    return response.data!;
  },

  async getRecruiterInterviewById(interviewId: number): Promise<RecruiterInterviewDetail> {
    const response = await api.get<RecruiterInterviewDetail>(`/recruiters/me/interviews/${interviewId}`);
    return response.data!;
  },

  async updateInterview(interviewId: number, data: UpdateInterviewData): Promise<RecruiterInterviewDetail> {
    const response = await api.patch<RecruiterInterviewDetail>(`/recruiters/me/interviews/${interviewId}`, data);
    return response.data!;
  },

  async cancelInterview(interviewId: number): Promise<RecruiterInterviewDetail> {
    const response = await api.patch<RecruiterInterviewDetail>(`/recruiters/me/interviews/${interviewId}/cancel`);
    return response.data!;
  },

  async getRecruiterInterviewStats(): Promise<number> {
    const response = await api.get<number>('/recruiters/me/interview-stats');
    return response.data!;
  },

  // Candidate methods
  async getCandidateInterviews(params?: { page?: number; size?: number; sort?: string }): Promise<PagedResponse<CandidateInterviewDetail>> {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    const response = await api.get<PagedResponse<CandidateInterviewDetail>>(
      `/candidates/me/interviews${query ? `?${query}` : ''}`
    );
    return response.data!;
  },

  async getUpcomingCandidateInterviews(): Promise<CandidateInterviewDetail[]> {
    const response = await api.get<CandidateInterviewDetail[]>('/candidates/me/interviews/upcoming');
    return response.data!;
  },

  async getCandidateInterviewById(interviewId: number): Promise<CandidateInterviewDetail> {
    const response = await api.get<CandidateInterviewDetail>(`/candidates/me/interviews/${interviewId}`);
    return response.data!;
  },

  async getCandidateInterviewStats(): Promise<number> {
    const response = await api.get<number>('/candidates/me/interview-stats');
    return response.data!;
  },
};
