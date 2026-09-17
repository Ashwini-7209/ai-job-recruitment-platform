import type {
  RecruiterJob,
  RecruiterCandidate,
  RecruiterApplication,
  RecruiterInterview,
  RecruiterNotification,
  RecruiterInsight,
  HiringMetric,
  HiringFunnelStage,
} from '@/types/recruiter';

export const hiringMetrics: HiringMetric[] = [
  { label: 'Active Jobs', value: 12, change: '+2 this week', changeType: 'positive' },
  { label: 'Total Applications', value: 347, change: '+28 this week', changeType: 'positive' },
  { label: 'Shortlisted', value: 64, change: '+5 this week', changeType: 'positive' },
  { label: 'Interviews', value: 18, change: '3 today', changeType: 'neutral' },
];

export const hiringFunnel: HiringFunnelStage[] = [
  { stage: 'Applications', count: 347, percentage: 100, color: 'bg-secondary-500' },
  { stage: 'Screening', count: 198, percentage: 57, color: 'bg-secondary-500' },
  { stage: 'Shortlisted', count: 64, percentage: 18, color: 'bg-secondary-500' },
  { stage: 'Interview', count: 18, percentage: 5, color: 'bg-warning-500' },
  { stage: 'Offer', count: 4, percentage: 1, color: 'bg-success-500' },
];

export const activeJobs: RecruiterJob[] = [
  {
    id: '1',
    title: 'Senior Frontend Developer',
    department: 'Engineering',
    location: 'San Francisco, CA',
    type: 'Full-time',
    applications: 42,
    status: 'Active',
    postedDate: '3 days ago',
  },
  {
    id: '2',
    title: 'Product Designer',
    department: 'Design',
    location: 'Remote',
    type: 'Full-time',
    applications: 28,
    status: 'Active',
    postedDate: '1 week ago',
  },
  {
    id: '3',
    title: 'Backend Engineer',
    department: 'Engineering',
    location: 'New York, NY',
    type: 'Full-time',
    applications: 35,
    status: 'Active',
    postedDate: '5 days ago',
  },
  {
    id: '4',
    title: 'Marketing Manager',
    department: 'Marketing',
    location: 'Austin, TX',
    type: 'Full-time',
    applications: 19,
    status: 'Paused',
    postedDate: '2 weeks ago',
  },
  {
    id: '5',
    title: 'Data Analyst Intern',
    department: 'Data',
    location: 'Remote',
    type: 'Internship',
    applications: 67,
    status: 'Active',
    postedDate: '4 days ago',
  },
];

export const candidatePipeline: RecruiterCandidate[] = [
  {
    id: '1',
    name: 'Alex Morgan',
    role: 'Senior Frontend Developer',
    experience: '7 years',
    skills: ['React', 'TypeScript', 'Node.js'],
    stage: 'Interview',
    matchScore: 94,
  },
  {
    id: '2',
    name: 'Sarah Chen',
    role: 'Product Designer',
    experience: '5 years',
    skills: ['Figma', 'UI/UX', 'Prototyping'],
    stage: 'Shortlisted',
    matchScore: 88,
  },
  {
    id: '3',
    name: 'James Wilson',
    role: 'Backend Engineer',
    experience: '6 years',
    skills: ['Java', 'Spring Boot', 'AWS'],
    stage: 'Screening',
    matchScore: 82,
  },
  {
    id: '4',
    name: 'Maria Garcia',
    role: 'Senior Frontend Developer',
    experience: '4 years',
    skills: ['React', 'CSS', 'GraphQL'],
    stage: 'New',
    matchScore: 76,
  },
  {
    id: '5',
    name: 'David Kim',
    role: 'Data Analyst Intern',
    experience: '1 year',
    skills: ['Python', 'SQL', 'Tableau'],
    stage: 'Selected',
    matchScore: 91,
  },
  {
    id: '6',
    name: 'Emily Brown',
    role: 'Product Designer',
    experience: '3 years',
    skills: ['Sketch', 'Adobe XD', 'Wireframing'],
    stage: 'New',
    matchScore: 70,
  },
];

