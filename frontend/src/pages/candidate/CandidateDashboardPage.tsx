import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Skeleton } from '@/components/ui';
import { useAuth } from '@/contexts';
import { applicationService } from '@/services/application.service';
import { interviewService } from '@/services/interview.service';
import { savedJobService, type SavedJob } from '@/services/savedJob.service';
import { jobService, type JobRecommendation } from '@/services/job.service';
import { notificationService, type Notification } from '@/services/notification.service';
import { profileService, type ProfileCompletion } from '@/services/profile.service';
import { aiService, type CareerInsightsResponse } from '@/services/ai.service';
import type { CandidateApplicationSummary, CandidateApplicationStats, CandidateInterviewDetail } from '@/types/candidate';

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

const notificationTypeColors: Record<string, string> = {
  APPLICATION_RECEIVED: 'bg-info-100 text-info-700',
  APPLICATION_STATUS_CHANGED: 'bg-secondary-100 text-secondary-700',
  INTERVIEW_SCHEDULED: 'bg-secondary-100 text-secondary-700',
  INTERVIEW_RESCHEDULED: 'bg-secondary-100 text-secondary-700',
  INTERVIEW_CANCELLED: 'bg-warning-100 text-warning-700',
  JOB_RECOMMENDATION: 'bg-success-100 text-success-700',
  JOB_ALERT_MATCH: 'bg-success-100 text-success-700',
  GENERAL: 'bg-neutral-100 text-neutral-600',
};

const insightTypeIcons: Record<string, string> = {
  resume: 'M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z',
  skill: 'M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 00-2.455 2.456z',
  profile: 'M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z',
};

function ProfileCompletionCard() {
  const navigate = useNavigate();
  const [completion, setCompletion] = useState<ProfileCompletion | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCompletion = async () => {
      try {
        setLoading(true);
        const result = await profileService.getProfileCompletion();
        setCompletion(result);
      } catch {
        // silently fail
      } finally {
        setLoading(false);
      }
    };
    fetchCompletion();
  }, []);

  return (
    <Card className="col-span-full lg:col-span-1">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-heading-sm text-neutral-900">Profile Completion</h3>
          {loading ? (
            <Skeleton className="h-5 w-10" />
          ) : (
            <span className="text-heading-sm text-secondary-600">{completion?.overallPercentage ?? 0}%</span>
          )}
        </div>
        <div className="w-full h-2 bg-neutral-100 rounded-full overflow-hidden mb-4">
          {loading ? (
            <Skeleton className="h-full w-full" />
          ) : (
            <div
              className="h-full bg-secondary-500 rounded-full transition-all duration-500"
              style={{ width: `${completion?.overallPercentage ?? 0}%` }}
            />
          )}
        </div>
        <div className="space-y-2">
          {(loading ? Array.from({ length: 5 }) : (completion?.sections ?? [])).map((item, i) => (
            <div key={loading ? i : (item as { label: string }).label} className="flex items-center gap-2 text-body-sm">
              {loading ? (
                <Skeleton className="h-4 w-28" />
              ) : (
                <>
                  <span className={(item as { completed: boolean }).completed ? 'text-success-500' : 'text-neutral-300'}>
                    {(item as { completed: boolean }).completed ? (
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="20,6 9,17 4,12" />
                      </svg>
                    ) : (
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                      </svg>
                    )}
                  </span>
                  <span className={(item as { completed: boolean }).completed ? 'text-neutral-600' : 'text-neutral-400'}>{(item as { label: string }).label}</span>
                </>
              )}
            </div>
          ))}
        </div>
        <Button variant="outline" size="sm" fullWidth className="mt-4" onClick={() => navigate('/candidate/profile')}>
          Complete Profile
        </Button>
      </CardContent>
    </Card>
  );
}

