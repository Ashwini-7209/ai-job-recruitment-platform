import { api } from './api';

export interface RecruiterProfile {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phone: string | null;
  jobTitle: string | null;
  department: string | null;
  companyName: string | null;
  companyWebsite: string | null;
  companyDescription: string | null;
  companyLocation: string | null;
  linkedinUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProfileUpdateRequest {
  phone?: string;
  jobTitle?: string;
  department?: string;
  companyName?: string;
  companyWebsite?: string;
  companyDescription?: string;
  companyLocation?: string;
  linkedinUrl?: string;
}

export const recruiterProfileService = {
  async getProfile(): Promise<RecruiterProfile> {
    const response = await api.get<RecruiterProfile>('/recruiters/me/profile');
    return response.data!;
  },

  async updateProfile(data: ProfileUpdateRequest): Promise<RecruiterProfile> {
    const response = await api.put<RecruiterProfile>('/recruiters/me/profile', data);
    return response.data!;
  },
};
