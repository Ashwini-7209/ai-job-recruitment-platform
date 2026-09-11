export interface PlatformStats {
  totalUsers: number;
  candidates: number;
  recruiters: number;
  companies: number;
  activeJobs: number;
  applications: number;
  pendingReports: number;
}

export interface ActivityItem {
  id: string;
  type: 'recruiter_registered' | 'company_submitted' | 'job_posted' | 'application_created' | 'verification_requested' | 'report_submitted';
  actor: string;
  description: string;
  timestamp: string;
  status: 'info' | 'success' | 'warning' | 'error';
}

export interface ModerationQueueItem {
  id: string;
  type: 'recruiter_verification' | 'job_moderation' | 'user_report' | 'company_verification';
  title: string;
  description: string;
  submittedBy: string;
  submittedDate: string;
  priority: 'high' | 'medium' | 'low';
  status: 'pending' | 'in_review' | 'approved' | 'rejected' | 'flagged';
}

export interface UserOverview {
  candidates: { total: number; active: number; new: number };
  recruiters: { total: number; verified: number; pending: number };
  admins: { total: number };
}

export interface VerificationRequest {
  id: string;
  companyName: string;
  recruiterName: string;
  submittedDate: string;
  status: 'pending' | 'verified' | 'rejected';
  priority: 'high' | 'medium' | 'low';
}

export interface JobModeration {
  id: string;
  title: string;
  company: string;
  submittedDate: string;
  status: 'pending' | 'approved' | 'flagged' | 'rejected';
  priority: 'high' | 'medium' | 'low';
}

export interface Report {
  id: string;
  category: 'spam' | 'inappropriate' | 'suspicious_recruiter' | 'incorrect_info';
  count: number;
  priority: 'high' | 'medium' | 'low';
  status: 'open' | 'investigating' | 'resolved';
}

export interface AnalyticsMetric {
  label: string;
  value: number;
  change: number;
  changeLabel: string;
}

export interface SystemService {
  name: string;
  status: 'operational' | 'degraded' | 'down';
  lastChecked: string;
}

export interface QuickAction {
  label: string;
  icon: string;
  href: string;
}
