import type {
  PlatformStats,
  ActivityItem,
  ModerationQueueItem,
  UserOverview,
  VerificationRequest,
  JobModeration,
  Report,
  AnalyticsMetric,
  SystemService,
  QuickAction,
} from '@/types/admin';

export const platformStats: PlatformStats = {
  totalUsers: 12847,
  candidates: 9234,
  recruiters: 3456,
  companies: 1892,
  activeJobs: 478,
  applications: 34521,
  pendingReports: 23,
};

export const recentActivity: ActivityItem[] = [
  {
    id: '1',
    type: 'recruiter_registered',
    actor: 'Sarah Johnson',
    description: 'New recruiter registered at TechCorp Inc.',
    timestamp: '5 minutes ago',
    status: 'info',
  },
  {
    id: '2',
    type: 'company_submitted',
    actor: 'InnovateLab',
    description: 'Company profile submitted for verification',
    timestamp: '12 minutes ago',
    status: 'warning',
  },
  {
    id: '3',
    type: 'job_posted',
    actor: 'Michael Chen',
    description: 'Posted "Senior React Developer" at StartupXYZ',
    timestamp: '28 minutes ago',
    status: 'success',
  },
  {
    id: '4',
    type: 'application_created',
    actor: 'Emily Rodriguez',
    description: 'Applied to "Product Manager" at GlobalTech',
    timestamp: '1 hour ago',
    status: 'info',
  },
  {
    id: '5',
    type: 'verification_requested',
    actor: 'DataFlow Systems',
    description: 'Recruiter verification requested',
    timestamp: '2 hours ago',
    status: 'warning',
  },
  {
    id: '6',
    type: 'report_submitted',
    actor: 'Anonymous',
    description: 'Report submitted for suspicious job posting',
    timestamp: '3 hours ago',
    status: 'error',
  },
];

export const moderationQueues: ModerationQueueItem[] = [
  {
    id: '1',
    type: 'recruiter_verification',
    title: 'Verify TechCorp Recruiter',
    description: 'New recruiter account pending verification',
    submittedBy: 'Sarah Johnson',
    submittedDate: 'Jan 15, 2025',
    priority: 'high',
    status: 'pending',
  },
  {
    id: '2',
    type: 'job_moderation',
    title: 'Review "Data Scientist" Posting',
    description: 'Job posting flagged for salary transparency',
    submittedBy: 'DataFlow Systems',
    submittedDate: 'Jan 14, 2025',
    priority: 'medium',
    status: 'in_review',
  },
  {
    id: '3',
    type: 'user_report',
    title: 'Report: Fake Company Profile',
    description: 'User reported potentially fraudulent company',
    submittedBy: 'Anonymous',
    submittedDate: 'Jan 14, 2025',
    priority: 'high',
    status: 'pending',
  },
  {
    id: '4',
    type: 'company_verification',
    title: 'Verify InnovateLab',
    description: 'Company profile submitted for verification',
    submittedBy: 'InnovateLab Admin',
    submittedDate: 'Jan 13, 2025',
    priority: 'medium',
    status: 'pending',
  },
];

export const userOverview: UserOverview = {
  candidates: { total: 9234, active: 7823, new: 142 },
  recruiters: { total: 3456, verified: 2891, pending: 565 },
  admins: { total: 12 },
};

export const verificationRequests: VerificationRequest[] = [
  {
    id: '1',
    companyName: 'TechCorp Inc.',
    recruiterName: 'Sarah Johnson',
    submittedDate: 'Jan 15, 2025',
    status: 'pending',
    priority: 'high',
  },
  {
    id: '2',
    companyName: 'InnovateLab',
    recruiterName: 'David Park',
    submittedDate: 'Jan 13, 2025',
    status: 'pending',
    priority: 'medium',
  },
  {
    id: '3',
    companyName: 'DataFlow Systems',
    recruiterName: 'Lisa Wang',
    submittedDate: 'Jan 12, 2025',
    status: 'verified',
    priority: 'low',
  },
];

export const jobModerations: JobModeration[] = [
  {
    id: '1',
    title: 'Senior React Developer',
    company: 'StartupXYZ',
    submittedDate: 'Jan 15, 2025',
    status: 'pending',
    priority: 'high',
  },
  {
    id: '2',
    title: 'Data Scientist',
    company: 'DataFlow Systems',
    submittedDate: 'Jan 14, 2025',
    status: 'flagged',
    priority: 'medium',
  },
  {
    id: '3',
    title: 'Product Manager',
    company: 'GlobalTech',
    submittedDate: 'Jan 13, 2025',
    status: 'approved',
    priority: 'low',
  },
];

export const reports: Report[] = [
  {
    id: '1',
    category: 'spam',
    count: 8,
    priority: 'medium',
    status: 'open',
  },
  {
    id: '2',
    category: 'inappropriate',
    count: 5,
    priority: 'high',
    status: 'investigating',
  },
  {
    id: '3',
    category: 'suspicious_recruiter',
    count: 7,
    priority: 'high',
    status: 'open',
  },
  {
    id: '4',
    category: 'incorrect_info',
    count: 3,
    priority: 'low',
    status: 'resolved',
  },
];

export const analyticsMetrics: AnalyticsMetric[] = [
  { label: 'User Growth', value: 12847, change: 8.2, changeLabel: 'vs last month' },
  { label: 'Job Activity', value: 478, change: 12.5, changeLabel: 'active postings' },
  { label: 'Applications', value: 34521, change: 15.3, changeLabel: 'this month' },
  { label: 'Recruiter Activity', value: 3456, change: 5.7, changeLabel: 'verified accounts' },
];

export const systemServices: SystemService[] = [
  { name: 'API Gateway', status: 'operational', lastChecked: '2 min ago' },
  { name: 'Database', status: 'operational', lastChecked: '2 min ago' },
  { name: 'File Storage', status: 'operational', lastChecked: '5 min ago' },
  { name: 'AI Services', status: 'degraded', lastChecked: '1 min ago' },
  { name: 'Email Service', status: 'operational', lastChecked: '3 min ago' },
];

export const quickActions: QuickAction[] = [
  { label: 'Review Recruiters', icon: 'M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z', href: '/admin/recruiters' },
  { label: 'Moderate Jobs', icon: 'M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z', href: '/admin/jobs' },
  { label: 'Review Reports', icon: 'M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z', href: '/admin/reports' },
  { label: 'Manage Users', icon: 'M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z', href: '/admin/users' },
  { label: 'View Analytics', icon: 'M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z', href: '/admin/analytics' },
];
