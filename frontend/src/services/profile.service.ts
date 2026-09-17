import { api } from './api';

export interface CandidateProfile {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phone: string | null;
  location: string | null;
  headline: string | null;
  bio: string | null;
  currentJobTitle: string | null;
  yearsOfExperience: number | null;
  educationSummary: string | null;
  skillsSummary: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  profileImageUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProfileUpdateRequest {
  phone?: string;
  location?: string;
  headline?: string;
  bio?: string;
  currentJobTitle?: string;
  yearsOfExperience?: number;
  educationSummary?: string;
  skillsSummary?: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
  profileImageUrl?: string;
}

export interface SectionCompletion {
  section: string;
  label: string;
  completed: boolean;
  weight: number;
  missingFields: string[];
}

export interface ProfileCompletion {
  overallPercentage: number;
  sections: SectionCompletion[];
}

export interface CareerDashboardData {
  profileCompletion: number;
  strongestSkills: string[];
  matchingJobCategories: string[];
  skillGaps: { skill: string; reason: string }[];
  applicationActivity: {
    totalApplications: number;
    underReview: number;
    shortlisted: number;
    interviews: number;
    hired: number;
    responseRate: number;
  };
  recommendedJobs: {
    jobId: number;
    title: string;
    company: string | null;
    location: string | null;
    matchScore: number;
    reason: string;
  }[];
  upcomingInterviews: {
    interviewId: number;
    jobTitle: string;
    title: string;
    interviewType: string;
    scheduledStart: string;
    location: string | null;
  }[];
}

export const profileService = {
  async getProfile(): Promise<CandidateProfile> {
    const response = await api.get<CandidateProfile>('/candidates/me/profile');
    return response.data!;
  },

  async updateProfile(data: ProfileUpdateRequest): Promise<CandidateProfile> {
    const response = await api.put<CandidateProfile>('/candidates/me/profile', data);
    return response.data!;
  },

  async getProfileCompletion(): Promise<ProfileCompletion> {
    const response = await api.get<ProfileCompletion>('/candidates/me/profile/completion');
    return response.data!;
  },

  async getCareerDashboard(): Promise<CareerDashboardData> {
    const response = await api.get<CareerDashboardData>('/candidates/me/career/dashboard');
    return response.data!;
  },
};
