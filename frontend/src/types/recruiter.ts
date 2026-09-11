export interface RecruiterJob {
  id: string;
  title: string;
  department: string;
  location: string;
  type: 'Full-time' | 'Part-time' | 'Contract' | 'Internship';
  applications: number;
  status: 'Active' | 'Paused' | 'Closed' | 'Draft';
  postedDate: string;
}

export interface RecruiterCandidate {
  id: string;
  name: string;
  role: string;
  experience: string;
  skills: string[];
  stage: 'New' | 'Screening' | 'Shortlisted' | 'Interview' | 'Selected';
  matchScore: number;
}

export interface RecruiterApplication {
  id: string;
  candidateName: string;
  jobTitle: string;
  appliedDate: string;
  status: 'New' | 'Screening' | 'Shortlisted' | 'Interview' | 'Rejected' | 'Hired';
  matchScore: number;
}

export interface RecruiterInterview {
  id: string;
  candidateName: string;
  jobTitle: string;
  date: string;
  time: string;
  type: string;
  status: string;
}

export interface RecruiterInterviewDetail {
  interviewId: number;
  applicationId: number;
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  jobId: number;
  jobTitle: string;
  title: string;
  interviewType: 'VIDEO' | 'PHONE' | 'IN_PERSON';
  scheduledStart: string;
  scheduledEnd: string;
  location: string | null;
  meetingLink: string | null;
  interviewerName: string | null;
  interviewerNotes: string | null;
  candidateNotes: string | null;
  status: 'SCHEDULED' | 'RESCHEDULED' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

export interface RecruiterNotification {
  id: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
  type: 'application' | 'interview' | 'candidate' | 'system';
}

export interface RecruiterInsight {
  id: string;
  type: 'matching' | 'description' | 'pipeline' | 'bottleneck';
  title: string;
  description: string;
  actionLabel: string;
}

export interface HiringMetric {
  label: string;
  value: number;
  change?: string;
  changeType?: 'positive' | 'negative' | 'neutral';
}

export interface HiringFunnelStage {
  stage: string;
  count: number;
  percentage: number;
  color: string;
}
