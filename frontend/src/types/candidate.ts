export interface CandidateJob {
  id: string;
  title: string;
  company: string;
  location: string;
  type: 'Full-time' | 'Part-time' | 'Contract' | 'Internship';
  experience: string;
  matchScore: number;
  postedDate: string;
  salary?: string;
}

export interface CandidateApplication {
  id: string;
  jobTitle: string;
  company: string;
  appliedDate: string;
  status: 'Applied' | 'Under Review' | 'Shortlisted' | 'Interview' | 'Rejected';
}

export interface CandidateApplicationSummary {
  applicationId: number;
  jobId: number;
  jobTitle: string;
  location: string;
  employmentType: string;
  workplaceType: string;
  status: 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'REJECTED' | 'HIRED' | 'WITHDRAWN';
  appliedAt: string;
  updatedAt: string;
  hasResume: boolean;
  resumeFileName: string | null;
  deadline: string | null;
}

export interface StatusHistoryEntry {
  id: number;
  oldStatus: string | null;
  newStatus: string;
  changedByName: string;
  changedAt: string;
}

export interface CandidateApplicationDetail {
  applicationId: number;
  status: 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'REJECTED' | 'HIRED' | 'WITHDRAWN';
  coverLetter: string | null;
  appliedAt: string;
  updatedAt: string;
  withdrawnAt: string | null;
  jobId: number;
  jobTitle: string;
  jobDescription: string;
  location: string;
  employmentType: string;
  workplaceType: string;
  experienceMin: number | null;
  experienceMax: number | null;
  salaryMin: number | null;
  salaryMax: number | null;
  skills: string | null;
  deadline: string | null;
  resumeId: number | null;
  resumeFileName: string | null;
  resumeContentType: string | null;
  resumeFileSize: number | null;
  statusHistory: StatusHistoryEntry[];
}

export interface CandidateApplicationStats {
  totalApplications: number;
  appliedCount: number;
  underReviewCount: number;
  shortlistedCount: number;
  rejectedCount: number;
  hiredCount: number;
  withdrawnCount: number;
}

export interface CandidateInterview {
  id: string;
  jobTitle: string;
  company: string;
  date: string;
  time: string;
  type: 'Video' | 'Phone' | 'In-person';
}

export interface CandidateInterviewDetail {
  interviewId: number;
  applicationId: number;
  jobId: number;
  jobTitle: string;
  title: string;
  interviewType: 'VIDEO' | 'PHONE' | 'IN_PERSON';
  scheduledStart: string;
  scheduledEnd: string;
  location: string | null;
  meetingLink: string | null;
  interviewerName: string | null;
  status: 'SCHEDULED' | 'RESCHEDULED' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

export interface CandidateNotification {
  id: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
  type: 'application' | 'interview' | 'message' | 'system';
}

export interface CandidateInsight {
  id: string;
  type: 'resume' | 'skill' | 'profile';
  title: string;
  description: string;
  actionLabel: string;
}

export interface DashboardStat {
  label: string;
  value: number;
  change?: string;
  changeType?: 'positive' | 'negative' | 'neutral';
  icon: React.ReactNode;
}
