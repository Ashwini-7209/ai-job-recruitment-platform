import { api } from './api';

export interface SkillGapResponse {
  jobId: number;
  jobTitle: string;
  jobRequiredSkills: string[];
  candidateSkills: string[];
  matchedSkills: string[];
  missingSkills: string[];
  partiallyMatchedSkills: string[];
  prioritySuggestions: string[];
  explanation: string;
  aiEnhanced: boolean;
}

export interface CareerInsightsResponse {
  strengths: string[];
  recommendedSkills: string[];
  suggestedJobCategories: string[];
  profileImprovements: string[];
  resumeImprovements: string[];
  generalCareerSuggestions: string[];
  aiEnhanced: boolean;
}

export interface ResumeImprovementResponse {
  resumeId: number;
  completeness: number;
  strengths: string[];
  improvements: string[];
  missingSections: string[];
  keywordSuggestions: string[];
  aiEnhanced: boolean;
}

export interface JobMatchResponse {
  jobId: number;
  jobTitle: string;
  overallScore: number;
  skillScore: number;
  experienceScore: number;
  profileScore: number;
  semanticScore: number | null;
  matchedSkills: string[];
  missingSkills: string[];
  matchingReasons: string[];
  potentialGaps: string[];
  aiExplanation: string | null;
  aiUsed: boolean;
}

export const aiService = {
  getJobMatch: (jobId: number) =>
    api.get<JobMatchResponse>(`/candidates/me/jobs/${jobId}/match`),

  getSkillGap: (jobId: number) =>
    api.get<SkillGapResponse>(`/candidates/me/jobs/${jobId}/skill-gap`),

  getCareerInsights: () =>
    api.get<CareerInsightsResponse>('/candidates/me/career-insights'),

  analyzeResumeImprovement: (resumeId: number) =>
    api.post<ResumeImprovementResponse>(`/candidates/me/resumes/${resumeId}/improvement-analysis`),
};
