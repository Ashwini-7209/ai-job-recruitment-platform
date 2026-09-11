import { api } from './api';

export interface TrendPoint {
  date: string;
  count: number;
}

export interface TrendResponse {
  data: TrendPoint[];
}

export interface CandidateAnalyticsSummary {
  totalApplications: number;
  applicationsUnderReview: number;
  applicationsShortlisted: number;
  applicationsRejected: number;
  applicationsHired: number;
  applicationsWithdrawn: number;
  totalInterviews: number;
  upcomingInterviews: number;
  totalSavedJobs: number;
  activeJobAlerts: number;
}

export interface ApplicationStatusDistribution {
  statusDistribution: Record<string, number>;
}

export interface RecruiterAnalyticsSummary {
  totalJobs: number;
  publishedJobs: number;
  closedJobs: number;
  totalApplications: number;
  applicationsUnderReview: number;
  shortlistedCandidates: number;
  rejectedCandidates: number;
  hiredCandidates: number;
  upcomingInterviews: number;
  completedInterviews: number;
}

export interface RecruiterFunnel {
  funnel: Record<string, number>;
}

export interface RecruiterJobAnalytics {
  jobId: number;
  jobTitle: string;
  status: string;
  applicationCount: number;
  shortlistedCount: number;
  hiredCount: number;
  interviewCount: number;
  createdAt: string;
  publishedAt: string | null;
}

export interface RecruiterHiringAnalytics {
  totalHired: number;
  totalShortlisted: number;
  totalInterviews: number;
  completedInterviews: number;
  cancelledInterviews: number;
  shortlistRate: number;
  hireRate: number;
}

export interface AdminAnalyticsSummary {
  totalUsers: number;
  totalCandidates: number;
  totalRecruiters: number;
  activeUsers: number;
  totalJobs: number;
  publishedJobs: number;
  closedJobs: number;
  totalApplications: number;
  totalInterviews: number;
  activeJobAlerts: number;
  totalSavedJobs: number;
}

export interface AdminJobAnalytics {
  totalJobs: number;
  draftJobs: number;
  publishedJobs: number;
  closedJobs: number;
  applicationsPerJob: Record<string, number>;
}

export interface AdminApplicationAnalytics {
  totalApplications: number;
  statusDistribution: Record<string, number>;
}

export interface AdminInterviewAnalytics {
  totalInterviews: number;
  scheduled: number;
  rescheduled: number;
  completed: number;
  cancelled: number;
  statusDistribution: Record<string, number>;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export const analyticsService = {
  // Candidate analytics
  getCandidateSummary: () =>
    api.get<CandidateAnalyticsSummary>('/candidates/me/analytics/summary'),

  getCandidateApplicationStatus: () =>
    api.get<ApplicationStatusDistribution>('/candidates/me/analytics/application-status'),

  getCandidateApplicationTrend: (from?: string, to?: string) => {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    return api.get<TrendResponse>(`/candidates/me/analytics/application-trend?${params.toString()}`);
  },

  // Recruiter analytics
  getRecruiterSummary: () =>
    api.get<RecruiterAnalyticsSummary>('/recruiters/me/analytics/summary'),

  getRecruiterFunnel: () =>
    api.get<RecruiterFunnel>('/recruiters/me/analytics/funnel'),

  getRecruiterJobAnalytics: (page = 0, size = 10) =>
    api.get<PagedResponse<RecruiterJobAnalytics>>(`/recruiters/me/analytics/jobs?page=${page}&size=${size}`),

  getRecruiterApplicationTrend: (from?: string, to?: string) => {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    return api.get<TrendResponse>(`/recruiters/me/analytics/application-trend?${params.toString()}`);
  },

  getRecruiterHiringMetrics: () =>
    api.get<RecruiterHiringAnalytics>('/recruiters/me/analytics/hiring-metrics'),

  // Admin analytics
  getAdminSummary: () =>
    api.get<AdminAnalyticsSummary>('/admin/analytics/summary'),

  getAdminUserGrowth: (from?: string, to?: string, roleFilter?: string) => {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    if (roleFilter) params.append('roleFilter', roleFilter);
    return api.get<TrendResponse>(`/admin/analytics/user-growth?${params.toString()}`);
  },

  getAdminJobAnalytics: () =>
    api.get<AdminJobAnalytics>('/admin/analytics/jobs'),

  getAdminApplicationAnalytics: () =>
    api.get<AdminApplicationAnalytics>('/admin/analytics/applications'),

  getAdminInterviewAnalytics: () =>
    api.get<AdminInterviewAnalytics>('/admin/analytics/interviews'),

  getAdminApplicationTrend: (from?: string, to?: string) => {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    return api.get<TrendResponse>(`/admin/analytics/application-trend?${params.toString()}`);
  },
};
