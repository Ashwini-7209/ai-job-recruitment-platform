import { api } from './api';
import type { PagedResponse } from '@/types/api';

export interface CandidateSearchResult {
  candidateId: number;
  userId: number;
  fullName: string;
  email: string;
  headline: string | null;
  location: string | null;
  currentJobTitle: string | null;
  yearsOfExperience: number | null;
  skillsSummary: string | null;
  educationSummary: string | null;
  bio: string | null;
  profileImageUrl: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  hasResume: boolean;
}

export interface CandidateSearchParams {
  q?: string;
  location?: string;
  skills?: string;
  minExperience?: number;
  maxExperience?: number;
  jobTitle?: string;
  page?: number;
  size?: number;
  sort?: string;
}

class RecruiterCandidateService {
  async searchCandidates(params?: CandidateSearchParams): Promise<PagedResponse<CandidateSearchResult>> {
    const queryParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParams.append(key, String(value));
        }
      });
    }
    const queryString = queryParams.toString();
    const url = `/recruiters/candidates${queryString ? `?${queryString}` : ''}`;
    const response = await api.get<PagedResponse<CandidateSearchResult>>(url);
    return response.data!;
  }
}

export const recruiterCandidateService = new RecruiterCandidateService();
