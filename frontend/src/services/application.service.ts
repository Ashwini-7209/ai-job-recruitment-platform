import { api } from './api';
import type { CandidateApplicationSummary, CandidateApplicationDetail, CandidateApplicationStats } from '@/types/candidate';
import type { PagedResponse } from '@/types/api';

export interface ApplicationListParams {
  q?: string;
  status?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const applicationService = {
  async listApplications(params?: ApplicationListParams): Promise<PagedResponse<CandidateApplicationSummary>> {
    const searchParams = new URLSearchParams();
    if (params?.q) searchParams.set('q', params.q);
    if (params?.status) searchParams.set('status', params.status);
    if (params?.page !== undefined) searchParams.set('page', String(params.page));
    if (params?.size !== undefined) searchParams.set('size', String(params.size));
    if (params?.sort) searchParams.set('sort', params.sort);

    const query = searchParams.toString();
    const url = `/candidates/me/applications${query ? `?${query}` : ''}`;
    const response = await api.get<PagedResponse<CandidateApplicationSummary>>(url);
    return response.data!;
  },

  async getApplicationDetail(applicationId: number): Promise<CandidateApplicationDetail> {
    const response = await api.get<CandidateApplicationDetail>(`/candidates/me/applications/${applicationId}`);
    return response.data!;
  },

  async withdrawApplication(applicationId: number) {
    const response = await api.patch<{ id: number; status: string }>(`/candidates/me/applications/${applicationId}/withdraw`);
    return response.data!;
  },

  async getStats(): Promise<CandidateApplicationStats> {
    const response = await api.get<CandidateApplicationStats>('/candidates/me/application-stats');
    return response.data!;
  },
};