export const recentApplications: RecruiterApplication[] = [
  {
    id: '1',
    candidateName: 'Alex Morgan',
    jobTitle: 'Senior Frontend Developer',
    appliedDate: 'Jan 15, 2025',
    status: 'Interview',
    matchScore: 94,
  },
  {
    id: '2',
    candidateName: 'Sarah Chen',
    jobTitle: 'Product Designer',
    appliedDate: 'Jan 14, 2025',
    status: 'Shortlisted',
    matchScore: 88,
  },
  {
    id: '3',
    candidateName: 'James Wilson',
    jobTitle: 'Backend Engineer',
    appliedDate: 'Jan 14, 2025',
    status: 'Screening',
    matchScore: 82,
  },
  {
    id: '4',
    candidateName: 'Maria Garcia',
    jobTitle: 'Senior Frontend Developer',
    appliedDate: 'Jan 13, 2025',
    status: 'New',
    matchScore: 76,
  },
  {
    id: '5',
    candidateName: 'David Kim',
    jobTitle: 'Data Analyst Intern',
    appliedDate: 'Jan 12, 2025',
    status: 'Hired',
    matchScore: 91,
  },
  {
    id: '6',
    candidateName: 'Emily Brown',
    jobTitle: 'Product Designer',
    appliedDate: 'Jan 11, 2025',
    status: 'Rejected',
    matchScore: 70,
  },
];

export const upcomingInterviews: RecruiterInterview[] = [
  {
    id: '1',
    candidateName: 'Alex Morgan',
    jobTitle: 'Senior Frontend Developer',
    date: 'Tomorrow',
    time: '10:00 AM',
    type: 'Video',
    status: 'Scheduled',
  },
  {
    id: '2',
    candidateName: 'Sarah Chen',
    jobTitle: 'Product Designer',
    date: 'Jan 20, 2025',
    time: '2:30 PM',
    type: 'In-person',
    status: 'Scheduled',
  },
  {
    id: '3',
    candidateName: 'James Wilson',
    jobTitle: 'Backend Engineer',
    date: 'Jan 21, 2025',
    time: '11:00 AM',
    type: 'Phone',
    status: 'Scheduled',
  },
];

export const recruiterNotifications: RecruiterNotification[] = [
  {
    id: '1',
    title: 'New Application',
    message: 'Maria Garcia applied for Senior Frontend Developer.',
    time: '2 hours ago',
    read: false,
    type: 'application',
  },
  {
    id: '2',
    title: 'Interview Scheduled',
    message: 'Alex Morgan confirmed the video interview for tomorrow.',
    time: '5 hours ago',
    read: false,
    type: 'interview',
  },
  {
    id: '3',
    title: 'Candidate Match',
    message: '3 new high-match candidates found for Backend Engineer.',
    time: '1 day ago',
    read: true,
    type: 'candidate',
  },
  {
    id: '4',
    title: 'Job Approved',
    message: 'Data Analyst Intern posting is now live.',
    time: '2 days ago',
    read: true,
    type: 'system',
  },
];

export const recruiterInsights: RecruiterInsight[] = [
  {
    id: '1',
    type: 'matching',
    title: 'Candidate Matching',
    description: '5 candidates with 85%+ match scores are available for your Senior Frontend Developer role.',
    actionLabel: 'View Matches',
  },
  {
    id: '2',
    type: 'description',
    title: 'Job Description Tip',
    description: 'Adding salary ranges to job postings increases application rates by 30%.',
    actionLabel: 'Edit Postings',
  },
  {
    id: '3',
    type: 'pipeline',
    title: 'Pipeline Health',
    description: 'Your Product Designer pipeline has a strong conversion rate of 24% from screening.',
    actionLabel: 'View Pipeline',
  },
  {
    id: '4',
    type: 'bottleneck',
    title: 'Hiring Bottleneck',
    description: 'Backend Engineer has 35 applications pending screening. Consider prioritizing review.',
    actionLabel: 'Review Queue',
  },
];

export const quickActions = [
  {
    label: 'Post a Job',
    icon: 'M12 4.5v15m7.5-7.5h-15',
    href: '/recruiter/jobs/new',
  },
  {
    label: 'Review Applications',
    icon: 'M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z',
    href: '/recruiter/applications',
  },
  {
    label: 'Find Candidates',
    icon: 'M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z',
    href: '/recruiter/candidates',
  },
  {
    label: 'Schedule Interview',
    icon: 'M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5',
    href: '/recruiter/interviews',
  },
];

export const companyInfo = {
  name: 'Demo Company',
  role: 'Recruiter Account',
  initials: 'DC',
};
