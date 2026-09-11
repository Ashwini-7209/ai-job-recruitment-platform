import type {
  CandidateJob,
  CandidateApplication,
  CandidateInterview,
  CandidateNotification,
  CandidateInsight,
} from '@/types/candidate';

export const mockRecommendedJobs: CandidateJob[] = [
  {
    id: '1',
    title: 'Senior Frontend Developer',
    company: 'TechNova Inc.',
    location: 'San Francisco, CA',
    type: 'Full-time',
    experience: '5-8 years',
    matchScore: 94,
    postedDate: '2 days ago',
    salary: '$140k - $180k',
  },
  {
    id: '2',
    title: 'React Engineer',
    company: 'CloudSync Solutions',
    location: 'Remote',
    type: 'Full-time',
    experience: '3-5 years',
    matchScore: 88,
    postedDate: '1 day ago',
    salary: '$120k - $155k',
  },
  {
    id: '3',
    title: 'UI/UX Developer',
    company: 'DesignLab Pro',
    location: 'New York, NY',
    type: 'Full-time',
    experience: '2-4 years',
    matchScore: 82,
    postedDate: '5 days ago',
    salary: '$100k - $130k',
  },
  {
    id: '4',
    title: 'Full Stack Developer',
    company: 'DataStream Analytics',
    location: 'Austin, TX',
    type: 'Contract',
    experience: '4-6 years',
    matchScore: 76,
    postedDate: '3 days ago',
    salary: '$110k - $145k',
  },
];

export const mockApplications: CandidateApplication[] = [
  { id: '1', jobTitle: 'Senior Frontend Developer', company: 'TechNova Inc.', appliedDate: 'Jan 15, 2025', status: 'Under Review' },
  { id: '2', jobTitle: 'React Engineer', company: 'CloudSync Solutions', appliedDate: 'Jan 12, 2025', status: 'Shortlisted' },
  { id: '3', jobTitle: 'UI/UX Developer', company: 'DesignLab Pro', appliedDate: 'Jan 10, 2025', status: 'Interview' },
  { id: '4', jobTitle: 'Full Stack Developer', company: 'DataStream Analytics', appliedDate: 'Jan 8, 2025', status: 'Applied' },
  { id: '5', jobTitle: 'Frontend Lead', company: 'InnovateTech', appliedDate: 'Jan 5, 2025', status: 'Rejected' },
];

export const mockInterviews: CandidateInterview[] = [
  { id: '1', jobTitle: 'UI/UX Developer', company: 'DesignLab Pro', date: 'Tomorrow', time: '10:00 AM', type: 'Video' },
  { id: '2', jobTitle: 'React Engineer', company: 'CloudSync Solutions', date: 'Jan 22, 2025', time: '2:30 PM', type: 'Phone' },
];

export const mockNotifications: CandidateNotification[] = [
  { id: '1', title: 'Application Viewed', message: 'Your application for Senior Frontend Developer at TechNova was viewed.', time: '2 hours ago', read: false, type: 'application' },
  { id: '2', title: 'Interview Scheduled', message: 'Interview for UI/UX Developer at DesignLab Pro is scheduled for tomorrow.', time: '5 hours ago', read: false, type: 'interview' },
  { id: '3', title: 'Profile Tip', message: 'Completing your portfolio section can increase profile visibility by 40%.', time: '1 day ago', read: true, type: 'system' },
  { id: '4', title: 'New Match', message: '3 new jobs match your skills. Check your recommendations.', time: '2 days ago', read: true, type: 'system' },
];

export const mockInsights: CandidateInsight[] = [
  { id: '1', type: 'resume', title: 'Resume Improvement', description: 'Adding quantified achievements to your resume can increase callback rates by 35%.', actionLabel: 'Update Resume' },
  { id: '2', type: 'skill', title: 'Skill Gap Alert', description: 'TypeScript is mentioned in 78% of jobs you view. Consider highlighting it.', actionLabel: 'Add Skills' },
  { id: '3', type: 'profile', title: 'Profile Strength', description: 'Your profile is 72% complete. Adding a portfolio link can boost it to 85%.', actionLabel: 'Complete Profile' },
];

export const mockStats = {
  applied: 12,
  underReview: 4,
  shortlisted: 2,
  interviews: 3,
};

export const profileCompletion = 72;