function QuickActionsCard() {
  const navigate = useNavigate();
  const actions = [
    { label: 'Browse Jobs', icon: 'M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z', href: '/candidate/jobs' },
    { label: 'Upload Resume', icon: 'M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5', href: '/candidate/resume' },
    { label: 'Practice Interview', icon: 'M15.75 10.5l4.72-4.72a.75.75 0 011.28.53v11.38a.75.75 0 01-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25h-9A2.25 2.25 0 002.25 7.5v9a2.25 2.25 0 002.25 2.25z', href: '/candidate/ai' },
    { label: 'View Applications', icon: 'M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25z', href: '/candidate/applications' },
  ];

  return (
    <Card className="col-span-full lg:col-span-1">
      <CardContent className="p-5">
        <h3 className="text-heading-sm text-neutral-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-2 gap-2">
          {actions.map((action) => (
            <button
              key={action.label}
              type="button"
              onClick={() => navigate(action.href)}
              className="flex flex-col items-center gap-2 rounded-lg border border-neutral-200 p-4 text-center hover:border-secondary-300 hover:bg-secondary-50/50 transition-all duration-150 group cursor-pointer"
            >
              <span className="text-neutral-400 group-hover:text-secondary-500 transition-colors">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d={action.icon} />
                </svg>
              </span>
              <span className="text-body-sm font-medium text-neutral-700 group-hover:text-secondary-700">{action.label}</span>
            </button>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

function ApplicationStatsCard({ stats, loading }: { stats: CandidateApplicationStats | null; loading: boolean }) {
  const statItems = stats
    ? [
        { label: 'Applied', value: stats.appliedCount, color: 'text-info-600', bg: 'bg-info-50' },
        { label: 'Under Review', value: stats.underReviewCount, color: 'text-warning-600', bg: 'bg-warning-50' },
        { label: 'Shortlisted', value: stats.shortlistedCount, color: 'text-success-600', bg: 'bg-success-50' },
        { label: 'Hired', value: stats.hiredCount, color: 'text-secondary-600', bg: 'bg-secondary-50' },
      ]
    : [
        { label: 'Applied', value: 0, color: 'text-info-600', bg: 'bg-info-50' },
        { label: 'Under Review', value: 0, color: 'text-warning-600', bg: 'bg-warning-50' },
        { label: 'Shortlisted', value: 0, color: 'text-success-600', bg: 'bg-success-50' },
        { label: 'Hired', value: 0, color: 'text-secondary-600', bg: 'bg-secondary-50' },
      ];

  return (
    <Card className="col-span-full">
      <CardContent className="p-5">
        <h3 className="text-heading-sm text-neutral-900 mb-4">Application Overview</h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {statItems.map((stat) => (
            <div key={stat.label} className={`rounded-lg ${stat.bg} p-3 text-center`}>
              {loading ? (
                <Skeleton className="h-7 w-10 mx-auto mb-1" />
              ) : (
                <p className={`text-display-sm font-bold ${stat.color}`}>{stat.value}</p>
              )}
              <p className="text-caption mt-1">{stat.label}</p>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

function RecommendedJobsCard() {
  const navigate = useNavigate();
  const [recommendations, setRecommendations] = useState<JobRecommendation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchJobs = async () => {
      try {
        setLoading(true);
        const result = await jobService.getRecommendations(0, 5);
        setRecommendations(result.matches ?? []);
      } catch {
        // silently fail
      } finally {
        setLoading(false);
      }
    };
    fetchJobs();
  }, []);

  return (
    <Card className="col-span-full">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">Recommended for You</h3>
          <button type="button" onClick={() => navigate('/candidate/jobs')} className="text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            View all
          </button>
        </div>
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="rounded-lg border border-neutral-100 p-4 space-y-3">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-32" />
                    <Skeleton className="h-3 w-20" />
                  </div>
                  <Skeleton className="h-8 w-8 rounded-full" />
                </div>
                <div className="flex gap-2">
                  <Skeleton className="h-5 w-16 rounded-full" />
                  <Skeleton className="h-5 w-20 rounded-full" />
                </div>
                <Skeleton className="h-3 w-24" />
              </div>
            ))}
          </div>
        ) : recommendations.length === 0 ? (
          <p className="text-body-sm text-neutral-500 text-center py-6">No recommendations available yet</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {recommendations.map((rec) => (
              <RecommendedJobCard key={rec.jobId} recommendation={rec} />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function getScoreColor(score: number): string {
  if (score >= 80) return 'text-success-600 bg-success-50';
  if (score >= 60) return 'text-secondary-600 bg-secondary-50';
  if (score >= 40) return 'text-warning-600 bg-warning-50';
  return 'text-neutral-600 bg-neutral-100';
}

function getScoreRingColor(score: number): string {
  if (score >= 80) return 'stroke-success-500';
  if (score >= 60) return 'stroke-secondary-500';
  if (score >= 40) return 'stroke-warning-500';
  return 'stroke-neutral-400';
}

function ScoreBadge({ score }: { score: number }) {
  const radius = 16;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;

  return (
    <div className="relative flex items-center justify-center w-10 h-10 shrink-0">
      <svg width="40" height="40" className="-rotate-90">
        <circle cx="20" cy="20" r={radius} fill="none" stroke="currentColor" strokeWidth="3" className="text-neutral-100" />
        <circle
          cx="20"
          cy="20"
          r={radius}
          fill="none"
          strokeWidth="3"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          className={getScoreRingColor(score)}
        />
      </svg>
      <span className={`absolute text-xs font-bold ${getScoreColor(score).split(' ')[0]}`}>{score}</span>
    </div>
  );
}

function RecommendedJobCard({ recommendation }: { recommendation: JobRecommendation }) {
  const navigate = useNavigate();

  return (
    <div
      className="flex flex-col rounded-lg border border-neutral-200 p-4 hover:border-secondary-200 hover:shadow-sm transition-all duration-150 cursor-pointer"
      onClick={() => navigate('/candidate/jobs/' + recommendation.jobId)}
    >
      <div className="flex items-start justify-between gap-2 mb-2">
        <div className="min-w-0 flex-1">
          <h4 className="text-body-md font-semibold text-neutral-900 truncate">{recommendation.jobTitle}</h4>
        </div>
        <ScoreBadge score={recommendation.overallScore} />
      </div>

      <div className="flex flex-wrap items-center gap-1.5 mb-3">
        {(recommendation.matchedSkills || []).slice(0, 3).map((skill) => (
          <span key={skill} className="inline-flex items-center rounded-full bg-success-50 px-2 py-0.5 text-caption text-success-700">
            {skill}
          </span>
        ))}
        {(recommendation.matchedSkills || []).length > 3 && (
          <span className="text-caption text-neutral-400">+{(recommendation.matchedSkills || []).length - 3}</span>
        )}
      </div>

      <div className="flex items-center gap-3 mt-auto text-caption text-neutral-400">
        <span className={`rounded-full px-2 py-0.5 font-medium ${getScoreColor(recommendation.skillScore)}`}>
          Skills {recommendation.skillScore}%
        </span>
        <span className={`rounded-full px-2 py-0.5 font-medium ${getScoreColor(recommendation.experienceScore)}`}>
          Experience {recommendation.experienceScore}%
        </span>
      </div>
    </div>
  );
}

const statusBadgeVariant: Record<string, 'info' | 'warning' | 'success' | 'error' | 'default' | 'primary'> = {
  APPLIED: 'info',
  UNDER_REVIEW: 'warning',
  SHORTLISTED: 'success',
  INTERVIEW: 'info',
  REJECTED: 'error',
  HIRED: 'primary',
  WITHDRAWN: 'default',
};

const statusLabels: Record<string, string> = {
  APPLIED: 'Applied',
  UNDER_REVIEW: 'Under Review',
  SHORTLISTED: 'Shortlisted',
  INTERVIEW: 'Interview',
  REJECTED: 'Rejected',
  HIRED: 'Hired',
  WITHDRAWN: 'Withdrawn',
};

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function RecentApplicationsCard({ applications, loading }: { applications: CandidateApplicationSummary[]; loading: boolean }) {
  const navigate = useNavigate();
  return (
    <Card className="col-span-full xl:col-span-2">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">Recent Applications</h3>
          <button type="button" onClick={() => navigate('/candidate/applications')} className="text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            View all
          </button>
        </div>
        {loading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex items-center justify-between gap-3 rounded-lg border border-neutral-100 p-3">
                <div className="flex-1">
                  <Skeleton className="h-4 w-32 mb-1.5" />
                  <Skeleton className="h-3 w-20" />
                </div>
                <Skeleton className="h-5 w-16 rounded-full" />
              </div>
            ))}
          </div>
        ) : applications.length === 0 ? (
          <p className="text-body-sm text-neutral-500 text-center py-6">No applications yet</p>
        ) : (
          <>
            {/* Desktop table */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-neutral-100">
                    <th className="pb-2 text-overline">Job</th>
                    <th className="pb-2 text-overline">Location</th>
                    <th className="pb-2 text-overline">Applied</th>
                    <th className="pb-2 text-overline">Status</th>
                    <th className="pb-2 text-overline text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {applications.map((app) => (
                    <tr key={app.applicationId} className="border-b border-neutral-50 last:border-0">
                      <td className="py-3 text-body-md font-medium text-neutral-900">{app.jobTitle}</td>
                      <td className="py-3 text-body-sm text-neutral-500">{app.location || 'Remote'}</td>
                      <td className="py-3 text-body-sm text-neutral-500">{formatDate(app.appliedAt)}</td>
                      <td className="py-3">
                        <Badge variant={statusBadgeVariant[app.status]} size="sm">{statusLabels[app.status]}</Badge>
                      </td>
                      <td className="py-3 text-right">
                        <Button variant="ghost" size="sm" onClick={() => navigate(`/candidate/applications/${app.applicationId}`)}>Details</Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {/* Mobile card list */}
            <div className="md:hidden space-y-3">
              {applications.map((app) => (
                <div key={app.applicationId} className="rounded-lg border border-neutral-100 p-3 cursor-pointer hover:border-secondary-200 transition-colors" onClick={() => navigate(`/candidate/applications/${app.applicationId}`)}>
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <p className="text-body-md font-medium text-neutral-900 truncate">{app.jobTitle}</p>
                      <p className="text-body-sm text-neutral-500">{app.location || 'Remote'}</p>
                    </div>
                    <Badge variant={statusBadgeVariant[app.status]} size="sm">{statusLabels[app.status]}</Badge>
                  </div>
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-caption">{formatDate(app.appliedAt)}</span>
                    <Button variant="ghost" size="sm">Details</Button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function InterviewPreviewCard({ interviews, loading }: { interviews: { interviewId: number; title: string; jobTitle: string; scheduledStart: string; interviewType: string; status: string }[]; loading: boolean }) {
  const navigate = useNavigate();
  const typeIconsMap: Record<string, string> = {
    VIDEO: 'M15.75 10.5l4.72-4.72a.75.75 0 011.28.53v11.38a.75.75 0 01-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25h-9A2.25 2.25 0 002.25 7.5v9a2.25 2.25 0 002.25 2.25z',
    PHONE: 'M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z',
    IN_PERSON: 'M15 10.5a3 3 0 11-6 0 3 3 0 016 0z M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z',
  };
  const typeLabelsMap: Record<string, string> = { VIDEO: 'Video', PHONE: 'Phone', IN_PERSON: 'In-person' };

  return (
    <Card className="col-span-full xl:col-span-1">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">Upcoming Interviews</h3>
          <button type="button" onClick={() => navigate('/candidate/interviews')} className="text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            View all
          </button>
        </div>
        {loading ? (
          <div className="space-y-3">
            {Array.from({ length: 2 }).map((_, i) => (
              <div key={i} className="flex items-start gap-3 rounded-lg border border-neutral-100 p-3">
                <Skeleton className="h-10 w-10 rounded-lg" />
                <div className="flex-1">
                  <Skeleton className="h-4 w-32 mb-1.5" />
                  <Skeleton className="h-3 w-20" />
                </div>
              </div>
            ))}
          </div>
        ) : interviews.length === 0 ? (
          <p className="text-body-sm text-neutral-500 text-center py-6">No upcoming interviews</p>
        ) : (
          <div className="space-y-3">
            {interviews.map((interview) => (
              <div key={interview.interviewId} className="flex items-start gap-3 rounded-lg border border-neutral-100 p-3 cursor-pointer hover:border-secondary-200 transition-colors" onClick={() => navigate('/candidate/interviews')}>
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-secondary-50 text-secondary-600">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d={typeIconsMap[interview.interviewType] || typeIconsMap.VIDEO} />
                  </svg>
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-body-md font-medium text-neutral-900 truncate">{interview.title}</p>
                  <p className="text-body-sm text-neutral-500">{interview.jobTitle}</p>
                  <div className="flex items-center gap-2 mt-1 text-caption">
                    <span className="font-medium text-neutral-700">{new Date(interview.scheduledStart).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</span>
                    <span>at</span>
                    <span className="font-medium text-neutral-700">{new Date(interview.scheduledStart).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</span>
                    <Badge variant="secondary" size="sm">{typeLabelsMap[interview.interviewType] || interview.interviewType}</Badge>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function AIInsightsCard() {
  const [insights, setInsights] = useState<CareerInsightsResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchInsights = async () => {
      try {
        setLoading(true);
        const response = await aiService.getCareerInsights();
        setInsights(response.data || null);
      } catch {
        // silently fail
      } finally {
        setLoading(false);
      }
    };
    fetchInsights();
  }, []);

  const insightItems = insights
    ? [
        ...(insights.strengths?.length > 0 ? [{ type: 'profile', title: 'Your Strengths', items: insights.strengths }] : []),
        ...(insights.recommendedSkills?.length > 0 ? [{ type: 'skill', title: 'Recommended Skills', items: insights.recommendedSkills }] : []),
        ...(insights.profileImprovements?.length > 0 ? [{ type: 'resume', title: 'Profile Improvements', items: insights.profileImprovements }] : []),
      ]
    : [];

  return (
    <Card className="col-span-full">
      <CardContent className="p-5">
        <div className="flex items-center gap-2 mb-4">
          <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-secondary-500 to-secondary-500">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="13,2 3,14 12,14 11,22 21,10 12,10" />
            </svg>
          </div>
          <h3 className="text-heading-sm text-neutral-900">AI Career Insights</h3>
        </div>
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="rounded-lg border border-neutral-100 bg-neutral-50/50 p-4 space-y-2">
                <Skeleton className="h-4 w-20" />
                <Skeleton className="h-5 w-32" />
                <Skeleton className="h-3 w-full" />
                <Skeleton className="h-3 w-24" />
              </div>
            ))}
          </div>
        ) : insightItems.length === 0 ? (
          <p className="text-body-sm text-neutral-500 text-center py-6">No insights available yet</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {insightItems.map((insight) => (
              <div key={insight.title} className="rounded-lg border border-neutral-100 bg-neutral-50/50 p-4">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-secondary-500">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d={insightTypeIcons[insight.type as string] || insightTypeIcons.profile} />
                    </svg>
                  </span>
                  <span className="text-overline">{(insight.type as string).toUpperCase()}</span>
                </div>
                <h4 className="text-body-md font-semibold text-neutral-900 mb-1">{insight.title}</h4>
                <ul className="text-body-sm text-neutral-500 space-y-1">
                  {(insight.items || []).slice(0, 3).map((item, idx) => (
                    <li key={idx}>{item}</li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function SavedJobsPreviewCard({ savedJobs, loading }: { savedJobs: SavedJob[]; loading: boolean }) {
  const navigate = useNavigate();
  return (
    <Card className="col-span-full xl:col-span-1">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">Saved Jobs</h3>
          <button type="button" onClick={() => navigate('/candidate/saved')} className="text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            View all
          </button>
        </div>
        {loading ? (
          <div className="space-y-3">
            {Array.from({ length: 2 }).map((_, i) => (
              <div key={i} className="rounded-lg border border-neutral-100 p-3">
                <Skeleton className="h-4 w-32 mb-1.5" />
                <Skeleton className="h-3 w-20" />
              </div>
            ))}
          </div>
        ) : savedJobs.length === 0 ? (
          <div className="text-center py-6">
            <p className="text-body-sm text-neutral-500 mb-3">No saved jobs yet</p>
            <Button variant="outline" size="sm" onClick={() => navigate('/candidate/jobs')}>
              Browse Jobs
            </Button>
          </div>
        ) : (
          <div className="space-y-3">
            {savedJobs.map((job) => (
              <div key={job.savedJobId} className="rounded-lg border border-neutral-100 p-3 hover:border-secondary-200 transition-colors">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <p className="text-body-md font-medium text-neutral-900 truncate">{job.jobTitle}</p>
                    <p className="text-body-sm text-neutral-500">{job.location || 'Remote'}</p>
                  </div>
                  <Badge variant={job.jobStatus === 'PUBLISHED' ? 'success' : 'error'} size="sm">
                    {job.jobStatus === 'PUBLISHED' ? 'Open' : 'Closed'}
                  </Badge>
                </div>
                <div className="flex items-center justify-between mt-2">
                  <span className="text-caption text-neutral-400">
                    Saved {new Date(job.savedAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                  </span>
                  {job.applied && <Badge variant="info" size="sm">Applied</Badge>}
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function NotificationPreviewCard() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        setLoading(true);
        const result = await notificationService.getNotifications({ size: 5 });
        setNotifications(result.content ?? []);
      } catch {
        // silently fail
      } finally {
        setLoading(false);
      }
    };
    fetchNotifications();
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <Card className="col-span-full lg:col-span-1">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">Recent Notifications</h3>
          {loading ? (
            <Skeleton className="h-5 w-12" />
          ) : (
            <Badge variant="error" size="sm">{unreadCount} new</Badge>
          )}
        </div>
        {loading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex items-start gap-3 rounded-lg p-2.5">
                <Skeleton className="h-7 w-7 rounded-full shrink-0" />
                <div className="flex-1 space-y-1.5">
                  <Skeleton className="h-3 w-32" />
                  <Skeleton className="h-3 w-full" />
                  <Skeleton className="h-3 w-16" />
                </div>
              </div>
            ))}
          </div>
        ) : notifications.length === 0 ? (
          <p className="text-body-sm text-neutral-500 text-center py-6">No notifications yet</p>
        ) : (
          <div className="space-y-3">
            {notifications.map((notif) => (
              <div
                key={notif.id}
                className={`flex items-start gap-3 rounded-lg p-2.5 transition-colors ${
                  notif.read ? 'bg-transparent' : 'bg-secondary-50/50'
                }`}
              >
                <div className={`mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full ${notificationTypeColors[notif.type] || 'bg-neutral-100 text-neutral-600'}`}>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                  </svg>
                </div>
                <div className="min-w-0 flex-1">
                  <p className={`text-body-sm ${notif.read ? 'text-neutral-600' : 'font-medium text-neutral-900'}`}>{notif.title}</p>
                  <p className="text-caption mt-0.5 line-clamp-2">{notif.message}</p>
                  <p className="text-caption mt-1 text-neutral-400">{new Date(notif.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export default function CandidateDashboardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [stats, setStats] = useState<CandidateApplicationStats | null>(null);
  const [recentApps, setRecentApps] = useState<CandidateApplicationSummary[]>([]);
  const [upcomingInterviews, setUpcomingInterviews] = useState<CandidateInterviewDetail[]>([]);
  const [recentSavedJobs, setRecentSavedJobs] = useState<SavedJob[]>([]);
  const [statsLoading, setStatsLoading] = useState(true);
  const [appsLoading, setAppsLoading] = useState(true);
  const [interviewsLoading, setInterviewsLoading] = useState(true);
  const [savedJobsLoading, setSavedJobsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setStatsLoading(true);
        const statsData = await applicationService.getStats();
        setStats(statsData);
      } catch {
        // silently fail, dashboard still works with mock data
      } finally {
        setStatsLoading(false);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    const fetchRecent = async () => {
      try {
        setAppsLoading(true);
        const appsData = await applicationService.listApplications({ size: 5, sort: 'newest' });
        setRecentApps(appsData.content ?? []);
      } catch {
        // silently fail
      } finally {
        setAppsLoading(false);
      }
    };
    fetchRecent();
  }, []);

  useEffect(() => {
    const fetchInterviews = async () => {
      try {
        setInterviewsLoading(true);
        const data = await interviewService.getUpcomingCandidateInterviews();
        setUpcomingInterviews(data);
      } catch {
        // silently fail
      } finally {
        setInterviewsLoading(false);
      }
    };
    fetchInterviews();
  }, []);

  useEffect(() => {
    const fetchSavedJobs = async () => {
      try {
        setSavedJobsLoading(true);
        const data = await savedJobService.getSavedJobs({ size: 3 });
        setRecentSavedJobs(data.content ?? []);
      } catch {
        // silently fail
      } finally {
        setSavedJobsLoading(false);
      }
    };
    fetchSavedJobs();
  }, []);

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      {/* Welcome + Profile Completion + Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-heading-lg text-neutral-900">{getGreeting()}, {user?.fullName?.split(' ')[0] || 'there'}</h2>
          <p className="text-body-md text-neutral-500 mt-1">Here's what's happening with your job search today.</p>
        </div>
        <Button onClick={() => navigate('/candidate/jobs')} className="w-full sm:w-auto">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-1">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          Find Jobs
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <ProfileCompletionCard />
        <QuickActionsCard />
      </div>

      <ApplicationStatsCard stats={stats} loading={statsLoading} />

      <RecommendedJobsCard />

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <RecentApplicationsCard applications={recentApps} loading={appsLoading} />
        <SavedJobsPreviewCard savedJobs={recentSavedJobs} loading={savedJobsLoading} />
      </div>

      <AIInsightsCard />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <InterviewPreviewCard interviews={upcomingInterviews} loading={interviewsLoading} />
        <NotificationPreviewCard />
      </div>
    </div>
  );
}
